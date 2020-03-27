package voidchess

import voidchess.ui.ChessFrame
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

fun main() {
    try {
        //Swing UI updates have to come from the SwingHandler or something
        SwingUtilities.invokeLater { ChessFrame() }
    } catch (e: Exception) {
        val sb = "The game got canceled because of an error.\nThe error message is:\n$e"
        JOptionPane.showMessageDialog(null, sb)
        e.printStackTrace()
        exitProcess(1)
    }
}