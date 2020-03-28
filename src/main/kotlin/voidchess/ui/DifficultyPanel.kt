package voidchess.ui

import voidchess.player.ki.ComputerPlayer
import voidchess.player.ki.Option
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DifficultyPanel internal constructor(private val player: ComputerPlayer) : JPanel() {

    private val difficultyOption: Option = player.getEngineSpec().difficultyOption

    private val comboBox = JComboBox<String>().apply {
        difficultyOption.possibleValues.forEach {
            addItem(it)
        }
        selectedItem = difficultyOption.currentValue
        isEditable = false
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
            player.setOption(difficultyOption.name, selected)
        }
    }

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }
}
