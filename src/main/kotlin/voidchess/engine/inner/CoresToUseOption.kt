package voidchess.engine.inner

import voidchess.common.engine.Option


internal data object CoresToUseOption : Option {
    override val name: String = "core#"
    override val possibleValues: List<String> = {
        Runtime.getRuntime().availableProcessors().let { maxCores ->
            if (maxCores == 1) {
                listOf(1)
            } else {
                listOf(
                    maxCores - 1,
                    maxCores
                )
            }
        }.map { it.toString() }
    }()
    override var currentValue: String = possibleValues.let { values->
        if(values.size==1) return@let values.first()
        // on a two core system we want to use both cores
        // if more cores are available we want to leave one core unoccupied by default
        val (firstValue: String, secondValue: String) = values
        return@let if(firstValue=="1") secondValue else firstValue
    }

    fun setCoresToUse(value: String) {
        if(!possibleValues.contains(value)) {
            throw IllegalArgumentException("unknown option for option $name, possible vaulues ${possibleValues.joinToString()} but was: $value")
        }
        currentValue = value
    }

    val coresToUse: Int get() = currentValue.toInt()
}
