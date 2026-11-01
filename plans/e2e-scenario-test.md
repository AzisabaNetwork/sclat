# e2e シナリオテスト(試合を1つまるごと実行する)

## 目的

「試合が最後まで破綻せずに回る」ことを自動で確認できるようにする。
`MatchMgr.matchRunnable` は毎tick Bukkit API(ブロック・エンティティ・パケット・スコアボード・サウンド)を
叩くため、ユニットテストでは実質検証できない。実サーバー上で1試合通すのが最短で確実。

対象は **Normal / TDM / TDMLong / Area** の4モード＋**TDM の 2v2**(`--bots 4`)。

## 方式の比較

| 方式 | 判定 |
|---|---|
| MockBukkit / Bukkit をモックした in-JVM テスト | 1試合の間に呼ばれる Bukkit API が多すぎてモック surface が非現実的。新規の Bukkit 呼び出しのたびに壊れる |
| 実サーバー + 偽プレイヤー(NMS で ServerPlayer を生成) | 1.14.4 の NMS に密結合。バージョン更新で壊れ、クライアントパケット経路も通らない |
| **実サーバー + 実クライアント(mineflayer)** | **採用**。プロトコル経路・描画以外のすべてが本番同等。ボットは `pnpm add mineflayer` だけで用意できる |
| 実サーバー + 手動操作 | 自動化できないので回帰検出に使えない |

mineflayer は 1.14.4(protocol 498)に対応している。

## 構成

```
e2e/
├── run.sh                  # ビルド + pnpm install + 実行
├── scenario/prepare.mjs    # サーバーディレクトリ組み立て + モード別の設定調整
├── scenario/run-match.mjs  # サーバー起動 → ボット接続 → フェーズ待ち → 判定 → report.json
├── settings/               # テスト用サーバー設定(config.yml は ServerType: MATCH)
└── .run/, .cache/          # 生成物
.github/workflows/e2e.yml   # 4モード + TDM 2v2 を matrix で並列実行
```

- 参加経路は `ServerType: MATCH` を使う。`GameMgr.onPlayerJoin` が MATCH のとき
  `MatchMgr.playerJoinMatch` を直接呼ぶので、ボットはGUI(チェストGUI/看板)を操作しなくてよい。
- 試合は既定で `StartPlayerCount: 2` の1v1。`--bots 4` を渡すと `StartPlayerCount` も 4 になって 2v2 になる
  (チーム分けはサーバー側のシャッフルなので、クライアントからは味方/敵が分からない)。
- ボットは試合中ずっと右クリック(射撃)する。`PlayerInteractEvent` → `MainWeaponMgr.useMainWeapon`
  → 塗り、という本番の経路をそのまま通るので、リザルトのスコアが 50/50 から動く = 塗りが動いた証拠になる。
  TDM 系は他のプレイヤーを順番に狙って撃ち(味方を避けられないため)、Area は1体だけがエリア中央で旋回しながら塗る。
  射撃を始めるのは `GO!` のタイトル受信後(イントロ中に撃つとインクを空費して本番で撃てなくなる)。
- 地形は `level-type=FLAT` の既定ワールドをロビーとマップで兼用。`bukkit.yml` の
  `worlds.<name>.generator` はプラグイン名しか受け付けずバニラの FLAT を指定できないため、
  動的生成ワールドをフラットにする手段がない(テスト用プラグインを足すほどではないと判断)。

### モード別の調整(prepare.mjs)

| モード | 調整 |
|---|---|
| 全モード | `NeedInk: 0.002`(≈500 発)。既定の 0.9 は 1 発でインク切れする(下記) |
| Normal | `WorkMode: Normal`, `MatchTime.Default: 20` |
| TDM | `WorkMode: TDM`、スポーン間隔を 8 ブロックに詰め、`Damage 20 / ShootSpeed 3.0 / ShootRandom 0.5` |
| TDMLong | 上記 + `MatchTime.TDMLong: 30` |
| Area | `WorkMode: Area`、`MaxPaintDistance: 4` / `PaintRandom: 12`(塗りを 3x3 に収める)、`MatchTime.Default: 150`(確保から 100 秒かかるため余裕をとる)、3x3 の Area 定義 |
| TDM 2v2 | `--bots 4`。`StartPlayerCount: 4` になるので 4 人そろってからカウントダウンが始まる |

### ハマりどころ(実測)

