#!/usr/bin/env node
/**
 * e2e シナリオテスト: 試合を1つまるごと実行する。
 *
 *   1. Spigot 1.14.4 + 依存プラグイン + Sclat.jar でテストサーバーを起動
 *   2. mineflayer のボットを StartPlayerCount 体接続 (ServerType: MATCH なので自動参加)
 *   3. 30秒カウントダウン -> イントロ -> 試合 (MatchTime.Default 秒) -> リザルト
 *   4. 各フェーズの到達と、プラグイン由来の例外が無いことを検証して result を出す
 *
 * 使い方: node e2e/scenario/run-match.mjs [--bots 2] [--keep] [--verbose]
 */
import mineflayer from 'mineflayer'
import net from 'node:net'
import { spawn } from 'node:child_process'
import { appendFile, mkdir, writeFile } from 'node:fs/promises'
import { existsSync, readdirSync } from 'node:fs'
import { homedir } from 'node:os'
import { dirname, join } from 'node:path'
import { prepare, repoRoot, runRoot, serverDir } from './prepare.mjs'

const args = process.argv.slice(2)
const opt = {
  bots: Number(getArg('--bots') ?? 2),
  keep: args.includes('--keep'),
  verbose: args.includes('--verbose'),
  port: Number(getArg('--port') ?? 25599),
  timeoutStart: Number(getArg('--timeout-start') ?? 150_000),
  timeoutMatch: Number(getArg('--timeout-match') ?? 240_000),
  noPrepare: args.includes('--no-prepare'),
}

function getArg(name) {
  const i = args.indexOf(name)
  return i >= 0 ? args[i + 1] : undefined
}

const t0 = Date.now()
const stamp = () => `${((Date.now() - t0) / 1000).toFixed(1).padStart(6)}s`
const log = (msg) => console.log(`${stamp()} ${msg}`)

/** Java 11 (Spigot 1.14.4 の上限) を優先して解決する */
function resolveJava() {
  if (process.env.SCLAT_JAVA) return process.env.SCLAT_JAVA
  const miseRoot = join(homedir(), '.local/share/mise/installs/java')
  for (const v of ['temurin-11.0.32+101', 'temurin-11']) {
    const p = join(miseRoot, v, 'bin/java')
    if (existsSync(p)) return p
  }
  for (const dir of [miseRoot, join(homedir(), '.sdkman/candidates/java')]) {
    if (!existsSync(dir)) continue
    for (const entry of readdirSync(dir)) {
      if (entry.includes('11')) {
        const p = join(dir, entry, 'bin/java')
        if (existsSync(p)) return p
      }
    }
  }
  return 'java'
}

/* ------------------------------------------------------------------ *
 * サーバー起動
 * ------------------------------------------------------------------ */
async function startServer() {
  await mkdir(runRoot, { recursive: true })
  const logPath = join(runRoot, 'server.log')
  await writeFile(logPath, '')
  const java = resolveJava()
  log(`java: ${java}`)

  const cp = [join(serverDir, 'spigot.jar'), join(serverDir, 'slf4j-api.jar'), join(serverDir, 'slf4j-jdk14.jar')].join(':')
  const child = spawn(java, ['-Xms512M', '-Xmx1536M', '-cp', cp, 'org.bukkit.craftbukkit.Main', 'nogui'], {
    cwd: serverDir,
    stdio: ['pipe', 'pipe', 'pipe'],
  })

  const lines = []
  let buffer = ''
  const onData = (chunk) => {
    buffer += chunk.toString()
    let idx
    while ((idx = buffer.indexOf('\n')) >= 0) {
      const line = buffer.slice(0, idx).replace(/\r$/, '')
      buffer = buffer.slice(idx + 1)
      lines.push(line)
      appendFile(logPath, line + '\n')
      const interesting = /Sclat|ERROR\]|Exception|Done \(|Preparing|WARN\]|勝|負|引き分け|試合|Finish/.test(line)
      if (opt.verbose || interesting) console.log(`  srv| ${line}`)
    }
  }
  child.stdout.on('data', onData)
  child.stderr.on('data', onData)

  let exited = false
  child.on('exit', (code) => {
    exited = true
    log(`server exited code=${code}`)
  })

  return { child, lines, logPath, isExited: () => exited }
}

