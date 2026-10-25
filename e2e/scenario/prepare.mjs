#!/usr/bin/env node
/**
 * e2e シナリオテスト: 実行ディレクトリの組み立て。
 *
 * - 依存プラグイン jar / slf4j を e2e/.cache/downloads にキャッシュ(初回のみDL)
 * - Spigot 1.14.4 は BuildTools 生成物を流用する(再配布不可のためDLしない)
 * - e2e/settings の設定テンプレートを e2e/.run/server にコピー
 * - build/libs/Sclat.jar を plugins/ に配置
 */
import { createHash } from 'node:crypto'
import { cp, mkdir, readFile, readdir, rm, stat, writeFile } from 'node:fs/promises'
import { existsSync } from 'node:fs'
import { homedir } from 'node:os'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
export const repoRoot = resolve(__dirname, '..', '..')
export const runRoot = join(repoRoot, 'e2e', '.run')
export const serverDir = join(runRoot, 'server')
const cacheDir = join(repoRoot, 'e2e', '.cache', 'downloads')

/** 依存jar(バージョン固定) */
const DOWNLOADS = [
  {
    file: 'ProtocolLib-4.5.0.jar',
    // 1.14.4 対応の最後の系列
    url: 'https://github.com/dmulloy2/ProtocolLib/releases/download/4.5.0/ProtocolLib.jar',
  },
  {
    file: 'NoteBlockAPI-1.6.3.jar',
    url: 'https://jitpack.io/com/github/koca2000/NoteBlockAPI/1.6.3/NoteBlockAPI-1.6.3.jar',
  },
  {
    file: 'BlockStudio-1.0.4.jar',
    url: 'https://jitpack.io/com/github/Be4rJP/BlockStudio/master-SNAPSHOT/BlockStudio-master-SNAPSHOT.jar',
  },
  {
    file: 'DADADAChecker.zip',
    url: 'https://github.com/bea4dev/DADADAChecker/releases/download/1.0.0/DADADAChecker.zip',
  },
  {
    file: 'WorldChef.zip',
    url: 'https://github.com/bea4dev/WorldChef/releases/download/1.0.3/WorldChef.zip',
  },
  {
    // Spigot 1.14.4 には slf4j が同梱されない。Sclat は slf4j を使うので
    // JVM のクラスパスに足す(プラグインのクラスローダの親に入る)。
    file: 'slf4j-api.jar',
    url: 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar',
  },
  {
    // slf4j の実装。無いと Sclat のログが NOP になり、失敗の原因が読めない。
    file: 'slf4j-jdk14.jar',
    url: 'https://repo1.maven.org/maven2/org/slf4j/slf4j-jdk14/1.7.25/slf4j-jdk14-1.7.25.jar',
  },
]

function log(msg) {
  console.log(`[prepare] ${msg}`)
}

async function exists(p) {
  try {
    await stat(p)
    return true
  } catch {
    return false
  }
}

async function download(entry) {
  const dest = join(cacheDir, entry.file)
  if (await exists(dest)) return dest
  log(`download ${entry.file}`)
  const res = await fetch(entry.url, { redirect: 'follow' })
  if (!res.ok) throw new Error(`download failed ${entry.url}: HTTP ${res.status}`)
  const buf = Buffer.from(await res.arrayBuffer())
  await writeFile(dest, buf)
  log(`  ${entry.file} ${buf.length} bytes sha1=${createHash('sha1').update(buf).digest('hex').slice(0, 12)}`)
  return dest
}

/** BuildTools が生成した Spigot 1.14.4 の jar を探す */
async function resolveSpigotJar() {
  const candidates = [
    process.env.SCLAT_SPIGOT_JAR,
    join(
      homedir(),
      '.m2/repository/org/spigotmc/spigot/1.14.4-R0.1-SNAPSHOT/spigot-1.14.4-R0.1-SNAPSHOT.jar',
    ),
    join(homedir(), 'buildtools/spigot-1.14.4.jar'),
  ].filter(Boolean)
  for (const c of candidates) {
    if (await exists(c)) return c
  }
  throw new Error(
    'Spigot 1.14.4 が見つかりません。BuildTools でビルドするか、SCLAT_SPIGOT_JAR で jar を指定してください。\n' +
      '  java -jar BuildTools.jar --rev 1.14.4 --nogui',
  )
}

/** zip の中の jar(リリースが zip 配布のプラグイン)を取り出す */
async function extractJarFromZip(zipPath, outDir) {
  const { execFile } = await import('node:child_process')
  const { promisify } = await import('node:util')
  const run = promisify(execFile)
  // python3 は標準で入っている前提(unzip が無い環境があるため)
  const script = `import zipfile,sys,os
z=zipfile.ZipFile(sys.argv[1])
for n in z.namelist():
    if n.endswith('.jar'):
        open(os.path.join(sys.argv[2], os.path.basename(n)),'wb').write(z.read(n))
        print(os.path.basename(n))
`
  const { stdout } = await run('python3', ['-c', script, zipPath, outDir])
  return stdout
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean)
}

export const WORK_MODES = ['Normal', 'TDM', 'TDMLong', 'Area']

