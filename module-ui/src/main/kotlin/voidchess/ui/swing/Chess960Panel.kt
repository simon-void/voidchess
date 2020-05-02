package voidchess.ui.swing

import voidchess.common.board.BasicChessGame
import voidchess.common.board.other.StartConfig

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import kotlin.math.floor

internal class Chess960Panel(
    private val game: BasicChessGame,
    private val gameUI: ChessboardComponent
) : JPanel(), ActionListener {

    var chess960Index: Int = 0
        private set
    private var classicButton: JButton = JButton("classic setup")
    private var random960Button: JButton = JButton("shuffle setup")
    private var positionIndexField: JTextField = JTextField(chess960Index.toString(), 3)

    private val randomPosition: Int
            get() = floor(Math.random() * 960).toInt()

    init {
        designLayout()
        setPosition(CLASSIC_CHESS_POSITION)
    }

    private fun designLayout() {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        background = Color.WHITE

        classicButton.addActionListener(this)
        random960Button.addActionListener(this)
        positionIndexField.addActionListener(this)

        add(classicButton)
        add(random960Button)
        add(JLabel(" current setup: "))
        add(positionIndexField)
    }

    override fun setEnabled(enabled: Boolean) {
        classicButton.isEnabled = enabled && chess960Index != CLASSIC_CHESS_POSITION
        random960Button.isEnabled = enabled
        positionIndexField.isEditable = enabled
    }

    override fun actionPerformed(event: ActionEvent) {
        when (event.source) {
            classicButton -> setPosition(CLASSIC_CHESS_POSITION)
            random960Button -> setPosition(randomPosition)
            positionIndexField -> setPosition(positionIndexField.text)
        }
    }

    private fun setPosition(position: Int) {
        chess960Index = position
        positionIndexField.text = position.toString()
        pullPosition(position)
    }

    private fun setPosition(position: String) {
        try {
            chess960Index = position.toInt()
            when {
                chess960Index < 0 -> setPosition(0)
                chess960Index > 959 -> setPosition(959)
                else -> pullPosition(chess960Index)
            }
        } catch (e: NumberFormatException) {
            setPosition(CLASSIC_CHESS_POSITION)
        }

    }

    private fun pullPosition(position: Int) {
        assert(position in 0..959)

        classicButton.isEnabled = position != CLASSIC_CHESS_POSITION

        game.initGame(StartConfig.Chess960Config(position))
        gameUI.repaintAtOnce()
    }

    companion object {
        // TODO should be move into an inline class Chess960StartConfig
        private const val CLASSIC_CHESS_POSITION = 518
    }
}
