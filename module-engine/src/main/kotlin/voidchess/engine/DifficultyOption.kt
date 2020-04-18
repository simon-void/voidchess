package voidchess.engine

import voidchess.common.player.ki.Option
import voidchess.engine.evaluation.AllMovesOrNonePruner
import voidchess.engine.evaluation.SearchTreePruner


internal object DifficultyOption: Option {
    private val levelsAndPruner: List<Pair<String, SearchTreePruner>> = listOf(
        "level 1" to AllMovesOrNonePruner(1, 5, 3),
//        "level 2" to PrunerWithIrreversibleMoves(1, 2, 4, 3),
        "level 2" to AllMovesOrNonePruner(2, 5, 3),
        "level 3" to AllMovesOrNonePruner(3, 5, 3)
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
