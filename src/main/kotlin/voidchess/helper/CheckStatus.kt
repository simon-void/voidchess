package voidchess.helper


class CheckStatus private constructor(
        val isCheck: Boolean,
        @get:JvmName("onlyKingCanMove") val isDoubleCheck: Boolean,
        val checkInterceptPositions: List<Position> = emptyList()
) {
    constructor(possiblePositions: List<Position>) :
            this(true, false, possiblePositions)

    companion object {
        val NO_CHECK = CheckStatus(false, false)
        val DOUBLE_CHECK = CheckStatus(true, true)
    }
}