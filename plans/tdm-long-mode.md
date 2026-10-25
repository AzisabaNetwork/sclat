# 実装計画: チームデスマッチ（ロング／5分）モード追加

- ブランチ: `feature/tdm-long-mode`
- 対象: ゲームモード選択 UI（投票）とマッチ進行（タイマー・スコアボード・結果表示）
- 前提: `WorkMode` は config.yml 上の文字列（`Normal` / `TDM` / `Area` / `Trial`）で、サーバーごとに保持される

## 1. 仕様（案）

### 1.1 追加するモード
| 内部値 (`WorkMode`) | 表示名 | 試合時間 | 勝敗判定 |
|---|---|---|---|
| `TDM` (既存) | チームデスマッチ | 3:00 (180s) | キル数 |
| **`TDMLong` (新規)** | チームデスマッチ(ロング) | **5:00 (300s)** | キル数（TDMと同一） |
| `Normal` (既存) | ナワバリバトル | 3:00 | 塗り面積 |
| `Area` (既存) | ガチエリア | 3:00 | カウント |

ロングは「TDM の試合時間だけ 5 分に延ばした別モード」。ルール（キル数勝負・延長戦なし・`tdmResultAnimation`）は TDM と完全に共通。

### 1.2 ゲームモード選択 UI（`Chose a Gamemode`）
現状は 18 スロットのうち **slot 2 = ナワバリ / slot 4 = TDM / slot 6 = ガチエリア**（ガチは `canAreaBattle` の時だけ設置）。
つまり 1 マス空けで 3 つ並んでいる。

**決定レイアウト（均等割り・空き枠を作らない）**

| マップ種別 | slot 1 | slot 2 | slot 3 | slot 4 | slot 5 | slot 6 | slot 7 |
|---|---|---|---|---|---|---|---|
| ガチ有り（4 種） | **ナワバリ** | – | **TDM** | – | **ガチエリア** | – | **TDM(ロング)** |
| ガチ無し（3 種） | – | **ナワバリ** | – | **TDM** | – | **TDM(ロング)** | – |

- 間隔 2 マス・左右マージン均等。4 種なら `1,3,5,7`、3 種なら現状どおり `2,4,6`。
- ガチ不可マップではガチ枠が消え、ロングが 3 番目に入る（= 「ガチエリアの枠にロング」）。
- ガチ有りマップでもロングは 4 番目に常設（選べないと意味がないため）。
- 端に半端な空きスロットは残さない。

### 1.3 テキスト
| 箇所 | 変更内容 |
|---|---|
| ボタン displayName | `チームデスマッチ(ロング)`（既存ボタンに英字併記はないので日本語のみ） |
| ボタン素材 / lore | `Material.IRON_SWORD`（通常 TDM の `DIAMOND_SWORD` と区別）＋ lore `§7試合時間 5:00` |
| 開始チャット | `§6ゲームモード : §b§lチームデスマッチ(ロング)` + `§f§l5分間、敵よりもキルをしろ！` |
| イントロタイトル副題 | `§7チームデスマッチ(ロング)` |
| イントロ／試合中スコアボード | `§lゲームモード » チームデスマッチ(ロング)` |
| 残り時間 | イントロ: `5:00`、試合中: `s/60:s%60` で自動的に 5:00 → 0:00 |
| 終了結果 | 既存 `tdmResultAnimation` を流用（キル数表示はそのまま） |
| （任意）GUI タイトル | `Chose a Gamemode` → `Choose a Gamemode` の typo 修正 |

## 2. 現状コードマップ（触る場所）

### モード識別子
- `src/main/kotlin/be4rjp/sclat/manager/MatchMgr.kt:324-343` — 投票結果の集計。`nawabariTCount` / `tdmTCount` / `gatiareaTCount` の比較で `WorkMode` を `Normal` / `TDM` / `Area` に確定。
- `WorkMode` の参照は全体で 46 箇所（うち `== "TDM"` 4 箇所、`== "Area"` 10 箇所、`!= "Area"` 1 箇所、残りは `Trial` 判定）。

### 投票 UI / 入力
- `src/main/kotlin/be4rjp/sclat/gui/OpenGUI.kt:277-310` `matchTohyoGUI()` — slot 2/4/6 にボタン、`if (match!!.mapData!!.canAreaBattle) inv.setItem(6, nu)`。
- `src/main/kotlin/be4rjp/sclat/gui/ClickListener.kt:205-218` — **ボタンの displayName 文字列一致**で投票（`"ナワバリバトル"`, `"チームデスマッチ"`, `"ガチエリア"`）。新モードはここに分岐追加が必要。
- `src/main/kotlin/be4rjp/sclat/data/Match.kt:27-31, 61-71` — 投票カウンタ（`nawabariTCount` / `tdmTCount` / `gatiareaTCount`）。
- `src/main/kotlin/be4rjp/sclat/manager/MatchMgr.kt:146-152` — 参加時に `CanVoting` なら `matchTohyoGUI` を開く。

