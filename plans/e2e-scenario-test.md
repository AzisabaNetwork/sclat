# e2e シナリオテスト(試合を1つまるごと実行する)

## 目的

「試合が最後まで破綻せずに回る」ことを自動で確認できるようにする。
`MatchMgr.matchRunnable` は毎tick Bukkit API(ブロック・エンティティ・パケット・スコアボード・サウンド)を
叩くため、ユニットテストでは実質検証できない。実サーバー上で1試合通すのが最短で確実。

対象は **Normal / TDM / TDMLong / Area** の4モード。

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
.github/workflows/e2e.yml   # 4モードを matrix で並列実行
```

- 参加経路は `ServerType: MATCH` を使う。`GameMgr.onPlayerJoin` が MATCH のとき
  `MatchMgr.playerJoinMatch` を直接呼ぶので、ボットはGUI(チェストGUI/看板)を操作しなくてよい。
- 試合は `StartPlayerCount: 2` で2体1試合。
- ボットは試合中ずっと右クリック(射撃)する。`PlayerInteractEvent` → `MainWeaponMgr.useMainWeapon`
  → 塗り、という本番の経路をそのまま通るので、リザルトのスコアが 50/50 から動く = 塗りが動いた証拠になる。
  TDM 系は相手を狙って撃ち、Area は1体だけがエリア内で旋回しながら塗る。
- 地形は `level-type=FLAT` の既定ワールドをロビーとマップで兼用。`bukkit.yml` の
  `worlds.<name>.generator` はプラグイン名しか受け付けずバニラの FLAT を指定できないため、
  動的生成ワールドをフラットにする手段がない(テスト用プラグインを足すほどではないと判断)。

### モード別の調整(prepare.mjs)

| モード | 調整 |
|---|---|
| Normal | `WorkMode: Normal`, `MatchTime.Default: 20` |
| TDM | `WorkMode: TDM`、スポーン間隔を 8 ブロックに詰め、`Damage 20 / ShootSpeed 3.0 / ShootRandom 0.5` |
| TDMLong | 上記 + `MatchTime.TDMLong: 30` |
| Area | `WorkMode: Area`、`MatchTime.Default: 120`(gatiCount が 1/秒で増え 100 で Knock Out のため)、3x3 の Area 定義 |

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
6. プラグイン由来のスタックトレースが0件
7. 試合終了後もサーバーが生存

## CI(GitHub Actions)

- `spigot` ジョブで BuildTools を1回だけ実行し `~/.m2` をキャッシュ
- `e2e` ジョブを `mode: [Normal, TDM, TDMLong, Area]` の matrix で**並列**実行(キャッシュ復元 + pnpm)
- 失敗時も `report.json` / `server.log` を artifact に上げる

## 検証結果(このホスト)

| モード | 結果 | 所要 | 備考 |
|---|---|---|---|
| Normal | PASS | 約115s | 53/47, 40/60, 26/74 と毎回スコアが動く |
| TDM | PASS | 約115s | 0 Kill の引き分け(ダメージ経路は未検証) |
| TDMLong | PASS | 約120s | 同上 |
| Area | PASS | 約215s | 120s 時間切れの引き分け(KO 未到達) |

## 未確定事項

1. TDM で実際にキルを発生させるか(ダメージ → DeathMgr → killCount までを通したい)
2. Area で Knock Out まで到達させるか(片チームがエリア全体を塗る動きの実装が要る)
3. 4人(2v2)以上のシナリオをやるか(`StartPlayerCount` と最大人数の扱いを決める必要がある)
4. Area の試合時間 120s は CI コストが大きい。KO を狙わないなら 30s に短縮できる
