package be4rjp.sclat.match

enum class MatchType(
    displayName: String,
    description: String,
) {
    Normal("ナワバリバトル", "敵よりもたくさんナワバリを確保しろ！"),
    TDM("チームデスマッチ", "敵よりもキルをしろ！"),
    Area("ガチエリア", "エリアを確保して守り抜け！"),
}
