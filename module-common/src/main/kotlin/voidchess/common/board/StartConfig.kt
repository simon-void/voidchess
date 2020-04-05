package voidchess.common.board

sealed class StartConfig(val isClassicStartConfig: Boolean, val doesWhitePlayerStart: Boolean) {
    object ClassicConfig: StartConfig(true, true) {
        val chess960Index = 518
    }
    class Chess960Config(val chess960Index: Int): StartConfig(chess960Index==518, true)
    class ManualConfig(val boardConfig: String, doesWhitePlayerStart: Boolean): StartConfig(false, doesWhitePlayerStart)
}