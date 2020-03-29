package voidchess

import voidchess.ui.ChessFrame
import javax.swing.SwingUtilities

fun main() {
    runCatching {
        //Swing UI updates have to come from the SwingHandler or something
        SwingUtilities.invokeLater { ChessFrame() }
    }
}