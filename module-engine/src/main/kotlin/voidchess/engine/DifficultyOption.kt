package voidchess.engine

import voidchess.common.engine.Option
import voidchess.engine.evaluation.DefaultPruner
import voidchess.engine.evaluation.PrunerWithPawnMoves
import voidchess.engine.evaluation.SearchTreePruner
import voidchess.engine.evaluation.SingleFullMovePrunerWithPawnMoves


internal object DifficultyOption: Option {
    private val levelsAndPruner: List<Pair<String, SearchTreePruner>> = listOf(
        "level 1" to SingleFullMovePrunerWithPawnMoves(3, 5, 3),
        "level 2" to PrunerWithPawnMoves(2, 3, 5, 4),
        "level 3" to DefaultPruner(3, 5, 5),
    )
    override val name: String = "Difficulty"
    override val possibleValues: List<String> get() = levelsAndPruner.map { it.first }
    override var currentValue: String = possibleValues.first()

    fun setDifficulty(value: String) {
        if (!possibleValues.contains(value)) {
            throw IllegalArgumentException("unknown option for option $name, possible vaulues ${possibleValues.joinToString()} but was: $value")
        }
        currentValue = value
    }

    val pruner: SearchTreePruner
        get() = levelsAndPruner.firstOrNull { it.first == currentValue }?.second
            ?: throw IllegalStateException("unknown current Difficulty value: $currentValue")
}
