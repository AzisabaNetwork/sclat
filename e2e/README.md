# e2e — シナリオテスト(試合を1つまるごと実行する)

実際の Spigot 1.14.4 サーバーに Sclat と依存プラグインを入れ、**mineflayer のボット2体で
「参加 → 30秒カウントダウン → イントロ → 試合 → リザルト」を1試合通す**テスト。
`WorkMode` を差し替えて **Normal / TDM / TDMLong / Area** の4モードを回せる。

ユニットテストでは絶対に踏めない経路(`MatchMgr.matchRunnable` の毎tick処理、ブロック塗り、
スコアボード、リザルト演出、試合後の片付け)をまとめて検証できる。

```bash
./gradlew scenarioTest                      # Normal を1本
./gradlew scenarioTest -PworkMode=TDM       # モード指定
bash e2e/run.sh --work-mode Area            # スクリプト直
bash e2e/run.sh --work-mode TDM --verbose   # サーバーログを全部出す
node e2e/scenario/run-match.mjs --keep --no-prepare   # 直実行(準備済みなら)
```

実行結果は `e2e/.run/report.json`(判定と各ボットの受信内容)と `e2e/.run/server.log`(全ログ)。
PASS/FAIL は終了コード(0/1)。

## 仕組み

| 段階 | 内容 |
|---|---|
| 準備 | `e2e/scenario/prepare.mjs` … 依存プラグイン/slf4j を `e2e/.cache` に取得(初回のみ)、`e2e/settings` を `e2e/.run/server` に展開、`build/libs/Sclat.jar` を配置、`world/` を毎回削除。モードごとに `config.yml`/`maps.yml`/`mainnweapon.yml` を微調整 |
| 起動 | Spigot 1.14.4 を Java 11 で起動し `Done (…)!` を待つ |
| 接続 | `ServerType: MATCH` なので、ボットはログインするだけで `MatchMgr.playerJoinMatch` される(GUI操作不要) |
| 試合 | モードに応じてボットが射撃/旋回。`StartPlayerCount: 2` に達するとカウントダウン開始 |
| 判定 | リザルトのタイトル(`You Win!` / `You Lose...` / `Knock Out !!` / `引き分け！`)と、モードごとの数値(面積% or キル数)を各ボットの受信内容で検証 |

## モードごとの違い

| モード | 試合時間 | ボットの動き | リザルトの数値 |
|---|---|---|---|
| Normal | 20s | 中央を向いて塗り + 前進 | 面積 % (`53% / 47%`) |
| TDM | 20s | 相手を狙って撃つ(スポーン間隔 8 ブロック / Damage 20) | `BlueTeam : N Kill` |
| TDMLong | 30s (`MatchTime.TDMLong`) | 同上 | 同上 |
| Area | 120s | 1体だけが 3x3 エリアに入って旋回しながら塗る | 面積 % / KO 時は `Knock Out !!` |

Area を 120s にしているのは、ガチエリアのカウント(`gatiCount`)が **1/秒** で進み、
100 到達で Knock Out になるため。20s だと必ず時間切れの引き分けになる。

## 検証していること

- Sclat が enable で落ちない(`Error occurred while enabling Sclat` を検出)
- 全ボットが「試合開始まで後◯秒」とモード告知(ナワバリ / チームデスマッチ / ガチエリア)を受信
- 全ボットが「試合が開始されました」を受信 → `matchStarted`
- 試合が制限時間で終了し、`Finish!` → リザルトのタイトルまで到達(`matchFinished` → `resultReceived`)
- 勝敗がついた試合はスコア(%)が2値で合計100%、ボット間で一致(TDM はキル数が一致)
- 勝敗と数値が矛盾しない(引き分けでなければ数値が同点でない / 全員引き分けなら同点)
- **プラグイン由来のスタックトレースが1件も出ていない**(`at be4rjp.sclat.…` を検出)
- 試合終了後もサーバープロセスが生存

実測(このホスト): Normal 約110s、TDM 約110s、TDMLong 約120s、Area 約225s(サーバー起動 40〜50s 込み)。

## CI

`.github/workflows/e2e.yml` が 4 モードを matrix で**並列**に回す。

- `spigot` ジョブ: BuildTools で Spigot 1.14.4 を1回だけビルドし `~/.m2` をキャッシュ
- `e2e` ジョブ(4並列): そのキャッシュを復元 → pnpm で mineflayer を install → `bash e2e/run.sh --work-mode <mode>`
- 失敗時も `report.json` と `server.log` を artifact に上げる

## 前提

