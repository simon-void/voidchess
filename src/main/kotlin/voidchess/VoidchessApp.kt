package voidchess

import voidchess.central.TableImpl
import voidchess.central.player.EnginePlayer
import voidchess.central.player.UiPlayer
import voidchess.common.integration.ComputerPlayerUI
import voidchess.common.integration.HumanPlayer
import voidchess.ui.initializeUI


fun main() {
    runCatching {
        val enginePlayer = EnginePlayer()
        val uiPlayer = UiPlayer()
        val (humanPlayer: HumanPlayer, computerPlayerUI: ComputerPlayerUI) = initializeUI(
            enginePlayer.getEngineConfig(),
            uiPlayer
        )
        TableImpl(humanPlayer, computerPlayerUI, uiPlayer, enginePlayer)

        Unit
    }
}