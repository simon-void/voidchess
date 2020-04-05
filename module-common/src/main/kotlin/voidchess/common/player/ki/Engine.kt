package voidchess.common.player.ki

import voidchess.common.board.StartConfig
import voidchess.common.player.ki.evaluation.EvaluatedMove

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
    fun evaluateMovesBestMoveFirst(movesSoFar: List<String>, startConfig: StartConfig): EngineAnswer
}

sealed class EngineAnswer {
    class Success(val evaluatedMove: EvaluatedMove): EngineAnswer()
    class Error(val errorMsg: String): EngineAnswer()
}

typealias ProgressCallback = (Int, Int) -> Unit
