package voidchess.ui.inner.swing

import voidchess.common.engine.EngineConfig
import voidchess.common.engine.Option

import javax.swing.*
import java.awt.*
import java.util.prefs.Preferences

internal class CoresPanel(
    private val engineConfig: EngineConfig
) : JPanel() {

    private val coresOption: Option = engineConfig.getSpec().coresToUseOption
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
            engineConfig.setOption(coresOption.name, numberOfCoresToUse)
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