- **Java 11**(Spigot 1.14.4 の上限)。`SCLAT_JAVA` か mise の temurin-11 を自動で使う
- **Java 21**(Gradle 用。`SCLAT_JAVA21` か mise の temurin-21。CI は 11 で通るので不要)
- **Node 20+ / pnpm**(mineflayer)。`e2e/scenario` で `pnpm install`(初回は run.sh が実行)
- **Spigot 1.14.4**(BuildTools 生成物。再配布できないのでスクリプトはDLしない)
  - 探索順: `$SCLAT_SPIGOT_JAR` → `~/.m2/repository/org/spigotmc/spigot/1.14.4-R0.1-SNAPSHOT/…jar` → `~/buildtools/spigot-1.14.4.jar`
  - 無い場合: `java -jar BuildTools.jar --rev 1.14.4 --nogui`

## ファイル構成

```
e2e/
├── run.sh                     # 入口(ビルド + pnpm install + シナリオ実行)
├── scenario/
│   ├── prepare.mjs            # サーバーディレクトリ組み立て + モード別調整
│   ├── run-match.mjs          # 起動・ボット・判定
│   ├── package.json           # mineflayer
│   └── pnpm-lock.yaml
├── settings/                  # テストサーバー設定テンプレート(毎回コピーされる)
│   ├── server.properties      # level-type=FLAT / online-mode=false / port 25599
│   ├── bukkit.yml
│   ├── eula.txt
│   └── plugins/Sclat/         # config.yml, maps.yml, class.yml, mainnweapon.yml, BGM/test.nbs …
└── .run/, .cache/             # 生成物(gitignore)
```

## 設定の勘所

- `settings/plugins/Sclat/config.yml`
  - `ServerType: MATCH` … ログインで自動参加させるための設定。`LOBBY` にすると GUI 操作が要る
  - `MatchTime.Default: 20` … 試合時間(Area は prepare が 120 に書き換える)
  - `StartPlayerCount: 2` … ボット数を増やすならここも合わせる
  - `nBGM` / `fBGM` / `Hologram` … 空にすると試合ループやリザルトが落ちるので必ず値を入れる(下記)
- `settings/plugins/Sclat/maps.yml` … フラットワールド `world` の地面(y=4)を前提にした座標。
  `bukkit.yml` の `worlds.<name>.generator` はプラグイン名しか受け付けないため、
  追加ワールドをフラットにできない。なので「既定ワールド(`level-type=FLAT`)をロビーとマップで使い回す」構成にしている。
  `Area` ブロックは Area 以外のモードでは prepare が削除する(未使用の Area は unload 時に NPE するため)。
- ボットの操作は `run-match.mjs` の `startPlaying()`。

## ハマりどころ(実際に踏んだもの)

| 症状 | 原因 / 対処 |
|---|---|
| Sclat が enable で即死 | Spigot 1.14.4 は slf4j 非同梱。`slf4j-api` + `slf4j-jdk14` を JVM クラスパスに追加 |
| Sclat のログが出ない | slf4j の binding が無いと NOP logger になる。`slf4j-jdk14` で解決 |
| `NullPointerException: NoteBlockAPIMgr.loadSongFiles` | `config.yml` に `nBGM`/`fBGM` が必須 |
| 試合開始直後に何も起きなくなる | `nBGM` が空だと `randomNormalSong` の `nextInt(0)` で試合ループが落ちる(→ 無音の `BGM/test.nbs` を同梱) |
| `finishMatch` で NPE(i=157 付近) | `Hologram.*` が未設定だと `sendHologram` が落ちる(ServerType 問わず呼ばれる) |
| `Area.stop()` が `IllegalStateException: Not scheduled yet` | `area.start()` 前に試合が終わると発生。上記 BGM 落ちで `start()` に到達しないのが引き金だった |
| `StatusClient` の `ConnectException` が延々と出る | MATCH サーバーは StatusShare に接続する。テスト側で 25598 に捨てサーバーを立てて塞ぐ |

## 既知の制約

- 2体1試合(1v1)のみ。`--bots 4` は不可(`StartPlayerCount` を超えた分は「既に開始しているため参加できません」)
- TDM は現状 **0 Kill の引き分け** に落ち着く(射撃は当たっている想定だが、ダメージ経路
  `SnowballListener` のプレイヤー命中までは至っていない)。キル数の集計・勝敗判定は
  リザルトの整合性チェックで検証しているが、キルそのものは未検証
- Area は片チームのエリア確保(Knock Out)に到達しないことがある。到達しなかった場合は
  時間切れの引き分けとして PASS する(KO の有無でテストを落とさない)
- Spigot 1.14.4 は slf4j を同梱しないため、`slf4j-api` / `slf4j-jdk14` を JVM クラスパスに足している
