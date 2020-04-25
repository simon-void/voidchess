package voidchess.engine.evaluation

import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.figures.King
import voidchess.common.player.ki.evaluation.*
import voidchess.engine.board.EngineChessGame
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.engine.evaluation.leaf.StaticEval


internal class MinMaxEval(
    private var pruner: SearchTreePruner,
    private var strategy: StaticEval
) {
    constructor() : this(PrunerWithIrreversibleMoves(),
        MiddleGameEval
    )

    fun evaluateMove(
        game: EngineChessGame,
        move: Move,
        currentMaxOneLevelUp: Evaluation?,
        movesToTryFirst: BestResponseSet
    ): Pair<Move?, Evaluation> {
        val depth = 0
        val forWhite = game.isWhiteTurn

        return game.withMove(move) { moveResult ->
            when (moveResult) {
                MoveResult.NO_END -> {
                    val thisMoveHasHitFigure = game.hasHitFigure
                    val thisMoveIsChess = game.isCheck

                    getMin(
                        game,
                        forWhite,
                        depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure,
                        game.getAllMoves(),
                        currentMaxOneLevelUp,
                        movesToTryFirst
                    )
                }
                MoveResult.CHECKMATE -> null to CheckmateOther(0)
                MoveResult.THREE_TIMES_SAME_POSITION -> null to ThreeFoldRepetition
                MoveResult.STALEMATE -> null to Stalemate
                else -> null to Draw
            }
        }
    }

    private fun getMin(
        game: EngineChessGame,
        forWhite: Boolean,
        depth: Int,
        lastMove_isChess: Boolean,
        lastMove_hasHitFigure: Boolean,
        minPossibleMovesBuffer: ArrayList<Move>,
        currentMaxOneLevelUp: Evaluation?,
        movesToTryFirst: BestResponseSet
    ): Pair<Move, Evaluation> {
        assert(minPossibleMovesBuffer.isNotEmpty()) {
            "minPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }

        var currentMinEvaluation: Evaluation? = null
        var currentBestMove: Move? = null
        val bestResponses = BestResponseSet()

        for (move in minPossibleMovesBuffer.shuffle(movesToTryFirst)) {

            assert(game.isFreeArea(move.to) || game.getFigure(move.to) !is King) {
                "getMin: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val latestEvaluation: Evaluation = game.withMove(move) { moveResult ->
                when (moveResult) {
                    MoveResult.NO_END -> {
                        val newDepth = depth + 1

                        val thisMoveHasHitFigure = game.hasHitFigure
                        val thisMoveIsChess = game.isCheck

                        val maxPossibleMovesBuffer =
                            when (pruner.continueMaxDynamicEvaluationBy(
                                newDepth,
                                thisMoveIsChess,
                                thisMoveHasHitFigure,
                                lastMove_isChess,
                                lastMove_hasHitFigure
                            )) {
                                ContinueEvalBy.StaticEval -> emptyMoveList
                                ContinueEvalBy.AllMoves -> game.getAllMoves()
                                ContinueEvalBy.IrreversibleMoves -> game.getCriticalMoves()
                                ContinueEvalBy.TakingMoves -> game.getTakingMoves()
                            }

                        if (maxPossibleMovesBuffer.isEmpty()) {
                            strategy.getNumericEvaluation(game, forWhite, forWhite)
                        } else {
                            val (bestResponse, eval) = getMax(
                                game,
                                forWhite,
                                newDepth,
                                thisMoveIsChess,
                                thisMoveHasHitFigure,
                                maxPossibleMovesBuffer,
                                currentMinEvaluation,
                                bestResponses
                            )
                            bestResponses.add(bestResponse)
                            eval
                        }
                    }
                    MoveResult.CHECKMATE -> {
                        stopLookingForBetterMove = true
                        val secondaryMateEval = strategy.getSecondaryCheckmateEvaluation(game, forWhite, forWhite)
                        CheckmateSelf(
                            depth + 1,
                            secondaryMateEval
                        )
                    }
                    MoveResult.THREE_TIMES_SAME_POSITION -> ThreeFoldRepetition
                    MoveResult.STALEMATE -> Stalemate
                    else -> Draw
                }
            }

            if (currentMinEvaluation == null || latestEvaluation < currentMinEvaluation) {
                currentMinEvaluation = latestEvaluation
                currentBestMove = move

                // Alpha-Beta Pruning
                if (currentMaxOneLevelUp != null && latestEvaluation <= currentMaxOneLevelUp) {
                    stopLookingForBetterMove = true
                }
            }

            if (stopLookingForBetterMove) break
        }

        return currentMinEvaluation?.let { eval -> currentBestMove?.let { move -> move to eval } }
            ?: throw IllegalStateException(
                "minPossibleMovesBuffer must have been empty! game: $game, latest moves: ${game.shortTermHistory}"
            )
    }

    private fun getMax(
        game: EngineChessGame,
        forWhite: Boolean,
        depth: Int,
        lastMoveIsChess: Boolean,
        lastMoveHasHitFigure: Boolean,
        maxPossibleMovesBuffer: ArrayList<Move>,
        currentMinOneLevelUp: Evaluation?,
        movesToTryFirst: BestResponseSet
    ): Pair<Move, Evaluation> {
        assert(maxPossibleMovesBuffer.isNotEmpty()) {
            "maxPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }

        var currentMaxEvaluation: Evaluation? = null
        var currentBestMove: Move? = null
        val bestResponses = BestResponseSet()

        for (move in maxPossibleMovesBuffer.shuffle(movesToTryFirst)) {

            assert(game.isFreeArea(move.to) || game.getFigure(move.to) !is King) {
                "getMax: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val latestEvaluation: Evaluation = game.withMove(move) { moveResult ->
                when (moveResult) {
                    MoveResult.NO_END -> {
                        val thisMoveHasHitFigure = game.hasHitFigure
                        val thisMoveIsChess = game.isCheck

                        val minPossibleMovesBuffer =
                            when (pruner.continueMinDynamicEvaluationBy(
                                depth,
                                thisMoveIsChess,
                                thisMoveHasHitFigure,
                                lastMoveIsChess,
                                lastMoveHasHitFigure
                            )) {
                                ContinueEvalBy.StaticEval -> emptyMoveList
                                ContinueEvalBy.AllMoves -> game.getAllMoves()
                                ContinueEvalBy.IrreversibleMoves -> game.getCriticalMoves()
                                ContinueEvalBy.TakingMoves -> game.getTakingMoves()
                            }

                        if (minPossibleMovesBuffer.isEmpty()) {
                            strategy.getNumericEvaluation(game, forWhite, !forWhite)
                        } else {
                            val (bestResponse, eval) = getMin(
                                game,
                                forWhite,
                                depth,
                                thisMoveIsChess,
                                thisMoveHasHitFigure,
                                minPossibleMovesBuffer,
                                currentMaxEvaluation,
                                bestResponses
                            )
                            bestResponses.add(bestResponse)
                            eval
                        }
                    }
                    MoveResult.CHECKMATE -> {
                        stopLookingForBetterMove = true
                        CheckmateOther(depth)
                    }
                    MoveResult.THREE_TIMES_SAME_POSITION -> ThreeFoldRepetition
                    MoveResult.STALEMATE -> Stalemate
                    else -> Draw
                }
            }

            if (currentMaxEvaluation == null || latestEvaluation > currentMaxEvaluation) {
                currentMaxEvaluation = latestEvaluation
                currentBestMove = move

                // Alpha-Beta Pruning
                if (currentMinOneLevelUp != null && latestEvaluation >= currentMinOneLevelUp) {
                    stopLookingForBetterMove = true
                }
            }

            if (stopLookingForBetterMove) break
        }

        return currentMaxEvaluation?.let { eval -> currentBestMove?.let { move -> move to eval } }
            ?: throw IllegalStateException(
                "maxPossibleMovesBuffer must have been empty! game: $game, latest moves: ${game.shortTermHistory}"
            )
    }
}

private val emptyMoveList: ArrayList<Move> = ArrayList(0)

// this makes Alpha-Beta-Pruning 10-25% faster (due to increasing the chance of finding good moves earlier)
private fun ArrayList<Move>.shuffle(movesToTryFirst: BestResponseSet): ArrayList<Move> {
    val maxIndex = this.size - 1
    if (maxIndex > 2) {
        for (i in 0 until (maxIndex / 2) - 1 step 2) {
            val temp = this[i]
            val inverseI = maxIndex - i
            this[i] = this[inverseI]
            this[inverseI] = temp
        }
    }
    var indexToReplace = 0
    movesToTryFirst.forEach { move ->
        val indexOfMoveToTryFirst = this.indexOf(move)
        if (indexOfMoveToTryFirst > indexToReplace) {
            val temp = this[indexToReplace]
            this[indexToReplace] = this[indexOfMoveToTryFirst]
            this[indexOfMoveToTryFirst] = temp
            indexToReplace++
        }
    }
    return this
}