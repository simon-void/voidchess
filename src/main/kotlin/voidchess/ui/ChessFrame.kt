package voidchess.ui

import voidchess.image.ImageLoader

import javax.swing.*
import java.awt.*
import kotlin.system.exitProcess


class ChessFrame : JFrame("  VoidChess960  ") {

    init {
        iconImage = ImageLoader.icon
        defaultCloseOperation = EXIT_ON_CLOSE
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
            setLocation(
                    (windowSize.width / 2 - frameSize.width / 2).coerceAtLeast(0),
                    (windowSize.height / 2 - frameSize.height / 2).coerceAtLeast(0)
            )
        } catch (e: RuntimeException) {
            // best effort
        }
    }
}
