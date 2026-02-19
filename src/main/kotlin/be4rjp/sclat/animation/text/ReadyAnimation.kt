package be4rjp.sclat.animation.text

class ReadyAnimation(
    private val text: String,
) {
    init {
        require(text.isNotBlank()) { "text must not be blank" }
    }

    private var step = 0
    private val maxSteps = text.length

    /**
     * get next frame
     * @return frame string. if finished, returns null
     */
    fun next(): String? {
        if (step > maxSteps) return null

        val frame = "${text.take(step)}§7${text.drop(step)}"
        step++
        return frame
    }
}
