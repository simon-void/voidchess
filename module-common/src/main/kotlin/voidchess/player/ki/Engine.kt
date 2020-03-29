package voidchess.player.ki

import voidchess.board.StartConfig
import voidchess.player.ki.evaluation.EvaluatedMove

interface Option {
    val name: String
    val possibleValues: List<String>
    val currentValue: String
}

data class EngineSpec(
    val name: String,
    val coresToUseOption: Option,
    val difficultyOption: Option,
    val supportsChess960: Boolean
)

interface Engine {
    fun getSpec(): EngineSpec
    fun setOption(name: String, value: String)
    fun evaluateMovesBestMoveFirst(movesSoFar: List<String>, startConfig: StartConfig): EvaluatedMove
}

typealias ProgressCallback = (Int, Int) -> Unit
