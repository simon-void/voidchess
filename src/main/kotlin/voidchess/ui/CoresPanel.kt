package voidchess.ui

import voidchess.player.ki.ComputerPlayer

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class CoresPanel constructor(private val player: ComputerPlayer) : JPanel(), ActionListener {

    private val comboBox: JComboBox<String> = JComboBox()

    val label: JPanel
        get() {
            val panel = JPanel(FlowLayout(FlowLayout.RIGHT))
            panel.background = Color.WHITE
            val label = JLabel("#cores:")
            panel.add(label)
            return panel
        }

    init {
        designLayout()
        //preselect the option of the maximum number of cores
        comboBox.selectedIndex = comboBox.itemCount - 1
    }

    /**
     * @return true if more than one core is available
     */
    fun hasOptions() = comboBox.itemCount > 1

    private fun designLayout() {
        background = Color.WHITE

        val numberOfCores = Runtime.getRuntime().availableProcessors()
        if (numberOfCores == 1) {
            comboBox.addItem("1")
        } else {
            comboBox.addItem((numberOfCores - 1).toString())
            comboBox.addItem(numberOfCores.toString())
        }
        comboBox.isEditable = false
        comboBox.addActionListener(this)

        add(comboBox)
    }

    override fun setEnabled(enable: Boolean) {
        comboBox.isEnabled = enable
    }

    override fun actionPerformed(event: ActionEvent) {
        val numberOfCoresToUse = comboBox.selectedItem!!.toString().toInt()
        player.setNumberOfCoresToUse(numberOfCoresToUse)
    }
}
