package voidchess.ui

import voidchess.player.ComputerPlayer
import voidchess.player.ki.Option

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class CoresPanel constructor(private val player: ComputerPlayer) : JPanel(), ActionListener {

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
        comboBox.addActionListener(this)
        add(comboBox)
    }

    /**
     * @return true if more than one core is available
     */
    fun hasOptions() = comboBox.itemCount > 1

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }

    override fun actionPerformed(event: ActionEvent) {
        val numberOfCoresToUse = comboBox.selectedItem as String
        player.setOption(coresOption.name, numberOfCoresToUse)
    }
}
