package voidchess.ui

import voidchess.player.ComputerPlayer
import voidchess.common.player.ki.Option

import javax.swing.*
import java.awt.*
import java.util.prefs.Preferences

class CoresPanel constructor(private val player: ComputerPlayer) : JPanel() {

    private val coresOption: Option = player.getEngineSpec().coresToUseOption
    private val comboBox = JComboBox<String>().apply{
        coresOption.possibleValues.forEach {
            addItem(it)
        }
        selectedItem = coresOption.currentValue
        isEditable = false
    }

    val label: JPanel
        get() {
            val panel = JPanel(FlowLayout(FlowLayout.RIGHT))
            panel.background = Color.WHITE
            val label = JLabel("#cores:")
            panel.add(label)
            return panel
        }

    init {
        background = Color.WHITE

        val preferences = Preferences.userNodeForPackage(CoresPanel::class.java)
        val coresPrefKey = "coresOption"
        val selectedByDefault = comboBox.selectedItem as String

        add(comboBox)
        comboBox.addActionListener {
            val numberOfCoresToUse = comboBox.selectedItem as String
            player.setOption(coresOption.name, numberOfCoresToUse)
            preferences.put(coresPrefKey, numberOfCoresToUse)
        }

        val savedOption: String = preferences.get(coresPrefKey, comboBox.selectedItem as String)
        if(savedOption!=selectedByDefault && coresOption.possibleValues.contains(savedOption)) {
            comboBox.selectedItem = savedOption
        }
    }

    /**
     * @return true if more than one core is available
     */
    fun hasOptions() = comboBox.itemCount > 1

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }
}
