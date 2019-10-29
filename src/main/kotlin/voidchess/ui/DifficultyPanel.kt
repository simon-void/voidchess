package voidchess.ui

import voidchess.player.ki.ComputerPlayer
import voidchess.player.ki.evaluation.AllMovesOrNonePruner
import voidchess.player.ki.evaluation.PrunerWithIrreversibleMoves
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DifficultyPanel internal constructor(private val player: ComputerPlayer) : JPanel() {

    private val namedPruners = sortedMapOf(
            "level 1" to AllMovesOrNonePruner(1, 4, 3),
            "level 2" to PrunerWithIrreversibleMoves(1, 2, 4, 3)
    )

    private val comboBox = JComboBox<String>().apply {
        namedPruners.keys.forEach {
            addItem(it)
        }
        isEditable = false
        selectedIndex = 0
    }

    val label = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
        background = Color.WHITE
        add(JLabel("difficulty:"))
    }

    init {
        background = Color.WHITE
        add(comboBox)
        comboBox.addActionListener {
            val selected = comboBox.selectedItem as String
            player.setSearchTreePruner(namedPruners.getValue(selected))
        }
        player.setSearchTreePruner(namedPruners.entries.first().value)
    }

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }
}