### マップ側のフラグ
- `src/main/kotlin/be4rjp/sclat/data/MapData.kt:75` `canAreaBattle`
- `src/main/kotlin/be4rjp/sclat/manager/MapDataMgr.kt:101-104` — map config に `Maps.<name>.Area` があると `canAreaBattle = true`。

### 試合時間・進行
- `src/main/kotlin/be4rjp/sclat/manager/MatchMgr.kt:1236` `var s: Int = 180` ← **試合時間の唯一の定義**
- `MatchMgr.kt:836` イントロスコアボードの `残り時間 » 3:00`（ハードコード）
- `MatchMgr.kt:1407-1436` `s == 60` で「残り1分」通知＋ラスト曲（TDM/ナワバリのみ、`!= "Area"`）
- `MatchMgr.kt:1437-1459` `s <= 0` / `s <= -60` で終了、`s in 1..5` でカウントダウン表示
- `MatchMgr.kt:895-936` — イントロの隊列演出は tick 固定（`160..220` など）。**試合時間とは独立なので変更不要**。

### モード別のテキスト／判定
- `MatchMgr.kt:610-639` 開始時メッセージ（`when (WorkMode)`）
- `MatchMgr.kt:777-801` イントロタイトル
- `MatchMgr.kt:822-836` イントロスコアボード
- `MatchMgr.kt:1257-1269` 試合中スコアボードのモード行
- `MatchMgr.kt:1278-1403` ガチエリア固有処理（ロングは通らない）
- `MatchMgr.kt:1586-1624` 結果判定（TDM: `killCount` 比較 → `tdmResultAnimation`）

## 3. 実装方針

### 3.1 モード判定をヘルパーに寄せる（取りこぼし防止）
新モード追加で 46 箇所の文字列比較を個別に直すのは危険なので、まず小さなヘルパーを入れる。

```kotlin
// be4rjp/sclat/data/WorkModeType.kt (新規)
object WorkModeType {
    const val NORMAL   = "Normal"
    const val TDM      = "TDM"
    const val TDM_LONG = "TDMLong"
    const val AREA     = "Area"
    const val TRIAL    = "Trial"

    fun current(): String = Sclat.conf!!.config!!.getString("WorkMode") ?: NORMAL

    /** TDM 系（ロング含む）: キル数勝負・エリア処理なし */
    fun isTdm(): Boolean = current() == TDM || current() == TDM_LONG
    fun isArea(): Boolean = current() == AREA
    fun isTrial(): Boolean = current() == TRIAL
}
```

置換対象（挙動不変のリファクタ）:
- `== "TDM"` 4 箇所（`MatchMgr.kt:779, 824, 1259, 1588`）→ `WorkModeType.isTdm()`
- `!= "Area"`（`MatchMgr.kt:1410`）→ `!WorkModeType.isArea()`
- `== "Area"` 10 箇所は段階的に（無理に一括でやらない）

これにより **ロングが TDM と同じ経路を通る**ことが保証される（結果画面・スコアボード・終了条件など）。

### 3.2 試合時間
`MatchMgr.kt:1236` を開始時に確定させる。

```kotlin
var s: Int = if (WorkModeType.current() == WorkModeType.TDM_LONG)
        Sclat.conf!!.config!!.getInt("MatchTime.TDMLong", 300)
    else
        Sclat.conf!!.config!!.getInt("MatchTime.Default", 180)
```

- 既定値はコード側に持たせ、既存 config.yml を書き換えなくても動くようにする（`MatchTime.*` は任意キー）。
- イントロスコアボード（`MatchMgr.kt:836`）の `3:00` は同じ値から `String.format("%d:%02d", s/60, s%60)` で生成。

### 3.3 投票
1. `Match.kt` に `tdmLongTCount` と `addTdmLongTCount()` を追加。
2. `ClickListener.kt:215` 付近に `"チームデスマッチ(ロング)" -> m.addTdmLongTCount()` を追加。
3. `MatchMgr.kt:328-342` の集計を 4 択に拡張。同点時の優先順位は既存の並びを踏襲:
   ```
   Normal >= TDM && Normal >= Area && Normal >= TDMLong → Normal
   else if TDM >= Area && TDM >= TDMLong               → TDM
   else if TDMLong >= Area                             → TDMLong
   else                                                → Area
   ```
   （全員無投票＝全部 0 のときは既存どおり `Normal`）
4. ガチ不可マップではガチボタンを出さないので `gatiareaTCount` は 0 のまま = 実質到達不能。防御的に「`canAreaBattle == false` なら `Area` に確定させない」ガードも入れる。

### 3.4 UI（`OpenGUI.matchTohyoGUI`）
```kotlin
val canArea = match!!.mapData!!.canAreaBattle
val slots = if (canArea) intArrayOf(1, 3, 5, 7) else intArrayOf(2, 4, 6)

inv.setItem(slots[0], nawabari)
inv.setItem(slots[1], tdm)
if (canArea) {
    inv.setItem(slots[2], gachi)     // ガチエリア
    inv.setItem(slots[3], tdmLong)   // TDM(ロング)
} else {
    inv.setItem(slots[2], tdmLong)   // ガチ枠にロング
}
```
- ロングのアイテム: `Material.IRON_SWORD` + `displayName = "チームデスマッチ(ロング)"` + `lore = ["§7試合時間 5:00"]`。
- 背景の黒ガラス（0..17 を埋める既存ループ）はそのまま。ボタンを置いたスロットだけ上書きされる。
- ⚠️ `ClickListener` は **displayName 完全一致**で分岐するので、`"チームデスマッチ(ロング)"` の分岐を追加（`"チームデスマッチ"` とは別文字列なので誤マッチしないが、prefix 判定に変えるなら要注意）。

