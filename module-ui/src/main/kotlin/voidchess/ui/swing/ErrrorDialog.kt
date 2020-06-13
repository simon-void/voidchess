package voidchess.ui.swing

import java.awt.Component
import javax.swing.JOptionPane

fun showErrorDialog(parent: Component?, exception: Throwable) {
    JOptionPane.showMessageDialog(
        parent,
        """
        An exception occurred: $exception
        Please consider logging an Issue here:
        https://github.com/simon-void/voidchess/issues
        """.trimIndent(),
        "VoidChess error",
        JOptionPane.ERROR_MESSAGE
    )
}