package voidchess.common.engine

import voidchess.common.board.other.StartConfig

interface Option {
    val name: String
    val possibleValues: List<String>
    val currentValue: String
}

data class EngineSpec(
        val name: String,
        val coresToUseOption: Option,
        val difficultyOption: Option,
        val supportsChess960: Boolean,
)

interface EngineConfig {
    fun getSpec(): EngineSpec
    fun setOption(name: String, value: String)
}

interface Engine {
    fun getConfig(): EngineConfig
    suspend fun evaluateMovesBestMoveFirst(movesSoFar: List<String>, startConfig: StartConfig): EngineAnswer
}

sealed class EngineAnswer {
    class Success(val evaluatedMove: EvaluatedMove): EngineAnswer()
    class Error(val errorMsg: String): EngineAnswer()
}

typealias ProgressCallback = suspend (Int, Int) -> Unit
