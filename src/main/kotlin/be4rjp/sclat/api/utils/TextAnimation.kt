package be4rjp.sclat.api.utils

class TextAnimation(
    text: String,
    private val length: Int,
) {
    private val text: String

    private var index = 0

    init {
        this.text = text + text + text + text
    }

    fun next(): String {
        var line = text.substring(index, index + length)

        var plus = 0
        var hankaku = 0

        val chars = line.toCharArray()
        for (aChar in chars) {
            if (SMALLS.contains(aChar)) {
                plus++
            } else if (aChar.toString().toByteArray().size < 2) {
                hankaku++
            }
        }

        line = text.substring(index, index + length + plus + hankaku / 2)

        index++
        if (index == text.length / 4) index = 0

        return line
    }

    companion object {
        private val SMALLS: MutableSet<Char?> =
            HashSet<Char?>(
                mutableListOf<Char?>('.', '|', 'i', '!', '！', '/', '1', ' ', 'l'),
            )
    }
}