export async function prepare({ freshWorld = true, workMode = 'Normal' } = {}) {
  if (!WORK_MODES.includes(workMode)) {
    throw new Error(`unknown work mode: ${workMode} (${WORK_MODES.join('|')})`)
  }
  await mkdir(cacheDir, { recursive: true })
  await mkdir(serverDir, { recursive: true })

  for (const entry of DOWNLOADS) await download(entry)

  const jarPath = await resolveSpigotJar()
  log(`spigot: ${jarPath}`)
  await cp(jarPath, join(serverDir, 'spigot.jar'))

  // 設定テンプレートを毎回上書きコピー(テストの再現性のため)
  await cp(join(repoRoot, 'e2e', 'settings'), serverDir, { recursive: true })

  // WorkMode だけはシナリオごとに差し替える(テンプレートは Normal で固定)
  const configPath = join(serverDir, 'plugins', 'Sclat', 'config.yml')
  const config = await readFile(configPath, 'utf8')
  await writeFile(configPath, config.replace(/^WorkMode:.*$/m, `WorkMode: ${workMode}`))
  log(`work mode: ${workMode}`)

  // Area 以外のモードでは Area 定義を落とす。
  // Area が定義されているとマップに Area オブジェクトが作られ、未使用のまま
  // ワールド unload 時に Area.stop() が NPE する(プラグイン側の警告ノイズ)。
  if (workMode !== 'Area') {
    const mapPath = join(serverDir, 'plugins', 'Sclat', 'maps.yml')
    const map = await readFile(mapPath, 'utf8')
    const marker = map.indexOf('\n    # Area(ガチエリア)モード用')
    if (marker >= 0) await writeFile(mapPath, map.slice(0, marker) + '\n')
  }

  // TDM 系はキルが発生するように調整する。
  // - ダメージ/弾速を上げる(テンプレートのままだと当たっても削れない)
  // - スポーン間隔を詰める(16 ブロック先の相手には当たりにくい)
  if (workMode === 'TDM' || workMode === 'TDMLong') {
    const weaponPath = join(serverDir, 'plugins', 'Sclat', 'mainnweapon.yml')
    const weapon = await readFile(weaponPath, 'utf8')
    await writeFile(
      weaponPath,
      weapon
        .replace(/^ {4}Damage:.*$/m, '    Damage: 20.0')
        .replace(/^ {4}ShootRandom:.*$/m, '    ShootRandom: 0.5')
        .replace(/^ {4}ShootSpeed:.*$/m, '    ShootSpeed: 3.0'),
    )
    const mapPath = join(serverDir, 'plugins', 'Sclat', 'maps.yml')
    const mapYml = await readFile(mapPath, 'utf8')
    await writeFile(
      mapPath,
      mapYml
        // Team0 は x=-8 / Team1 は x=+8 なので、TDM では ±4 に寄せる
        .replace(/(    Team0:\n      X: )-8/m, '$1-4')
        .replace(/(    Team1:\n      X: )8/m, '$14'),
    )
    log('map/weapon tuned for TDM (spawn 8 blocks apart, Damage 20 / ShootSpeed 3.0)')
  }

  // Area はガチエリアのカウント(gatiCount)が 100 になると Knock Out で終わる。
  // カウントは 1/秒 なので、KO まで到達させるには試合時間を長めにする必要がある。
  if (workMode === 'Area') {
    const configPath2 = join(serverDir, 'plugins', 'Sclat', 'config.yml')
    const cfg2 = await readFile(configPath2, 'utf8')
    await writeFile(configPath2, cfg2.replace(/^( {2}Default: )\d+$/m, (_m, p) => `${p}120`))
    log('match time for Area set to 120s (gatiCount reaches 100)')
  }

  const pluginsDir = join(serverDir, 'plugins')
  await mkdir(pluginsDir, { recursive: true })

  for (const f of ['ProtocolLib-4.5.0.jar', 'NoteBlockAPI-1.6.3.jar', 'BlockStudio-1.0.4.jar']) {
    await cp(join(cacheDir, f), join(pluginsDir, f))
  }
  for (const z of ['DADADAChecker.zip', 'WorldChef.zip']) {
    const names = await extractJarFromZip(join(cacheDir, z), pluginsDir)
    log(`extracted ${names.join(', ')}`)
  }

  const sclatJar = join(repoRoot, 'build', 'libs', 'Sclat.jar')
  if (!existsSync(sclatJar)) throw new Error(`Sclat.jar がありません: ${sclatJar} (./gradlew shadowJar)`)
  await cp(sclatJar, join(pluginsDir, 'Sclat.jar'))
  for (const f of ['slf4j-api.jar', 'slf4j-jdk14.jar']) {
    await cp(join(cacheDir, f), join(serverDir, f))
  }

  // ワールドを作り直して、前回の塗り跡やエンティティを持ち越さない
  if (freshWorld) {
    const world = join(serverDir, 'world')
    if (await exists(world)) await rm(world, { recursive: true, force: true })
  }

  const jars = (await readdir(pluginsDir)).filter((f) => f.endsWith('.jar')).sort()
  log(`server dir: ${serverDir}`)
  log(`plugins: ${jars.join(', ')}`)
  return { serverDir, plugins: jars }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  prepare().catch((e) => {
    console.error(e)
    process.exit(1)
  })
}
