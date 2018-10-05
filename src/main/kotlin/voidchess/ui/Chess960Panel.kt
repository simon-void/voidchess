/*
 * Created on 20.10.2006
 */

package voidchess.ui

import voidchess.board.ChessGameInterface

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class Chess960Panel internal constructor(private val game: ChessGameInterface, private val gameUI: ChessboardComponent) : JPanel(), ActionListener {
    var positionCode: Int = 0
        private set
    private var classicButton: JButton = JButton("classic setup")
    private var random960Button: JButton = JButton("shuffle setup")
    private var positionIndexField: JTextField = JTextField(positionCode.toString(), 3)

    private val randomPosition = Math.floor(Math.random() * 960).toInt()

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
        classicButton.isEnabled = enabled && positionCode != CLASSIC_CHESS_POSITION
        random960Button.isEnabled = enabled
        positionIndexField.isEditable = enabled
    }

    override fun actionPerformed(event: ActionEvent) {
        val source = event.source
        when (source) {
            classicButton -> setPosition(CLASSIC_CHESS_POSITION)
            random960Button -> setPosition(randomPosition)
            positionIndexField -> setPosition(positionIndexField.text)
        }
    }

    private fun setPosition(position: Int) {
        positionCode = position
        positionIndexField.text = position.toString()
        pullPosition(position)
    }

    private fun setPosition(position: String) {
        try {
            positionCode = position.toInt()
            when {
                positionCode < 0 -> setPosition(0)
                positionCode > 959 -> setPosition(959)
                else -> pullPosition(positionCode)
            }
        } catch (e: NumberFormatException) {
            setPosition(CLASSIC_CHESS_POSITION)
        }

    }

    private fun pullPosition(position: Int) {
        assert(position in 0..959)

        classicButton.isEnabled = position != CLASSIC_CHESS_POSITION

        game.initGame(position)
        gameUI.repaintAtOnce()
    }

    companion object {
        private const val CLASSIC_CHESS_POSITION = 518
    }
}