| 症状 | 原因 / 対処 |
|---|---|
| `NoClassDefFoundError: org/slf4j/LoggerFactory` | Spigot 1.14.4 は slf4j 非同梱。`slf4j-api` + `slf4j-jdk14` を JVM クラスパスに追加(プラグインのクラスローダの親に入る) |
| Sclat のログが出ない | slf4j の binding が無いと NOP logger になる。上記の `slf4j-jdk14` で解決 |
| `NullPointerException: NoteBlockAPIMgr.loadSongFiles` | `config.yml` に `nBGM` / `fBGM` が必須 |
| 試合開始直後に何も起きなくなる | `nBGM` が空だと `NoteBlockAPIMgr.randomNormalSong` の `nextInt(0)` で `matchRunnable` が落ち、その後の `area.start()` や武器配布に到達しない。無音の `BGM/test.nbs` を同梱して解決 |
| `finishMatch` の NPE(i=157 付近) | `Hologram.*` 未設定で `PlayerStatusMgr.sendHologram` が落ちる(ServerType 問わず呼ばれる) |
| `Area.stop()` の `IllegalStateException: Not scheduled yet` | `area.start()` 前に試合が終わると発生。上記 BGM 落ちが引き金だった |
| `NullPointerException: WeaponClassMgr.weaponClassSetup` | `class.yml` に `WeaponClass:` の子が1つ以上必要(空セクションは null になる) |
| `StatusClient` の `ConnectException` が延々と出る | MATCH サーバーは StatusShare に接続する。テスト側で 25598 に捨てサーバーを立てて塞ぐ |
| `Could not set generator for default world: Plugin 'FLAT' does not exist` | `bukkit.yml` の worlds generator はプラグイン名のみ。`level-type=FLAT` を使う |
| 撃っているのにキルも塗りも発生しない | インクは `player.exp` で管理され、ヒト形態では回復しない(イカ形態のみ)。`NeedInk: 0.9` だと 1 発で息切れする。テストは `NeedInk: 0.002` にして撃ち続けさせる |
| イントロ中にインクを撃ち切る | 「試合が開始されました」はイントロの開始時点。`GO!` のタイトルが届くまで射撃しない |
| Area でエリアがいつまでも埋まらない | 着弾点の塗りは半径 `MaxPaintDistance`(=既定 24)のスフィアを `PaintRandom` 個でランダムに打ち切るため、遠くに散らばる。テストは半径 4 / PaintRandom 12 にする |
| Area のボットが走り去る | 前進を押しっぱなしにすると中心を通り過ぎて戻れない。短いバースト歩行 + 中心到達後は停止 |
| オフライン環境で「プラグイン例外 1 件」になる | `SkullMeta.owningPlayer` の Mojang セッション照会(`com.mojang.authlib`)が `SocketTimeout`。テスト側は環境要因として別カウントする(`environmentExceptions`) |

### プラグイン側の変更(テストで顕在化したもの)

`MatchMgr` の 3 つの `catch (e: Exception) { cancel() }` にログを追加した。
例外を握りつぶすと「試合が始まったまま何も起きない」状態で固まり、原因がログから一切分からない。
実際、上記の BGM 落ちはこの変更で初めて特定できた。

## 検証項目

1. Sclat が enable で落ちない
2. 開始カウントダウン / モード告知のチャットが全ボットに届く(モードごとに文言が違う)
3. 「試合が開始されました」→ 試合開始
4. 制限時間で試合終了(`Finish!`)→ リザルトのタイトル
   (Normal: `You Win!` / `You Lose...` / `引き分け！`、Area: `Knock Out !!`、TDM: `You  Win!` + `Team : N Kill`)
5. リザルトの数値が整合する(面積% は合計100%・ボット間一致・勝敗と矛盾しない / TDM はキル数)
6. TDM 系は実際にキルが発生する(射撃 → `SnowballListener` → `giveDamage` → `DeathMgr` → `killCount`)
7. Area は Knock Out まで到達する(エリア確保 → `gatiCount` 100)
8. プラグイン由来のスタックトレースが0件(環境要因の `com.mojang.authlib` 系は別枠)
9. 試合終了後もサーバーが生存
10. 2v2(`--bots 4`)でも同じ判定が通る

## CI(GitHub Actions)

- `spigot` ジョブで BuildTools を1回だけ実行し `~/.m2` をキャッシュ
- `e2e` ジョブを `Normal / TDM / TDMLong / Area / TDM-2v2(--bots 4)` の matrix で**並列**実行(キャッシュ復元 + pnpm)
- 失敗時も `report.json` / `server.log` を artifact に上げる

## 検証結果(このホスト)

| モード | 結果 | 所要 | 備考 |
|---|---|---|---|
| Normal | PASS | 約87s | 48/52, 47/53, 26/74 など毎回スコアが動く |
| TDM | PASS | 約88s | 1:1〜2:2 のキルが発生(ダメージ経路を通る) |
| TDMLong | PASS | 約98s | 1:1〜3:0 のキルが発生 |
| Area | PASS | 約171s | エリア確保 → Knock Out(100/0)まで到達 |
| TDM 2v2 | PASS | 約88s | 4 人で 1:2 / 2:2 のキルが発生 |

## 決定事項(旧・未確定事項)

1. **TDM のキル発生**: `NeedInk` と射撃開始タイミング(`GO!` 待ち)を直して実現。キルが 0 件なら FAIL にする
2. **Area の Knock Out**: 塗りスフィアをエリアに収め、試合時間を 150s に延ばして到達。KO 未到達は FAIL にする
3. **2v2**: `--bots 4` に対応(`StartPlayerCount` も追従)。CI でも `TDM-2v2` を 1 ジョブ回す
4. **Area の試合時間**: KO 到達を検証するので 150s のまま(CI の Area ジョブは約 3 分)
