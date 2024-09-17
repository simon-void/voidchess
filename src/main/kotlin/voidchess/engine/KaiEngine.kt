package voidchess.engine

import voidchess.common.board.BasicChessGame
import voidchess.common.board.BasicChessGameImpl
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.engine.*
import voidchess.engine.board.EngineChessGame
import voidchess.engine.board.EngineChessGameImpl
import voidchess.engine.concurrent.MultiThreadStrategy
import voidchess.engine.evaluation.*
import voidchess.engine.evaluation.leaf.KingToCornerEndgameEval
import voidchess.engine.evaluation.leaf.KingToEdgeEndgameEval
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.engine.evaluation.leaf.StaticEval
import voidchess.engine.evaluation.leaf.getInventory
import voidchess.engine.openings.OpeningsLibrary
import kotlin.math.pow
import kotlin.random.Random


class KaiEngine(private val progressCallback: ProgressCallback): Engine {

    private val openingsLibrary: OpeningsLibrary = OpeningsLibrary.loadFromFile("openings.txt")

    override fun getConfig(): EngineConfig {
        val kaiEngine = this
        return object : EngineConfig {
            override fun getSpec() = EngineSpec(
                name = "Kai",
                coresToUseOption = CoresToUseOption,
                difficultyOption = DifficultyOption,
                supportsChess960 = true
            )

            override fun setOption(name: String, value: String) {
                kaiEngine.setOption(name, value)
            }
        }
    }

    private fun setOption(name: String, value: String) {
        when(name) {
            CoresToUseOption.name -> CoresToUseOption.setCoresToUse(
                value
            )
            DifficultyOption.name -> DifficultyOption.setDifficulty(
                value
            )
            else -> throw IllegalArgumentException("unknown option name: $name")
        }
    }

    override suspend fun evaluateMovesBestMoveFirst(movesSoFar: List<String>, startConfig: StartConfig): EngineAnswer = try {
        val moves = movesSoFar.map { Move.byCode(it) }
        validateMovesOrReturnErrorMsg(moves, startConfig)?.let { validatorErrorMsg->
            return EngineAnswer.Error(validatorErrorMsg)
        }

        val evaluatedMove = openingsLibrary.lookUpNextMove(startConfig, moves, progressCallback)
            ?: computeNextMove(startConfig, moves)

        EngineAnswer.Success(evaluatedMove)
    }catch (e: Exception) {
        EngineAnswer.Error(e.toString())
    }

    private suspend fun computeNextMove(startConfig: StartConfig, movesSoFar: List<Move>): EvaluatedMove {
        val game = EngineChessGameImpl(startConfig, movesSoFar)

        val pruner: SearchTreePruner
        val staticEval: StaticEval
        val okDistance: Double

        when(game.getEndgameOption()) {
            EndgameOption.OneSidedWithQueen -> {
                pruner  = DefaultPruner(2, 2, 3)
                staticEval = KingToEdgeEndgameEval
                okDistance = 0.0
            }
            EndgameOption.OneSidedWithRook -> {
                pruner  = DefaultPruner(3, 3, 3)
                staticEval = KingToEdgeEndgameEval
                okDistance = 0.0
            }
            EndgameOption.OneSidedWithOnlyBishopsAndOrKnights -> {
                pruner  = DefaultPruner(3, 3, 4)
                staticEval = KingToCornerEndgameEval
                okDistance = 0.0
            }
            EndgameOption.OnlyPawns -> {
                val defaultAllMoveRadius = DifficultyOption.pruner.allMoveRadius
                // PrunerWithPawnMoves makes still sense, because due to pawn promotion
                pruner  = PrunerWithPawnMoves(defaultAllMoveRadius + 1, defaultAllMoveRadius + 3, defaultAllMoveRadius + 3, defaultAllMoveRadius + 3)
                staticEval = MiddleGameEval
                okDistance = 0.0
            }
            EndgameOption.PawnsAndMaxOneFigure -> {
                val defaultAllMoveRadius = DifficultyOption.pruner.allMoveRadius
                pruner  = PrunerWithPawnMoves(defaultAllMoveRadius + 1, defaultAllMoveRadius + 2, defaultAllMoveRadius + 3, defaultAllMoveRadius + 2)
                staticEval = MiddleGameEval
                okDistance = okDistanceToBest/2
            }
            else -> {
                pruner  = DifficultyOption.pruner
                staticEval = MiddleGameEval
                okDistance = okDistanceToBest
            }
        }

        return MultiThreadStrategy.evaluateMovesBestMoveFirst(
            game, MinMaxEval(pruner, staticEval), CoresToUseOption.coresToUse, okDistance, progressCallback
            ).pickOkMove()
    }

