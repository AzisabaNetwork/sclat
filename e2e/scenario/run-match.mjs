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
  workMode: getArg('--work-mode') ?? 'Normal',
}

const isTdm = () => opt.workMode === 'TDM' || opt.workMode === 'TDMLong'
const usesPercentScore = () => !isTdm() // Normal / Area は塗り面積(%)

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
// Normal/Area は "You Win!" / "Knock Out !!"、TDM 系は "You  Win!"(スペース2つ)
const RESULT_PATTERNS = [/You\s+Win!/, /You\s+Lose/, /Knock Out !!?/, /引き分け/, /Finish!/]

function createBot(name, index) {
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
    hits: 0,
    minHealth: null,
    lastHealth: null,
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
    startPlaying(bot, state, index)
  })
  bot.on('death', () => {
    state.died++
    log(`${name}: died (${state.died})`)
  })
  bot.on('health', () => {
    // 被弾しているかどうかの診断用
    if (state.minHealth === null || bot.health < state.minHealth) state.minHealth = bot.health
    if (state.lastHealth !== null && bot.health < state.lastHealth) state.hits++
    state.lastHealth = bot.health
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

/**
 * 試合中のボットの操作。
 * - Normal: 中央を向いて足元を撃つ(=塗り)。少し歩いて塗り範囲を広げる
 * - Area  : 1体だけがエリア中央に入って塗り続ける(片チームがエリアを確保する状況を作る)。
 *           もう1体は何もしない(両者が塗ると確保判定にならず必ず引き分けになる)
 * - TDM / TDMLong: 相手プレイヤーを狙って撃つ(=キル)
 */
function startPlaying(bot, state, index) {
  const idle = opt.workMode === 'Area' && index !== 0
  const interval = isTdm() ? 250 : 400
  const timer = setInterval(() => {
    if (!bot.entity || idle) return
    try {
      if (isTdm()) {
        const target = bot.nearestEntity((e) => e.type === 'player' && e.username !== bot.username)
        if (target) {
          bot.lookAt(target.position.offset(0, 1.2, 0), true)
        } else {
          bot.look(0, 0, true)
        }
      } else {
        // 中央(0,0)方向を向き、足元を狙う
        const dx = 0 - bot.entity.position.x
        const dz = 0 - bot.entity.position.z
        const yaw = Math.atan2(-dx, dz) // mineflayer: yaw=0 は +z
        bot.look(yaw, 0.9, true)
        // Area はエリア内(半径1)まで歩いて入る
        if (opt.workMode === 'Area' && Math.hypot(dx, dz) > 1) {
          bot.setControlState('forward', true)
          return
        }
        if (opt.workMode === 'Area') {
          // エリア全体(3x3)を塗るため、その場で旋回しながら撃ち続ける
          state.areaYaw = (state.areaYaw ?? 0) + 0.7
          bot.look(state.areaYaw, 1.0, true)
        }
      }
      bot.activateItem()
      if (opt.workMode === 'Normal' && state.matchStarted && Math.random() < 0.3) {
        bot.setControlState('forward', true)
        setTimeout(() => bot.setControlState('forward', false), 400)
      }
    } catch (e) {
      /* 無視: サーバー切断中など */
    }
  }, interval)
  bot.once('end', () => {
    clearInterval(timer)
    try {
      bot.setControlState('forward', false)
    } catch {
      /* ignore */
    }
  })
}

/* ------------------------------------------------------------------ *
 * 検証
 * ------------------------------------------------------------------ */
const PLUGIN_FRAME = /\bat (be4rjp\.sclat\.[\w.$]+)\(/

/**
 * リザルトのタイトル群から勝敗とスコアを取り出す。
 *
 * モードごとに Animation.xxxResultAnimation の文言が違う:
 *   Normal : "You Win!" / "You Lose..." / "引き分け！"      + "45% [ゲージ] 55%"
 *   Area   : "Knock Out !!" / "You Lose..."                  + "45% [ゲージ] 55%"
 *   TDM系  : "You  Win!"(スペース2つ) / "You  Lose..." / "引き分け！" + "BlueTeam : 3 Kill   OrangeTeam : 1 Kill"
 *
 * タイトル(action=0)とサブタイトル(action=1)は別パケットなので、
 * リザルトのタイトル以降の数件から数値を拾う。
 * TDM はアニメーション前半にランダムな Kill 数が出るため、必ずリザルトの後だけを見る。
 */
function analyzeResult(titles) {
  const idx = titles.findIndex((t) => /You\s+Win!|You\s+Lose|Knock Out !!?|引き分け/.test(t.text))
  if (idx < 0) return null
  const raw = titles[idx].text
  const win = /You\s+Win!|Knock Out !!?/.test(raw) ? true : /You\s+Lose/.test(raw) ? false : null

  let percents = null
  let kills = null
  for (let i = idx; i < Math.min(titles.length, idx + 5); i++) {
    const text = titles[i].text
    if (!percents) {
      const m = text.match(/(\d+)%[^%]*?(\d+)%/)
      if (m) percents = [Number(m[1]), Number(m[2])]
    }
    if (!kills) {
      const m = [...text.matchAll(/:\s*(\d+)\s*Kill/g)].map((x) => Number(x[1]))
      if (m.length >= 2) kills = [m[0], m[1]]
    }
  }
  return { win, percents, kills, text: plainChat(titles[idx].text).slice(0, 120) }
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
  if (!opt.noPrepare) await prepare({ workMode: opt.workMode })
  const statusStub = startStatusShareStub(25598)
  const server = await startServer()
  const report = { ok: false, mode: opt.workMode, phases: {}, bots: [], errors: [], scores: null }
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
      bots.push(createBot(`Player${i}`, i - 1))
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
    // モード告知の文言(Sclat の matchRunnable が出す)
    const modeMessage = isTdm()
      ? opt.workMode === 'TDMLong'
        ? /チームデスマッチ\(ロング\)/
        : /チームデスマッチ/
      : opt.workMode === 'Area'
        ? /ガチエリア/
        : /ナワバリバトル/

    const results = bots.map(({ state }) => ({
      name: state.name,
      result: analyzeResult(state.titles),
      sawModeMessage: state.messages.some((m) => modeMessage.test(m.text)),
      sawCountdown: state.messages.some((m) => /試合開始まで後/.test(m.text)),
    }))
    report.scores = results

    for (const r of results) {
      if (!r.sawModeMessage) report.errors.push(`${r.name}: ${opt.workMode} のモード告知チャットが届いていない`)
      if (!r.sawCountdown) report.errors.push(`${r.name}: 開始カウントダウンのチャットが届いていない`)
      if (!r.result) {
        report.errors.push(`${r.name}: リザルトのタイトル(Win/Lose/引き分け)が届いていない`)
        continue
      }
      if (usesPercentScore() && r.result.win !== null) {
        // 引き分け(Normal/Area の @35 分岐)はサブタイトルに % を出さないので、
        // 勝敗がついたときだけスコアを要求する
        const p = r.result.percents
        if (!p) report.errors.push(`${r.name}: リザルトのスコア(%)が読めない`)
        else if (p[0] + p[1] !== 100) report.errors.push(`${r.name}: リザルトのスコアが100%にならない (${p.join('% / ')}%)`)
      } else if (isTdm()) {
        const k = r.result.kills
        if (!k) report.errors.push(`${r.name}: リザルトのキル数が読めない`)
      }
    }
    const winners = results.filter((r) => r.result?.win === true).length
    const losers = results.filter((r) => r.result?.win === false).length
    const draws = results.filter((r) => r.result && r.result.win === null).length
    if (draws === 0 && (winners === 0 || losers === 0)) {
      report.errors.push(`勝敗が矛盾している (win=${winners}, lose=${losers})`)
    }
    // 同じ試合を見ているので、全ボットの表示は一致するはず
    const shown = results.map((r) => r.result?.percents ?? r.result?.kills).filter(Boolean)
    if (shown.length > 1 && shown.some((v) => v[0] !== shown[0][0] || v[1] !== shown[0][1])) {
      report.errors.push(`ボット間でリザルトの数値が食い違う: ${JSON.stringify(shown)}`)
    }
    // 引き分けでないなら、勝敗と数値が対応していること(TDM はキル数、Normal/Area は面積)
    if (draws === 0 && shown.length && results[0].result) {
      const [a, b] = shown[0]
      if (a === b) report.errors.push(`引き分けでないのに数値が同点 (${a} / ${b})`)
    }
    // 逆に、全員引き分けなら数値も同点であるべき
    if (draws === results.length && shown.length && shown[0][0] !== shown[0][1]) {
      report.errors.push(`引き分けなのに数値が同点でない (${shown[0].join(' / ')})`)
    }
    log(
      `results(${opt.workMode}): ${JSON.stringify(
        results.map((r) => ({ name: r.name, win: r.result?.win, p: r.result?.percents, kills: r.result?.kills })),
      )}`,
    )
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
      hits: state.hits,
      minHealth: state.minHealth,
      kicked: state.kicked,
      errors: state.errors,
      messageCount: state.messages.length,
      // 失敗の調査に要る分だけ残す(全件だと report.json が肥大する)
      messages: state.messages.filter((m) => /Sclat|試合|残り|勝|負/.test(m.text)).slice(-60),
      titles: state.titles
        .filter((t) => /Finish|Win|Lose|Knock Out|Kill|引き分け|READY|GO!|%|ナワバリ|デスマッチ|ガチエリア/.test(t.text))
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
