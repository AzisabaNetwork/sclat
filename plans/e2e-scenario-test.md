# e2e シナリオテスト(試合を1つまるごと実行する)

## 目的

「試合が最後まで破綻せずに回る」ことを自動で確認できるようにする。
`MatchMgr.matchRunnable` は毎tick Bukkit API(ブロック・エンティティ・パケット・スコアボード・サウンド)を
叩くため、ユニットテストでは実質検証できない。実サーバー上で1試合通すのが最短で確実。

## 方式の比較

| 方式 | 判定 |
|---|---|
| MockBukkit / Bukkit をモックした in-JVM テスト | 1試合の間に呼ばれる Bukkit API が多すぎてモック surface が非現実的。新規の Bukkit 呼び出しのたびに壊れる |
| 実サーバー + 偽プレイヤー(NMS で ServerPlayer を生成) | 1.14.4 の NMS に密結合。バージョン更新で壊れ、クライアントパケット経路も通らない |
| **実サーバー + 実クライアント(mineflayer)** | **採用**。プロトコル経路・描画以外のすべてが本番同等。ボットは `npm i mineflayer` だけで用意できる |
| 実サーバー + 手動操作 | 自動化できないので回帰検出に使えない |

mineflayer は 1.14.4(protocol 498)に対応している。

## 構成

```
e2e/
├── run.sh                  # ビルド + npm install + 実行
├── scenario/prepare.mjs    # サーバーディレクトリ組み立て(依存jar取得・設定展開・world初期化)
├── scenario/run-match.mjs  # サーバー起動 → ボット接続 → フェーズ待ち → 判定 → report.json
├── settings/               # テスト用サーバー設定(config.yml は ServerType: MATCH)
└── .run/, .cache/          # 生成物
```

- 参加経路は `ServerType: MATCH` を使う。`GameMgr.onPlayerJoin` が MATCH のとき
  `MatchMgr.playerJoinMatch` を直接呼ぶので、ボットはGUI(チェストGUI/看板)を操作しなくてよい。
- 試合は `StartPlayerCount: 2` で2体1試合。`MatchTime.Default: 20` で短縮。
- ボットは試合中ずっと中央を向いて右クリック(射撃)する。`PlayerInteractEvent` → `MainWeaponMgr.useMainWeapon`
  → 塗り、という本番の経路をそのまま通るので、リザルトのスコアが 50/50 から動く = 塗りが動いた証拠になる。
- 地形は `level-type=FLAT` の既定ワールドをロビーとマップで兼用。`bukkit.yml` の
  `worlds.<name>.generator` はプラグイン名しか受け付けずバニラの FLAT を指定できないため、
  動的生成ワールドをフラットにする手段がない(テスト用プラグインを足すほどではないと判断)。

### ハマりどころ(実測)

| 症状 | 原因 / 対処 |
|---|---|
| `NoClassDefFoundError: org/slf4j/LoggerFactory` | Spigot 1.14.4 は slf4j 非同梱。`slf4j-api` + `slf4j-jdk14` を JVM クラスパスに追加(プラグインのクラスローダの親に入る) |
| Sclat のログが出ない | slf4j の binding が無いと NOP logger になる。上記の `slf4j-jdk14` で解決 |
| `NullPointerException: NoteBlockAPIMgr.loadSongFiles` | `config.yml` に `nBGM: {}` / `fBGM: {}` が必須(空セクションでよい) |
| `NullPointerException: WeaponClassMgr.weaponClassSetup` | `class.yml` に `WeaponClass:` の子が1つ以上必要(空セクションは null になる) |
| `StatusClient` の `ConnectException` が延々と出る | MATCH サーバーは StatusShare に接続する。テスト側で 25598 に捨てサーバーを立てて塞ぐ |
| `Could not set generator for default world: Plugin 'FLAT' does not exist` | `bukkit.yml` の worlds generator はプラグイン名のみ。`level-type=FLAT` を使う |

## 検証項目

1. Sclat が enable で落ちない
2. 開始カウントダウン / モード告知のチャットが全ボットに届く
3. 「試合が開始されました」→ 試合開始
4. 制限時間で試合終了(`Finish!`)→ リザルトのタイトル(`You Win!` / `You Lose...` / `引き分け！`)
5. リザルトのスコアが合計100%・ボット間で一致・勝敗が矛盾しない
6. プラグイン由来のスタックトレースが0件
7. 試合終了後もサーバーが生存

## 検証結果(このホスト)

- `bash e2e/run.sh` → PASS(0 errors)。所要 約115s(起動 約42s + 試合 約70s)
- リザルト: 53% / 47% で勝敗が分かれる = 射撃・塗り・集計・リザルト演出まで動作
- サーバーログに `at be4rjp.sclat.…` のスタックトレースなし

## 未確定事項

1. TDM / TDMLong / Area のシナリオも回すか(`WorkMode` を差し替えるだけで枠組みは流用できる)
2. 4人(2v2)以上のシナリオをやるか(`StartPlayerCount` と最大人数の扱いを決める必要がある)
3. GitHub Actions に載せるか(BuildTools 約10分 + node が要るので別ジョブ/手動トリガー想定)
