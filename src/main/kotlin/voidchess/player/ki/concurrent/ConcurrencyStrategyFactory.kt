package voidchess.player.ki.concurrent

import voidchess.ui.ComputerPlayerUI

object ConcurrencyStrategyFactory {
    @JvmStatic
    fun getConcurrencyStrategy(ui: ComputerPlayerUI, numberOfCoresToUse: Int): ConcurrencyStrategy {
        return if (numberOfCoresToUse == 1) {
            SingleThreadStrategy(ui)
        } else {
            MultiThreadStrategy(ui, numberOfCoresToUse)
        }
    }
}