### 3.5 テキスト差し替え一覧（`when` の網羅漏れ注意）
`MatchMgr.kt` の `when (WorkMode)`（620-639）と各 `if/else`（777-801 / 822-836 / 1257-1269 / 1586 以降）に `TDMLong` 分岐を追加。
将来の取りこぼしを防ぐため、`3.1` のヘルパーと併せて「モード名を返す関数」を作るのが望ましい:

```kotlin
fun displayName(): String = when (current()) {
    TDM      -> "チームデスマッチ"
    TDM_LONG -> "チームデスマッチ(ロング)"
    AREA     -> "ガチエリア"
    else     -> "ナワバリバトル"
}
```

## 4. 変更ファイル（想定）

| ファイル | 変更 |
|---|---|
| `data/WorkModeType.kt` (新規) | 定数・判定・表示名ヘルパー |
| `data/Match.kt` | `tdmLongTCount` 追加 |
| `gui/OpenGUI.kt` | `matchTohyoGUI` のスロット再配置 |
| `gui/ClickListener.kt` | 新ボタンの投票分岐 |
| `manager/MatchMgr.kt` | 集計 4 択 / 試合時間 300s / テキスト 5 箇所 / 結果分岐を `isTdm()` 化 |
| `plans/tdm-long-mode.md` | 本ファイル |

**触らなくてよい**もの: 武器・ギア・パケット・`Area`/`Paint` 系処理、`MapData`（`canAreaBattle` は既にある）。

## 5. 検証

1. `./gradlew build`（ktlint 含む）がグリーン。
2. `ktlintFormat` 後に差分が整形のみであること。
3. 手動確認（ローカル Paper 1.14.4 + 依存プラグイン）:
   - ガチ有りマップで参加 → 4 ボタンが slot 1,3,5,7 に均等配置される
   - ガチ無しマップで参加 → 3 ボタン（ロングが 3 番目）
   - ロングに投票して開始 → スコアボードが `5:00` 始まり、残り `1:00` で「残り1分」通知＋ラスト曲、`0:00` で終了
   - 結果画面が TDM と同じ（キル数・`tdmResultAnimation`）
   - ロング以外の 3 モードが従来どおり 3:00 で動く（リグレッション）
4. `CanVoting: false` のサーバー（tdm 専用など）で `WorkMode: TDMLong` を直接設定しても 5 分動作する。

## 6. 未確定事項

- **~~Q1. UI レイアウト~~ → 決定**: 4 種のときは 4 つ均等（`1,3,5,7`）、3 種のときは 3 つ均等（`2,4,6`）。半端な空き枠は作らない。
- **~~Q2. ボタンの見た目~~ → 決定**: `IRON_SWORD` ＋ lore `§7試合時間 5:00`。
- **~~Q3. 試合時間の外出し~~ → 決定**: `MatchTime.TDMLong`（既定 300）/ `MatchTime.Default`（既定 180）を**任意キー**として読む。config.yml に書かなければ既定値で動く。
- **~~Q4. ロングの勝敗・レート~~ → 決定**: TDM と同一扱い（`RankMgr` は `WorkMode` を見ていないので変更不要）。
- **~~Q5. デプロイ側 config~~ → 決定**: リポジトリ外の運用 config は触らない。TDM 専用サーバーにロングを出す場合は運用側で `WorkMode: TDMLong` を設定する（§5-4 で動作確認）。

## 7. コミット分割

実施済み（`MatchMgr` 内で変更が交錯するため計画の 5 分割から 2 分割に変更）:

1. `feat(data): WorkModeType ヘルパーと TDM(ロング) の投票カウンタを追加` — `data/WorkModeType.kt`（新規）, `data/Match.kt`
2. `feat: チームデスマッチ(ロング/5分)モードを追加` — `gui/OpenGUI.kt`, `gui/ClickListener.kt`, `manager/MatchMgr.kt`

## 8. 実装結果メモ

- `./gradlew build` / `ktlintCheck` グリーン、`build/libs/Sclat.jar` に `be4rjp/sclat/data/WorkModeType.class` と `plugin.yml` を確認
- 実行時（ゲーム内）の確認は未実施: ローカルに Paper 1.14.4 + ProtocolLib / NoteBlockAPI / DADADAChecker / BlockStudio と maps.yml・ワールド一式が必要で、このホストでは用意していない
- `WorkMode` の残存文字列比較は全て `Area` 専用処理（エリア初期化・倍率・結果分岐）で、`TDMLong` は else 側で TDM と同じ経路を通ることを確認済み
