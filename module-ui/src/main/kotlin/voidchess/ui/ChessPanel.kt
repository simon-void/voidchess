package voidchess.ui

import voidchess.board.CentralChessGameImpl
import voidchess.common.board.move.MoveResult
import voidchess.common.board.other.ChessGameSupervisorDummy
import voidchess.player.human.HumanPlayer
import voidchess.player.ComputerPlayer

import javax.swing.*
import javax.swing.border.LineBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


class ChessPanel : JPanel(), ActionListener {

    private var table: Table
    private var startButton: JButton
    private var switchButton: JButton
    private var panel960: Chess960Panel
    private var difficultyPanel: DifficultyPanel
    private var coresPanel: CoresPanel

    init {
        startButton = JButton(resignString)
        switchButton = JButton(switchString)
        val game =
            CentralChessGameImpl(ChessGameSupervisorDummy)
        val chessboardComponent = ChessboardComponent(game, this)
        panel960 = Chess960Panel(game, chessboardComponent)
        table = Table(game, chessboardComponent, this, panel960)
        game.useSupervisor(table)
        val humanPlayer = HumanPlayer(table, true, chessboardComponent, game)
        val computerPlayerUI = ComputerPlayerComponent()
        val kiPlayer = ComputerPlayer(table, game, computerPlayerUI)
        table.setWhitePlayer(humanPlayer)
        table.setBlackPlayer(kiPlayer)
        difficultyPanel = DifficultyPanel(kiPlayer)
        coresPanel = CoresPanel(kiPlayer)

        designLayout(chessboardComponent, computerPlayerUI)
    }

    private fun designLayout(chessboardComponent: ChessboardComponent, computerPlayerUI: ComputerPlayerComponent) {
        val prefSize = startButton.preferredSize
        startButton.text = startString
        startButton.preferredSize = prefSize
        startButton.addActionListener(this)
        switchButton.addActionListener(this)

        val buttonPanel = JPanel(FlowLayout()).apply {
            background = Color.WHITE
            add(startButton)
            add(switchButton)
        }

        val computerPlayerOptionsPanel = getComputerPlayerSettingsPanel()

        val computerUIPanel = JPanel(BorderLayout()).apply {
            background = Color.WHITE
            add(computerPlayerUI, BorderLayout.CENTER)
            add(computerPlayerOptionsPanel, BorderLayout.SOUTH)
        }

        val gameUIPanel = JPanel().apply {
            background = Color.WHITE
            add(chessboardComponent)
        }

        background = Color.WHITE
        val layout = GridBagLayout()
        setLayout(layout)
        add(buttonPanel, layout, 1, 1)
        add(panel960, layout, 2, 1)
        add(computerUIPanel, layout, 1, 2)
        add(gameUIPanel, layout, 2, 2)
    }

    private fun getComputerPlayerSettingsPanel(): JPanel {
        if (!coresPanel.hasOptions()) {
            return difficultyPanel
        }

        return JPanel(GridLayout(2, 2)).apply {
            background = Color.WHITE
            border = LineBorder(Color.LIGHT_GRAY)
            background = Color.WHITE
            add(difficultyPanel.label)
            add(difficultyPanel)
            add(coresPanel.label)
            add(coresPanel)
        }
    }

    private fun add(component: JComponent, layout: GridBagLayout, x: Int, y: Int) {
        val constraints = GridBagConstraints().apply {
            gridx = x
            gridy = y
        }
        layout.setConstraints(component, constraints)
        add(component)
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.actionCommand) {
            startString -> start()
            switchString -> table.switchPlayer()
            resignString -> table.stopGame(MoveResult.RESIGN)
        }
    }

    private fun start() {
        startButton.text = resignString
        switchButton.isEnabled = false
        panel960.isEnabled = false
        difficultyPanel.isEnabled = false
        coresPanel.isEnabled = false
        table.startGame()
    }

    fun stop() {
        startButton.text = startString
        switchButton.isEnabled = true
        panel960.isEnabled = true
        difficultyPanel.isEnabled = true
        coresPanel.isEnabled = true
    }

    companion object {
        private const val startString = "start"
        private const val resignString = "resign"
        private const val switchString = "change seats"
    }
}
