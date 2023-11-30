package voidchess.ui.swing

import voidchess.common.board.BasicChessGame
import voidchess.common.board.other.Chess960Index
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

    var chess960Index: Chess960Index = Chess960Index.classic
        private set
    private var classicButton: JButton = JButton("classic setup")
    private var random960Button: JButton = JButton("shuffle setup")
    private var positionIndexField: JTextField = JTextField(chess960Index.toString(), 3)

    init {
        designLayout()
        setPosition(Chess960Index.classic)
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
        classicButton.isEnabled = enabled && chess960Index.isNotClassic
        random960Button.isEnabled = enabled
        positionIndexField.isEditable = enabled
    }

    override fun actionPerformed(event: ActionEvent) {
        when (event.source) {
            classicButton -> setPosition(Chess960Index.classic)
            random960Button -> setPosition(Chess960Index.random())
            positionIndexField -> setPosition(positionIndexField.text)
        }
    }

    private fun setPosition(position: String) {
        try {
            val positionIndex = position.toInt()
            when {
                positionIndex < 0 -> setPosition(Chess960Index.min)
                positionIndex > 959 -> setPosition(Chess960Index.max)
                else -> {
                    chess960Index = Chess960Index(positionIndex)
                    pullPosition(chess960Index)
                }
            }
        } catch (e: NumberFormatException) {
            setPosition(Chess960Index.classic)
        }
    }

    private fun setPosition(position: Chess960Index) {
        chess960Index = position
        positionIndexField.text = position.value.toString()
        pullPosition(position)
    }

    private fun pullPosition(position: Chess960Index) {
        classicButton.isEnabled = position.isNotClassic

        game.initGame(StartConfig.Chess960Config(position))
        gameUI.repaintAtOnce()
    }
}