async function stopServer(server) {
  if (server.isExited()) return
  try {
    server.child.stdin.write('stop\n')
  } catch {
    /* ignore */
  }
  for (let i = 0; i < 60 && !server.isExited(); i++) await sleep(500)
  if (!server.isExited()) {
    log('server did not stop; killing')
    server.child.kill('SIGKILL')
  }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

/**
 * StatusShare(LOBBY と MATCH が Unix socket 相当の TCP で状態を共有する)の受け口。
 * 待ち受けが無いと StatusClient が ConnectException を WARN で吐き続けるので、
 * テストでは捨てるだけのサーバーを立ててログを静かにする。
 */
function startStatusShareStub(port) {
  const srv = net.createServer((sock) => {
    sock.on('error', () => {})
    sock.resume()
  })
  srv.on('error', () => {})
  srv.listen(port, '127.0.0.1')
  return srv
}

async function waitFor(predicate, timeoutMs, label) {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    const v = predicate()
    if (v) return v
    await sleep(250)
  }
  throw new Error(`timeout waiting for ${label} (${timeoutMs}ms)`)
}

/* ------------------------------------------------------------------ *
 * ボット
 * ------------------------------------------------------------------ */
const MATCH_STARTED = '試合が開始されました'
const RESULT_PATTERNS = [/You Win!/, /You Lose/, /Finish!/]

function createBot(name) {
  const bot = mineflayer.createBot({
    host: '127.0.0.1',
    port: opt.port,
    username: name,
    version: '1.14.4',
    auth: 'offline',
  })

  const state = {
    name,
    messages: [],
    titles: [],
    spawned: false,
    matchStarted: false,
    finished: false,
    died: 0,
    kicked: null,
    errors: [],
    endedAt: null,
  }

  const recordTitle = (kind, payload) => {
    const text = typeof payload === 'string' ? payload : JSON.stringify(payload)
    state.titles.push({ at: Date.now() - t0, kind, text })
    for (const p of RESULT_PATTERNS) {
      if (p.test(text)) state.finished = true
    }
  }

  bot.on('message', (msg) => {
    const text = msg.toString()
    state.messages.push({ at: Date.now() - t0, text })
    if (text.includes(MATCH_STARTED)) state.matchStarted = true
  })
  bot.on('title', (title) => recordTitle('title', title?.text ?? title))
  // 1.14.4 はタイトルが生パケットで来るので両方拾う
  bot._client.on('packet', (data, meta) => {
    if (/title/i.test(meta.name) || /subtitle/i.test(meta.name)) recordTitle(meta.name, data)
  })
  bot.on('spawn', () => {
    state.spawned = true
    log(`${name}: spawn at ${fmtPos(bot.entity.position)}`)
    startPlaying(bot, state)
  })
  bot.on('death', () => {
    state.died++
    log(`${name}: died (${state.died})`)
  })
  bot.on('kicked', (reason) => {
    state.kicked = String(reason)
    log(`${name}: kicked ${state.kicked}`)
  })
  bot.on('error', (e) => {
    state.errors.push(String(e.message ?? e))
    log(`${name}: error ${e.message}`)
  })
  bot.on('end', (reason) => {
    state.endedAt = Date.now() - t0
    log(`${name}: end (${reason})`)
  })

  return { bot, state }
}

const fmtPos = (p) => (p ? `${p.x.toFixed(1)},${p.y.toFixed(1)},${p.z.toFixed(1)}` : 'n/a')

/** 試合中のボットの操作: 中央を向いて撃ち続ける(塗り) */
function startPlaying(bot, state) {
  const timer = setInterval(() => {
    if (!bot.entity) return
    try {
      // 中央(0,0)方向を向き、足元を狙う
      const dx = 0 - bot.entity.position.x
      const dz = 0 - bot.entity.position.z
      const yaw = Math.atan2(-dx, dz) // mineflayer: yaw=0 は +z
      bot.look(yaw, 0.9, true)
      bot.activateItem()
      if (state.matchStarted && Math.random() < 0.3) {
        bot.setControlState('forward', true)
        setTimeout(() => bot.setControlState('forward', false), 400)
      }
    } catch (e) {
      /* 無視: サーバー切断中など */
    }
  }, 400)
  bot.once('end', () => clearInterval(timer))
}

/* ------------------------------------------------------------------ *
 * 検証
 * ------------------------------------------------------------------ */
