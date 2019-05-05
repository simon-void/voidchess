package voidchess.player.ki.concurrent

fun getConcurrencyStrategy(showProgress: (Int, Int) -> Unit, numberOfCoresToUse: Int) = if (numberOfCoresToUse == 1) {
    SingleThreadStrategy(showProgress)
} else {
    MultiThreadStrategy(showProgress, numberOfCoresToUse)
}