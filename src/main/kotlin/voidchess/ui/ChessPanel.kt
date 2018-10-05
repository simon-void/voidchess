package voidchess.ui

import voidchess.board.ChessGame
import voidchess.board.MoveResult
import voidchess.helper.ChessGameSupervisorDummy
import voidchess.player.HumanPlayer
import voidchess.player.ki.ComputerPlayer

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
        val game = ChessGame(ChessGameSupervisorDummy)
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
        val buttonPanel = JPanel(FlowLayout())
        buttonPanel.background = Color.WHITE
        buttonPanel.add(startButton)
        buttonPanel.add(switchButton)

        val computerPlayerOptionsPanel = getComputerPlayerSettingsPanel()

        val computerUIPanel = JPanel(BorderLayout())
        computerUIPanel.background = Color.WHITE
        computerUIPanel.add(computerPlayerUI, BorderLayout.CENTER)
        computerUIPanel.add(computerPlayerOptionsPanel, BorderLayout.SOUTH)

        val gameUIPanel = JPanel()
        gameUIPanel.background = Color.WHITE
        gameUIPanel.add(chessboardComponent)

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

        val panel = JPanel(GridLayout(2, 2))
        panel.background = Color.WHITE
        panel.border = LineBorder(Color.LIGHT_GRAY)
        panel.background = Color.WHITE
        panel.add(difficultyPanel.label)
        panel.add(difficultyPanel)
        panel.add(coresPanel.label)
        panel.add(coresPanel)

        return panel
    }

    private fun add(component: JComponent, layout: GridBagLayout, x: Int, y: Int) {
        val constraints = GridBagConstraints()
        constraints.gridx = x
        constraints.gridy = y
        layout.setConstraints(component, constraints)
        add(component)
    }

    override fun actionPerformed(e: ActionEvent) {
        val com = e.actionCommand
        when (com) {
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
