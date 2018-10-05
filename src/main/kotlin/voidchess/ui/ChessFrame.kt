package voidchess.ui

import voidchess.image.ImageLoader

import javax.swing.*
import java.awt.*

fun main(args: Array<String>) {
    try {
        //Swing UI updates have to come from the SwingHandler or something
        SwingUtilities.invokeLater { ChessFrame() }
    } catch (e: Exception) {
        val sb = "The game got canceled because of an error.\nThe error message is:\n$e"
        JOptionPane.showMessageDialog(null, sb)
        e.printStackTrace()
        System.exit(1)
    }
}

class ChessFrame : JFrame("  VoidChess960  ") {

    init {
        iconImage = ImageLoader.icon
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane = ChessPanel()
        pack()
        isResizable = false
        center()
        isVisible = true
    }

    private fun center() {
        try {
            val frameSize = size
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val windowSize = ge.maximumWindowBounds
            setLocation(Math.max(windowSize.width / 2 - frameSize.width / 2, 0), Math.max(windowSize.height / 2 - frameSize.height / 2, 0))
        } catch (e: RuntimeException) {
            // best effort
        }
    }
}
