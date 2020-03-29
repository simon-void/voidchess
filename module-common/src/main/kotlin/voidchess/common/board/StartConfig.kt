package voidchess.common.board

sealed class StartConfig(val isClassicStartConfig: Boolean) {
    object ClassicConfig: StartConfig(true) {
        val chess960Index = 518
    }
    class Chess960Config(val chess960Index: Int): StartConfig(chess960Index==518)
    object ManualConfig: StartConfig(false)
}