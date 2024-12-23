package voidchess.ui.inner.swing

import voidchess.common.board.move.Move
import voidchess.common.board.other.Chess960Index
import voidchess.common.helper.toUrlSafeEncoding
import java.awt.Color
import javax.swing.*


internal class ShareGamePanel(
    private val getMovesPlayed: ()->List<Move>,
    private val getCurrentChess960Index: ()->Chess960Index,
) : JPanel() {
    private var shareGameButton: JButton = JButton("share game")

    init {
        shareGameButton.isEnabled = false
        designLayout()
    }

    var enableShareButtonAssumingThereAreMovesToShare: Boolean
        get() = shareGameButton.isEnabled
        set(value) {shareGameButton.isEnabled = value && getMovesPlayed().isNotEmpty()}

    private fun designLayout() {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        background = Color.WHITE

        shareGameButton.addActionListener { displayMovesPlayed() }

        add(shareGameButton)
    }

    private fun displayMovesPlayed() {
        val movesPlayed: List<Move> = getMovesPlayed()
        val chess960Index = getCurrentChess960Index()
        val encodedMoves = movesPlayed.toUrlSafeEncoding(chess960Index)
        val url = "https://simon-void.github.io/chess_replay_rs/?c960=${chess960Index.value}&moves=$encodedMoves"

        JOptionPane.showMessageDialog(
            null,
            JScrollPane(JTextArea(6, 40).apply {
                text = url
                isEditable = false
                wrapStyleWord = true
                lineWrap = true
            }),
            "Share this URL to share this game",
            JOptionPane.PLAIN_MESSAGE,
        )
    }
}
