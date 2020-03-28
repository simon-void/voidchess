package voidchess.player.ki

import voidchess.board.ChessGame
import voidchess.board.StartConfig
import voidchess.board.move.Move
import voidchess.player.ki.concurrent.ConcurrencyStrategy
import voidchess.player.ki.concurrent.MultiThreadStrategy
import voidchess.player.ki.concurrent.SingleThreadStrategy
import voidchess.player.ki.evaluation.*
import voidchess.player.ki.openings.OpeningsLibrary
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

typealias ProgressCallback = (Int, Int) -> Unit

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

class Engine(private val progressCallback: ProgressCallback) {

    private val concurrencyStrategyCache = ConcurrencyStrategyContainer(progressCallback)
    private val openingsLibrary = OpeningsLibrary("openings.txt")
    private val staticEvaluation: EvaluatingStatically = EvaluatingAsIsNow

    fun getSpec(): EngineSpec = EngineSpec(
        name = "Kai",
        coresToUseOption = CoresToUseOption,
        difficultyOption = DifficultyOption,
        supportsChess960 = true
    )

    fun setOption(name: String, value: String) {
        when(name) {
            CoresToUseOption.name -> CoresToUseOption.setCoresToUse(value)
            DifficultyOption.name -> DifficultyOption.setDifficulty(value)
            else -> throw IllegalArgumentException("unknown option name: $name")
        }
    }

    fun evaluateMovesBestMoveFirst(movesSoFar: List<String>, startConfig: StartConfig): EvaluatedMove {
        val chess960StartIndex = when(startConfig) {
            is StartConfig.ClassicConfig -> startConfig.chess960Index
            is StartConfig.Chess960Config -> startConfig.chess960Index
            is StartConfig.ManualConfig -> throw IllegalStateException("can't copy game from manual starting config")
        }
        val game = ChessGame(
            initialPosition = chess960StartIndex,
            moves = *movesSoFar.toTypedArray()
        )

        //display that the computer is working
        progressCallback(0, 1)

        val pruner = DifficultyOption.pruner
        val coresToUse = CoresToUseOption.coresToUse
        val evaluatingMinMax = EvaluatingMinMax(pruner, staticEvaluation)

        return lookUpNextMove(game, evaluatingMinMax, movesSoFar, chess960StartIndex)
            ?: concurrencyStrategyCache.get(coresToUse).evaluateMovesBestMoveFirst(game, evaluatingMinMax).pickOkMove()
    }

    private fun lookUpNextMove(
        game: ChessGame,
        evaluatingMinMax: EvaluatingMinMax,
        movesSoFar: List<String>,
        chess960StartIndex: Int
    ): EvaluatedMove? {
        fun wait(milliseconds: Int) { runCatching { Thread.sleep(milliseconds.toLong()) } }

        if (movesSoFar.size<openingsLibrary.maxDepth) {
            return openingsLibrary.nextMove(movesSoFar, chess960StartIndex)?.let { libraryMove ->
                concurrencyStrategyCache.get(1).evaluateMove(game, libraryMove, evaluatingMinMax)
            }
        }
        return null
    }

    /**
     * pick the move to make given an evaluated Set of possible moves.
     * (Taking always the best move would lead to a limited number of openings!)
     * @return the move the ki will make next
     */
    private fun List<EvaluatedMove>.pickOkMove(): EvaluatedMove {
        val sortedEvaluatedMoves = this
        val bestMove: EvaluatedMove = sortedEvaluatedMoves.first()

        if (bestMove.value !is NumericalEvaluation) {
            return bestMove
        }

        fun pickEvaluateMoveBy(move: Move) = sortedEvaluatedMoves.first { it.move.equalsMove(move) }

        val okDistanceToBest = .2

        // the weight lies between (0-1]
        // with bestMove will have a weight of 1
        // and a move that is almost okDistanceToBest apart will have a weight of almost 0
        val moveAndLinearWeight: List<Pair<Move, Double>> = LinkedList<Pair<Move, Double>>().apply {
            val bestFullEvaluation = bestMove.value.fullEvaluation
            for((move, evaluation) in sortedEvaluatedMoves) {
                if(evaluation !is NumericalEvaluation) break
                val distanceToBest = bestFullEvaluation-evaluation.fullEvaluation
                if(distanceToBest>=okDistanceToBest) break
                add(Pair(move, (okDistanceToBest-distanceToBest)/okDistanceToBest))
            }
        }

        require(moveAndLinearWeight.isNotEmpty())
        if(moveAndLinearWeight.size==1) {
            return pickEvaluateMoveBy(moveAndLinearWeight.first().first)
        }

        // make it more than linear probable to pick a better move (the bigger the factor, the more preferable better solutions are)
        val moveAndWeight = moveAndLinearWeight.map { Pair(it.first, it.second.pow(1.8)) }
        val weightSum = moveAndWeight.map { it.second }.sum()
        // the sum of all percentages will be 1.0 (or close to it because of rounding errors)
        val moveAndPercentage = moveAndWeight.map { Pair(it.first, it.second/weightSum) }

        var randomValueInbetween0and1 = Random.nextDouble()
        val moveAndPercentageIter = moveAndPercentage.iterator()
        var moveWithPercentage = moveAndPercentageIter.next()
        while(moveAndPercentageIter.hasNext() && randomValueInbetween0and1>moveWithPercentage.second) {
            randomValueInbetween0and1 -= moveWithPercentage.second
            moveWithPercentage = moveAndPercentageIter.next()
        }

        return pickEvaluateMoveBy(moveWithPercentage.first)
    }
}


private class ConcurrencyStrategyContainer(private val progressCallback: ProgressCallback) {
    private var singleThreadStrategy = SingleThreadStrategy(progressCallback)
    private var coresAndStrategy: Pair<Int, ConcurrencyStrategy> =
        CoresToUseOption.coresToUse.let { it to MultiThreadStrategy(progressCallback, it) }

    fun get(numberOfCoresToUse: Int): ConcurrencyStrategy {
        if(numberOfCoresToUse==1) return singleThreadStrategy
        if( numberOfCoresToUse!=coresAndStrategy.first) {
            val oldStrategy = coresAndStrategy.second
            coresAndStrategy = numberOfCoresToUse to MultiThreadStrategy(progressCallback, numberOfCoresToUse)
            oldStrategy.shutdown()
        }
        return coresAndStrategy.second
    }
}

private object CoresToUseOption : Option {
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
    override var currentValue: String = possibleValues.let {values->
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

private object DifficultyOption: Option {
    override val name: String = "Difficulty"
    override val possibleValues: List<String> get() = listOf("level 1", "level 2")
    override var currentValue: String = possibleValues.first()

    fun setDifficulty(value: String) {
        if (!possibleValues.contains(value)) {
            throw IllegalArgumentException("unknown option for option $name, possible vaulues ${possibleValues.joinToString()} but was: $value")
        }
        currentValue = value
    }

    val pruner: SearchTreePruner get() = when(currentValue) {
        "level 1" -> AllMovesOrNonePruner(1, 4, 3)
        "level 2" -> PrunerWithIrreversibleMoves(1, 2, 4, 3)
        else -> throw IllegalStateException("unknown current Difficulty value: $currentValue")
    }
}
