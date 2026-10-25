# e2e — シナリオテスト(試合を1つまるごと実行する)

実際の Spigot 1.14.4 サーバーに Sclat と依存プラグインを入れ、**mineflayer のボット2体で
「参加 → 30秒カウントダウン → イントロ → 試合(ナワバリ) → リザルト」を1試合通す**テスト。

ユニットテストでは絶対に踏めない経路(`MatchMgr.matchRunnable` の毎tick処理、ブロック塗り、
スコアボード、リザルト演出、試合後の片付け)をまとめて検証できる。

```bash
./gradlew scenarioTest          # = shadowJar + e2e/run.sh
bash e2e/run.sh --verbose       # サーバーログを全部出す
node e2e/scenario/run-match.mjs --keep --no-prepare   # 直実行(準備済みなら)
```

実行結果は `e2e/.run/report.json`(判定と各ボットの受信内容)と `e2e/.run/server.log`(全ログ)。
PASS/FAIL は終了コード(0/1)。

## 仕組み

| 段階 | 内容 |
|---|---|
| 準備 | `e2e/scenario/prepare.mjs` … 依存プラグイン/slf4j を `e2e/.cache` に取得(初回のみ)、`e2e/settings` を `e2e/.run/server` に展開、`build/libs/Sclat.jar` を配置、`world/` を毎回削除 |
| 起動 | Spigot 1.14.4 を Java 11 で起動し `Done (…)!` を待つ |
| 接続 | `e2e/settings/plugins/Sclat/config.yml` が `ServerType: MATCH` なので、ボットはログインするだけで `MatchMgr.playerJoinMatch` される(GUI操作不要) |
| 試合 | ボットは中央を向いて右クリック(射撃)し続ける。`StartPlayerCount: 2` に達するとカウントダウン開始 |
| 判定 | リザルトのタイトル(`You Win!` / `You Lose...` / `引き分け！`)とサブタイトルのスコア(%)を各ボットが受信した内容で検証 |

## 検証していること

- Sclat が enable で落ちない(`Error occurred while enabling Sclat` を検出)
- 全ボットが「試合開始まで後◯秒」「ゲームモード : ナワバリバトル」を受信
- 全ボットが「試合が開始されました」を受信 → `matchStarted`
- 試合が制限時間で終了し、`Finish!` → リザルトのタイトルまで到達(`matchFinished` → `resultReceived`)
- リザルトのスコアが 2値で合計100%、ボット間で一致
- 勝敗が矛盾しない(引き分けでなければ勝者と敗者が両方いる)
- **プラグイン由来のスタックトレースが1件も出ていない**(`at be4rjp.sclat.…` を検出)
- 試合終了後もサーバープロセスが生存

実測(このホスト): サーバー起動 約42s + 試合 約70s = 約110s。スコアは 53%/47% のように
偏る = 塗り(射撃)が実際に動いていることの証拠。

## 前提

- **Java 11**(Spigot 1.14.4 の上限)。`SCLAT_JAVA` か mise の temurin-11 を自動で使う
- **Java 21**(Gradle 用。`SCLAT_JAVA21` か mise の temurin-21)
- **Node 20+**(mineflayer)。`e2e/scenario` で `npm install` が必要(初回は run.sh が実行)
- **Spigot 1.14.4**(BuildTools 生成物。再配布できないのでスクリプトはDLしない)
  - 探索順: `$SCLAT_SPIGOT_JAR` → `~/.m2/repository/org/spigotmc/spigot/1.14.4-R0.1-SNAPSHOT/…jar` → `~/buildtools/spigot-1.14.4.jar`
  - 無い場合: `java -jar BuildTools.jar --rev 1.14.4 --nogui`

## ファイル構成

```
e2e/
├── run.sh                     # 入口(ビルド + npm install + シナリオ実行)
├── scenario/
│   ├── prepare.mjs            # サーバーディレクトリ組み立て
│   ├── run-match.mjs          # 起動・ボット・判定
│   └── package.json           # mineflayer
├── settings/                  # テストサーバー設定テンプレート(毎回コピーされる)
│   ├── server.properties      # level-type=FLAT / online-mode=false / port 25599
│   ├── bukkit.yml
│   ├── eula.txt
│   └── plugins/Sclat/         # config.yml, maps.yml, class.yml, mainnweapon.yml, …
└── .run/, .cache/             # 生成物(gitignore)
```

## 設定の勘所

- `settings/plugins/Sclat/config.yml`
  - `ServerType: MATCH` … ログインで自動参加させるための設定。`LOBBY` にすると GUI 操作が要る
  - `MatchTime.Default: 20` … 試合時間。長くしたいときはここ
  - `StartPlayerCount: 2` … ボット数を増やすならここも合わせる
- `settings/plugins/Sclat/maps.yml` … フラットワールド `world` の地面(y=4)を前提にした座標。
  `bukkit.yml` の `worlds.<name>.generator` はプラグイン名しか受け付けないため、
  追加ワールドをフラットにできない。なので「既定ワールド(`level-type=FLAT`)をロビーとマップで使い回す」構成にしている。
- ボットの操作は `run-match.mjs` の `startPlaying()`。射撃(`activateItem`)と前進だけ。

## 既知の制約

- 2体1試合(1v1)のみ。`--bots 4` は不可(`StartPlayerCount` を超えた分は「既に開始しているため参加できません」)
- 検証するのは Normal(ナワバリ)モードのみ。TDM / TDMLong / Area は `config.yml` の `WorkMode` を変えれば同じ枠組みで通せるが、現状のアサーションは勝敗とスコアだけなので追加検証は未実装
- CI(GitHub Actions)には入れていない。回すなら BuildTools と node が必要
- Spigot 1.14.4 は slf4j を同梱しないため、`slf4j-api` / `slf4j-jdk14` を JVM クラスパスに足している
  (足さないと Sclat のログが NOP になり、失敗理由が読めない)
- `StatusShare` の待ち受け(25598)はテスト側で捨てサーバーを立てて塞いでいる。無いと `StatusClient` が
  `ConnectException` を WARN で吐き続ける