const PLUGIN_FRAME = /\bat (be4rjp\.sclat\.[\w.$]+)\(/

/**
 * リザルトのタイトル群から勝敗とスコアを取り出す。
 * Normal モードは Animation.resultAnimation が
 *   タイトル "You Win!" / "You Lose..." / "引き分け！"
 *   サブタイトル "45% [ゲージ] 55%"
 * を送る。タイトルは action=0, サブタイトルは action=1 の別パケットなので、
 * リザルトのタイトル以降の数件からパーセントを拾う。
 */
function analyzeResult(titles) {
  const idx = titles.findIndex((t) => /You Win!|You Lose|引き分け/.test(t.text))
  if (idx < 0) return null
  const raw = titles[idx].text
  const win = /You Win!/.test(raw) ? true : /You Lose/.test(raw) ? false : null
  let percents = null
  for (let i = idx; i < Math.min(titles.length, idx + 5); i++) {
    const m = titles[i].text.match(/(\d+)%[^%]*?(\d+)%/)
    if (m) {
      percents = [Number(m[1]), Number(m[2])]
      break
    }
  }
  return { win, percents, text: plainChat(titles[idx].text).slice(0, 120) }
}

/** タイトル/チャットのパケット payload(JSON文字列 or JSON)を素のテキストにする */
function plainChat(payload) {
  const parse = (s) => {
    try {
      return JSON.parse(s)
    } catch {
      return null
    }
  }
  const flatten = (node) => {
    if (node == null) return ''
    if (typeof node === 'string') return parse(node) ? flatten(parse(node)) : node
    if (Array.isArray(node)) return node.map(flatten).join('')
    return flatten(node.text ?? '') + (node.extra ? flatten(node.extra) : '')
  }
  const parsed = typeof payload === 'string' ? (parse(payload) ?? payload) : payload
  return flatten(parsed)
}

function scanLog(lines) {
  const pluginFrames = []
  const fatal = []
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    if (PLUGIN_FRAME.test(line)) {
      // 直前の例外ヘッダを探す
      let header = ''
      for (let j = i; j >= Math.max(0, i - 25); j--) {
        if (/Exception|Error|Caused by/.test(lines[j])) {
          header = lines[j]
          break
        }
      }
      pluginFrames.push({ header, frame: line.trim() })
    }
    if (/Error occurred while enabling Sclat|Could not pass event|Sclat v0\.1 \(Is it up to date/.test(line)) fatal.push(line)
  }
  return { pluginFrames, fatal }
}

/* ------------------------------------------------------------------ *
 * main
 * ------------------------------------------------------------------ */
async function main() {
  if (!opt.noPrepare) await prepare()
  const statusStub = startStatusShareStub(25598)
  const server = await startServer()
  const report = { ok: false, phases: {}, bots: [], errors: [], scores: null }
  let bots = []

  try {
    await waitFor(
      () => server.lines.some((l) => /Done \(.*\)! For help/.test(l)),
      opt.timeoutStart,
      'server ready',
    )
    report.phases.serverReady = Date.now() - t0
    log(`server ready (${(report.phases.serverReady / 1000).toFixed(1)}s)`)

    const enabled = server.lines.find((l) => l.includes('[Sclat] Enabling Sclat'))
    if (!enabled) throw new Error('Sclat が enable されていない')
    if (server.lines.some((l) => /Error occurred while enabling Sclat/.test(l))) {
      throw new Error('Sclat の enable に失敗している (server.log 参照)')
    }

    for (let i = 1; i <= opt.bots; i++) {
      bots.push(createBot(`Player${i}`))
      await sleep(400)
    }
    await waitFor(() => bots.every(({ state }) => state.spawned), 60_000, 'bots spawn')
    log(`${opt.bots} bots spawned`)

    await waitFor(() => bots.every(({ state }) => state.matchStarted), opt.timeoutMatch, 'match start')
    report.phases.matchStarted = Date.now() - t0
    log(`match started (${(report.phases.matchStarted / 1000).toFixed(1)}s)`)

    await waitFor(() => bots.every(({ state }) => state.finished), opt.timeoutMatch, 'match result')
    report.phases.matchFinished = Date.now() - t0
    log(`match finished (${(report.phases.matchFinished / 1000).toFixed(1)}s)`)

    // 試合終了後もサーバーが生きていること
    await sleep(1000)
    if (server.isExited()) throw new Error('試合終了後にサーバープロセスが落ちた')

    // "Finish!" の後、finishMatch のタスクが 46tick 後にリザルトのタイトルを送る
    await waitFor(
      () => bots.every(({ state }) => analyzeResult(state.titles) !== null),
      60_000,
      'result titles (You Win / You Lose)',
    )
    report.phases.resultReceived = Date.now() - t0
    log(`result titles received (${(report.phases.resultReceived / 1000).toFixed(1)}s)`)

    // --- リザルトの内容を検証 ---------------------------------------
    const results = bots.map(({ state }) => ({
      name: state.name,
      result: analyzeResult(state.titles),
      sawModeMessage: state.messages.some((m) => /ナワバリバトル/.test(m.text)),
      sawCountdown: state.messages.some((m) => /試合開始まで後/.test(m.text)),
    }))
    report.scores = results

    for (const r of results) {
      if (!r.sawModeMessage) report.errors.push(`${r.name}: ゲームモードのチャットが届いていない`)
      if (!r.sawCountdown) report.errors.push(`${r.name}: 開始カウントダウンのチャットが届いていない`)
      if (!r.result) {
        report.errors.push(`${r.name}: リザルトのタイトル(Win/Lose/引き分け)が届いていない`)
        continue
      }
      const p = r.result.percents
      if (!p) report.errors.push(`${r.name}: リザルトのスコア(%)が読めない`)
      else if (p[0] + p[1] !== 100) report.errors.push(`${r.name}: リザルトのスコアが100%にならない (${p.join('% / ')}%)`)
    }
    const winners = results.filter((r) => r.result?.win === true).length
    const losers = results.filter((r) => r.result?.win === false).length
    const draws = results.filter((r) => r.result && r.result.win === null).length
    if (draws === 0 && (winners === 0 || losers === 0)) {
      report.errors.push(`勝敗が矛盾している (win=${winners}, lose=${losers})`)
    }
    // 同じ試合を見ているので、全ボットのスコア表示は一致するはず
    const pcts = results.map((r) => r.result?.percents).filter(Boolean)
    if (pcts.length > 1 && pcts.some((p) => p[0] !== pcts[0][0] || p[1] !== pcts[0][1])) {
      report.errors.push(`ボット間でリザルトのスコアが食い違う: ${JSON.stringify(pcts)}`)
    }
    log(`results: ${JSON.stringify(results.map((r) => ({ name: r.name, win: r.result?.win, p: r.result?.percents })))}`)
  } catch (e) {
    report.errors.push(e.message)
    log(`FAILED: ${e.message}`)
  } finally {
    for (const { bot } of bots) {
      try {
        bot.quit()
      } catch {
        /* ignore */
      }
    }
    await sleep(1000)
    await stopServer(server)

    const { pluginFrames, fatal } = scanLog(server.lines)
    report.pluginExceptions = pluginFrames.slice(0, 10)
    report.fatalLogLines = fatal
    report.bots = bots.map(({ state }) => ({
      name: state.name,
      spawned: state.spawned,
      matchStarted: state.matchStarted,
      finished: state.finished,
      result: analyzeResult(state.titles),
      died: state.died,
      kicked: state.kicked,
      errors: state.errors,
      messageCount: state.messages.length,
      // 失敗の調査に要る分だけ残す(全件だと report.json が肥大する)
      messages: state.messages.filter((m) => /Sclat|試合|残り|勝|負/.test(m.text)).slice(-60),
      titles: state.titles
        .filter((t) => /Finish|Win|Lose|引き分け|READY|GO!|%|ナワバリ|GO/.test(t.text))
        .slice(-40),
      titleCount: state.titles.length,
    }))
    try {
      statusStub.close()
    } catch {
      /* ignore */
    }

    if (pluginFrames.length) report.errors.push(`プラグイン由来のスタックトレース ${pluginFrames.length} 件`)
    if (fatal.length) report.errors.push(`致命的ログ ${fatal.length} 件: ${fatal[0]}`)
    if (!report.phases.matchFinished) report.errors.push('試合が終了しなかった')

    report.ok = report.errors.length === 0
    report.durationS = (Date.now() - t0) / 1000

    const out = join(runRoot, 'report.json')
    await writeFile(out, JSON.stringify(report, null, 2))
    log(`report: ${out}`)
    console.log(`\n${report.ok ? 'PASS' : 'FAIL'} — ${report.errors.length} error(s), ${report.durationS.toFixed(1)}s`)
    if (!report.ok) for (const e of report.errors) console.log(`  - ${e}`)
    if (report.bots.length) console.log(`  scores: ${JSON.stringify(report.scores)}`)
  }

  process.exit(report.ok ? 0 : 1)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
