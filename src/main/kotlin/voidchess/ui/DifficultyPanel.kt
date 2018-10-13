/*
 * Created on 01.11.2006
 */

package voidchess.ui

import voidchess.player.ki.ComputerPlayer
import voidchess.player.ki.evaluation.SimplePruner

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class DifficultyPanel internal constructor(private val player: ComputerPlayer) : JPanel(), ActionListener {
    private val level1Pruner: SimplePruner = SimplePruner(1, 4, 3)
    private val level2Pruner: SimplePruner = SimplePruner(2, 3, 2)
    private val level3Pruner: SimplePruner = SimplePruner(2, 4, 3)
    private val comboBox: JComboBox<String>

    val label: JPanel
        get() {
            val panel = JPanel(FlowLayout(FlowLayout.RIGHT))
            panel.background = Color.WHITE
            val label = JLabel("difficulty:")
            panel.add(label)
            return panel
        }

    init {
        //it's a good idea to have figureHitRadius bigger than chessRadius
        //else the ai tries to prevent material loss through bad chesses
        player.setSearchTreePruner(level1Pruner)

        comboBox = JComboBox()
        designLayout()
        //preselect the second option
        comboBox.selectedIndex = 0
    }

    private fun designLayout() {
        background = Color.WHITE

        comboBox.addItem(LEVEL1_TEXT)
        comboBox.addItem(LEVEL2_TEXT)
        comboBox.addItem(LEVEL3_TEXT)
        comboBox.isEditable = false
        comboBox.addActionListener(this)

        add(comboBox)
    }

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }

    override fun actionPerformed(event: ActionEvent) {
        when (comboBox.selectedItem) {
            LEVEL1_TEXT -> player.setSearchTreePruner(level1Pruner)
            LEVEL2_TEXT -> player.setSearchTreePruner(level2Pruner)
            LEVEL3_TEXT -> player.setSearchTreePruner(level3Pruner)
        }
    }

    companion object {
        private const val LEVEL1_TEXT = "level 1"
        private const val LEVEL2_TEXT = "level 2"
        private const val LEVEL3_TEXT = "level 3"
    }
}
