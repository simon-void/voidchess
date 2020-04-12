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

    fun evaluateMove(game: EngineChessGame, move: Move): Evaluation {
        val depth = 0
        val forWhite = game.isWhiteTurn

        return game.withMove(move) { moveResult ->
            when (moveResult) {
                MoveResult.NO_END -> {
                    val thisMoveHasHitFigure = game.hasHitFigure
                    val thisMoveIsChess = game.isCheck

                    getMin(game, forWhite, depth, thisMoveIsChess, thisMoveHasHitFigure, game.getAllMoves())
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
                       minPossibleMovesBuffer: Collection<Move>
    ): Evaluation {
        assert(minPossibleMovesBuffer.isNotEmpty()) {
            "minPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }

        var minEvaluation: Evaluation? = null

        for (move in minPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMin: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val currentEvaluation: Evaluation = game.withMove(move) {moveResult ->
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
                                maxPossibleMovesBuffer
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

            if(minEvaluation==null  || LowestEvaluationFirstComparator.compare(minEvaluation, currentEvaluation)>0) {
                minEvaluation = currentEvaluation
            }

            if(stopLookingForBetterMove) break
        }

        return minEvaluation
            ?: throw IllegalStateException(
                "minPossibleMovesBuffer must have been empty! game: $game, latest moves: ${game.shortTermHistory}"
            )
    }

    private fun getMax(game: EngineChessGame,
                       forWhite: Boolean,
                       depth: Int,
                       lastMoveIsChess: Boolean,
                       lastMoveHasHitFigure: Boolean,
                       maxPossibleMovesBuffer: Collection<Move>
    ): Evaluation {
        assert(maxPossibleMovesBuffer.isNotEmpty()) {
            "maxPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }

        var maxEvaluation: Evaluation? = null

        for (move in maxPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMax: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val currentEvaluation: Evaluation = game.withMove(move) {moveResult ->
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
                                minPossibleMovesBuffer
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

            if(maxEvaluation==null  || LowestEvaluationFirstComparator.compare(maxEvaluation, currentEvaluation)<0) {
                maxEvaluation = currentEvaluation
            }

            if(stopLookingForBetterMove) break
        }

        return maxEvaluation
            ?: throw IllegalStateException(
                "maxPossibleMovesBuffer must have been empty! game: $game, latest moves: ${game.shortTermHistory}"
            )
    }

    companion object {
        private val emptyMoveList: List<Move> = emptyList()
    }
}