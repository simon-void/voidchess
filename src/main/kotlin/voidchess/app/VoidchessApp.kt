package voidchess.app

import voidchess.common.integration.ComputerPlayerUI
import voidchess.ui.initializeUI
import javax.swing.JOptionPane


fun main() {
    runCatching {
        val engineAdapter = EngineAdapter()
        val table = Table(engineAdapter)
        val computerPlayerUI: ComputerPlayerUI = initializeUI(
            engineAdapter.getEngineConfig(),
            table
        )
        table.postConstruct(computerPlayerUI)

        Unit
    }.onFailure { exception ->
        JOptionPane.showMessageDialog(
            null,
            """
            An exception occurred: $exception
            Please consider logging an Issue here:
            https://github.com/simon-void/voidchess/issues
            """.trimIndent(),
            "VoidChess error",
            JOptionPane.ERROR_MESSAGE
        )
    }
}