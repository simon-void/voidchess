package voidchess.ui.swing

import voidchess.ui.image.ImageLoader

import javax.swing.*
import java.awt.*


internal class ChessFrame(
    chessPanel: ChessPanel
) : JFrame("  VoidChess  ") {

    init {
        iconImage = ImageLoader.icon
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane = chessPanel
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
