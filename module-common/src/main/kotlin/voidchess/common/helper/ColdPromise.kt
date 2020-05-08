package voidchess.common.helper

class ColdPromise<out T>(
    private val generateValue: ()->T
) {
    fun computeAndCallback(
        callback: (T)->Unit
    ) {
        val value = generateValue()
        callback(value)
    }
}