    /**
     * pick the move to make given an evaluated Set of possible moves.
     * (Taking always the best move would lead to a limited number of openings!)
     * @return the move the ki will make next
     */
    private fun List<EvaluatedMove>.pickOkMove(): EvaluatedMove {
        val sortedEvaluatedMoves = this
        val bestEval: NumericalEvaluation = sortedEvaluatedMoves.first().let { bestEvaluatedMove ->
            if (bestEvaluatedMove.value !is NumericalEvaluation) {
                return bestEvaluatedMove
            }
            bestEvaluatedMove.value as NumericalEvaluation
        }



        fun pickEvaluateMoveBy(move: Move) = sortedEvaluatedMoves.first { it.move.equalsMove(move) }

        // the weight lies between (0-1]
        // with bestMove will have a weight of 1
        // and a move that is almost okDistanceToBest apart will have a weight of almost 0
        val moveAndLinearWeight: List<Pair<Move, Double>> = mutableListOf<Pair<Move, Double>>().apply {
            val bestFullEvaluation = bestEval.numericValue
            for((move, evaluation) in sortedEvaluatedMoves) {
                if(evaluation !is NumericalEvaluation) break
                val distanceToBest = bestFullEvaluation-evaluation.numericValue
                if(distanceToBest>= okDistanceToBest) break
                add(move to (okDistanceToBest -distanceToBest)/ okDistanceToBest)
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

    private fun validateMovesOrReturnErrorMsg(movesSoFar: List<Move>, startConfig: StartConfig): String? {
        val basicGame: BasicChessGame = BasicChessGameImpl(startConfig)
        val movesApplied = mutableListOf<Move>()
        for(move in movesSoFar) {
            if(!basicGame.isMovable(move.from, move.to)) {
                return "$move is illegal with $startConfig after moves: ${movesApplied.joinToString()}"
            }
            basicGame.move(move)
            movesApplied.add(move)
        }
        return null
    }

    companion object {
        const val okDistanceToBest = .2
    }
}

internal enum class EndgameOption {
    // TODO add more endgame options
    No,
    PawnsAndMaxOneFigure,
    OnlyPawns,
    OneSidedWithQueen,
    OneSidedWithRook,
    OneSidedWithOnlyBishopsAndOrKnights,
    OneSidedWithOnlyPawnsAndBishopsAndOrKnights;
}

internal fun EngineChessGame.getEndgameOption(): EndgameOption {
    val inventory = this.getInventory()
    return when {
        inventory.areOnlyPawnsLeft -> EndgameOption.OnlyPawns
        inventory.hasOneSideOnlyKingLeft -> {
            when {
                inventory.isQueenLeft -> EndgameOption.OneSidedWithQueen
                inventory.isRookLeft -> EndgameOption.OneSidedWithRook
                inventory.arePawnsLeft -> EndgameOption.OneSidedWithOnlyPawnsAndBishopsAndOrKnights
                else -> EndgameOption.OneSidedWithOnlyBishopsAndOrKnights
            }
        }
        else -> EndgameOption.No
    }
}
