package voidchess

import voidchess.united.TableImpl
import voidchess.united.player.EnginePlayer
import voidchess.united.player.UiPlayer
import voidchess.common.integration.ComputerPlayerUI
import voidchess.common.integration.HumanPlayer
import voidchess.ui.initializeUI
import javax.swing.JOptionPane


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
    }.onFailure { exception ->
        JOptionPane.showMessageDialog(
            null,
            """
            An exception occurred: $exception
            If the exception is "bad", please consider logging an Issue here:
            https://github.com/simon-void/voidchess/issues
            """.trimIndent(),
            "VoidChess error",
            JOptionPane.ERROR_MESSAGE
        )
    }
}