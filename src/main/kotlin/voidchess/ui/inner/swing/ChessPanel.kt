package voidchess.ui.inner.swing

import voidchess.ui.inner.player.BoardUiListener
import javax.swing.*
import javax.swing.border.LineBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


internal class ChessPanel(
    private val boardUiListener: BoardUiListener,
    private val panel960: Chess960Panel,
    private val difficultyPanel: DifficultyPanel,
    private val coresPanel: CoresPanel,
    private val shareGamePanel: ShareGamePanel,
    chessboardComponent: ChessboardComponent,
    computerPlayerUI: ComputerPlayerComponent
) : JPanel(), ActionListener {

    private var startButton: JButton = JButton(resignString)
    private var switchButton: JButton = JButton(switchString)

    init {
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

        fun JPanel.concat(secondPanel: JPanel): JPanel {
            val firstPanel = this
            return JPanel(GridLayout(1, 2)).apply {
                background = Color.WHITE
                add(firstPanel)
                add(secondPanel)
            }
        }

        return JPanel(GridLayout(3, 1)).apply {
            border = LineBorder(Color.LIGHT_GRAY)
            add(difficultyPanel.label.concat(difficultyPanel))
            add(coresPanel.label.concat(coresPanel))
            add(JPanel(GridBagLayout()).apply {
                background = Color.WHITE
                add(shareGamePanel)
            })
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
            switchString -> boardUiListener.switchPlayerSelected()
            resignString -> boardUiListener.resignSelected()
        }
    }

    private fun start() {
        startButton.text = resignString
        switchButton.isEnabled = false
        panel960.isEnabled = false
        difficultyPanel.isEnabled = false
        coresPanel.isEnabled = false
        shareGamePanel.enableShareButtonAssumingThereAreMovesToShare = false
        boardUiListener.startSelected(panel960.chess960Index)
    }

    fun stop() {
        startButton.text = startString
        startButton.isEnabled = true
        switchButton.isEnabled = true
        panel960.isEnabled = true
        difficultyPanel.isEnabled = true
        coresPanel.isEnabled = true
        shareGamePanel.enableShareButtonAssumingThereAreMovesToShare = true
    }

    fun enableResign(enable: Boolean) {
        check(startButton.text == resignString) {"button should be in resign mode but is in start mode"}
        startButton.isEnabled = enable
    }

    companion object {
        private const val startString = "start"
        private const val resignString = "resign"
        private const val switchString = "change seats"
    }
}
