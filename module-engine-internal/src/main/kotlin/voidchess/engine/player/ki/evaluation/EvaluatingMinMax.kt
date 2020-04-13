package voidchess.engine.player.ki.evaluation

import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.player.ki.evaluation.*
import voidchess.engine.board.EngineChessGame


internal class EvaluatingMinMax(
    private var pruner: SearchTreePruner,
    private var strategy: EvaluatingStatically
) {

    constructor() : this(PrunerWithIrreversibleMoves(), EvaluatingAsIsNow)

    // TODO make sure currentMaxOneLevelUp is provided one level up!!!
    fun evaluateMove(game: EngineChessGame, move: Move, currentMaxOneLevelUp: Evaluation?): Evaluation {
        val depth = 0
        val forWhite = game.isWhiteTurn

        return game.withMove(move) { moveResult ->
            when (moveResult) {
                MoveResult.NO_END -> {
                    val thisMoveHasHitFigure = game.hasHitFigure
                    val thisMoveIsChess = game.isCheck

                    getMin(game, forWhite, depth, thisMoveIsChess, thisMoveHasHitFigure, game.getAllMoves(), currentMaxOneLevelUp)
                }
                MoveResult.CHECKMATE -> CheckmateOther(depth + 1)
                MoveResult.THREE_TIMES_SAME_POSITION -> ThreeFoldRepetition
                MoveResult.STALEMATE -> Stalemate
                else -> Draw
            }
        }
    }

    private fun getMin(game: EngineChessGame,
                       forWhite: Boolean,
                       depth: Int,
                       lastMove_isChess: Boolean,
                       lastMove_hasHitFigure: Boolean,
                       minPossibleMovesBuffer: Collection<Move>,
                       currentMaxOneLevelUp: Evaluation?
    ): Evaluation {
        assert(minPossibleMovesBuffer.isNotEmpty()) {
            "minPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }

        var currentMinEvaluation: Evaluation? = null

        for (move in minPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMin: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val latestEvaluation: Evaluation = game.withMove(move) { moveResult ->
                when(moveResult) {
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
                            strategy.getNumericEvaluation(game, forWhite)
                        }else{
                            getMax(
                                game,
                                forWhite,
                                newDepth,
                                thisMoveIsChess,
                                thisMoveHasHitFigure,
                                maxPossibleMovesBuffer,
                                currentMinEvaluation
                            )
                        }
                    }
                    MoveResult.CHECKMATE -> {
                        stopLookingForBetterMove = true
                        val secondaryMateEval = strategy.getCheckmateMaterialEvaluation(game, forWhite)
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

                // Alpha-Beta Pruning
                if (currentMaxOneLevelUp != null && latestEvaluation <= currentMaxOneLevelUp) {
                    stopLookingForBetterMove = true
                }
            }

            if (stopLookingForBetterMove) break
        }

        return currentMinEvaluation
            ?: throw IllegalStateException(
                "minPossibleMovesBuffer must have been empty! game: $game, latest moves: ${game.shortTermHistory}"
            )
    }

    private fun getMax(game: EngineChessGame,
                       forWhite: Boolean,
                       depth: Int,
                       lastMoveIsChess: Boolean,
                       lastMoveHasHitFigure: Boolean,
                       maxPossibleMovesBuffer: Collection<Move>,
                       currentMinOneLevelUp: Evaluation?
    ): Evaluation {
        assert(maxPossibleMovesBuffer.isNotEmpty()) {
            "maxPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }

        var currentMaxEvaluation: Evaluation? = null

        for (move in maxPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMax: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val latestEvaluation: Evaluation = game.withMove(move) { moveResult ->
                when(moveResult) {
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
                            strategy.getNumericEvaluation(game, forWhite)
                        } else {
                            getMin(
                                game,
                                forWhite,
                                depth,
                                thisMoveIsChess,
                                thisMoveHasHitFigure,
                                minPossibleMovesBuffer,
                                currentMaxEvaluation
                            )
                        }
                    }
                    MoveResult.CHECKMATE -> {
                        stopLookingForBetterMove = true
                        CheckmateOther(depth + 1)
                    }
                    MoveResult.THREE_TIMES_SAME_POSITION -> ThreeFoldRepetition
                    MoveResult.STALEMATE -> Stalemate
                    else -> Draw
                }
            }

            if (currentMaxEvaluation == null || latestEvaluation > currentMaxEvaluation) {
                currentMaxEvaluation = latestEvaluation

                // Alpha-Beta Pruning
                if (currentMinOneLevelUp != null && latestEvaluation >= currentMinOneLevelUp) {
                    stopLookingForBetterMove = true
                }
            }

            if (stopLookingForBetterMove) break
        }

        return currentMaxEvaluation
            ?: throw IllegalStateException(
                "maxPossibleMovesBuffer must have been empty! game: $game, latest moves: ${game.shortTermHistory}"
            )
    }

    companion object {
        private val emptyMoveList: List<Move> = emptyList()
    }
}