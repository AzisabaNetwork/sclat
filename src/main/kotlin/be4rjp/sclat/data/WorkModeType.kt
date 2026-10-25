package be4rjp.sclat.data

import be4rjp.sclat.Sclat

/**
 * config.yml の `WorkMode` 値と、その判定・表示名をまとめたヘルパー。
 *
 * `WorkMode` は文字列として config に保存されており参照箇所が多いため、
 * 新しいモードを追加するときはここに行を足して各分岐へ追従させる。
 */
object WorkModeType {
    const val NORMAL: String = "Normal"
    const val TDM: String = "TDM"
    const val TDM_LONG: String = "TDMLong"
    const val AREA: String = "Area"
    const val TRIAL: String = "Trial"

    /** 試合時間の既定値 (秒) */
    const val DEFAULT_MATCH_TIME: Int = 180
    const val TDM_LONG_MATCH_TIME: Int = 300

    fun current(): String = Sclat.conf?.config?.getString("WorkMode") ?: NORMAL

    /** TDM 系（ロングを含む）: キル数勝負・エリア処理なし */
    fun isTdm(): Boolean = current() == TDM || current() == TDM_LONG

    fun isArea(): Boolean = current() == AREA

    fun isTrial(): Boolean = current() == TRIAL

    fun isTdmLong(): Boolean = current() == TDM_LONG

    /** 試合時間 (秒)。config.yml の MatchTime.Default / MatchTime.TDMLong で上書きできる */
    fun matchTimeSeconds(): Int {
        val config = Sclat.conf?.config
        return if (isTdmLong()) {
            config?.getInt("MatchTime.TDMLong", TDM_LONG_MATCH_TIME) ?: TDM_LONG_MATCH_TIME
        } else {
            config?.getInt("MatchTime.Default", DEFAULT_MATCH_TIME) ?: DEFAULT_MATCH_TIME
        }
    }

    /** スコアボード・タイトル・チャットに表示するモード名 */
    fun displayName(): String =
        when (current()) {
            TDM -> "チームデスマッチ"
            TDM_LONG -> "チームデスマッチ(ロング)"
            AREA -> "ガチエリア"
            else -> "ナワバリバトル"
        }
}
