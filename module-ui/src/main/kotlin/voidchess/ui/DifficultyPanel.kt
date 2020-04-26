package voidchess.ui

import voidchess.player.ComputerPlayer
import voidchess.common.player.ki.Option
import java.awt.Color
import java.awt.FlowLayout
import java.util.prefs.Preferences
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

        val preferences = Preferences.userNodeForPackage(DifficultyPanel::class.java)
        val difficultyPrefKey = "difficultyOption"
        val selectedByDefault = comboBox.selectedItem as String

        add(comboBox)
        comboBox.addActionListener {
            val selected = comboBox.selectedItem as String
            player.setOption(difficultyOption.name, selected)
            preferences.put(difficultyPrefKey, selected)
        }

        val savedOption: String = preferences.get(difficultyPrefKey, comboBox.selectedItem as String)
        if(savedOption!=selectedByDefault && difficultyOption.possibleValues.contains(savedOption)) {
            comboBox.selectedItem = savedOption
        }
    }

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }
}
