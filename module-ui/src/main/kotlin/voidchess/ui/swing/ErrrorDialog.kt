package voidchess.ui.swing

import java.awt.Component
import javax.swing.JOptionPane

fun showErrorDialog(parent: Component?, exception: Throwable) {
    JOptionPane.showMessageDialog(
        parent,
        """
        An exception occurred:
        ${if(exception is IllegalStateException) exception.message else exception}
        Please consider logging an Issue here: https://github.com/simon-void/voidchess/issues
        Adding a screenshot of the position which triggered the problem would be appreciated.
        """.trimIndent(),
        "VoidChess error",
        JOptionPane.ERROR_MESSAGE
    )
}