package voidchess.engine.player.ki.evaluation

import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.player.ki.evaluation.*
import voidchess.engine.board.ChessGameInterface
import java.util.Collections.emptyList


internal class EvaluatingMinMax(
    private var pruner: SearchTreePruner,
    private var strategy: EvaluatingStatically
) {

    constructor() : this(PrunerWithIrreversibleMoves(), EvaluatingAsIsNow)

    fun evaluateMove(game: ChessGameInterface, move: Move): Evaluation {
        val depth = 0
        val forWhite = game.isWhiteTurn

        val interactiveSupervisor = game.suspendInteractiveSupervisor()

        val result = when (game.move(move)) {
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

        game.useSupervisor(interactiveSupervisor)
        game.undo()
        return result
    }

    private fun getMin(game: ChessGameInterface,
                       forWhite: Boolean,
                       depth: Int,
                       lastMove_isChess: Boolean,
                       lastMove_hasHitFigure: Boolean,
                       minPossibleMovesBuffer: Collection<Move>
    ): Evaluation {
        assert(minPossibleMovesBuffer.isNotEmpty()) {
            "minPossibleMovesBuffer mustn't be empty. history: ${game.getCompleteHistory()}"
        }

        val minEvaluationSelector =
            Selector(HighestEvaluationFirstComparator)
        val minPrimaryEvaluations = MinPreliminaryEvaluatedMoves()

        for (move in minPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMin: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            when(game.move(move)) {
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
                        minPrimaryEvaluations.add(move, strategy.getPreliminaryEvaluation(game, forWhite))
                    }else{
                        minEvaluationSelector.propose(
                                getMax(
                                        game,
                                        forWhite,
                                        newDepth,
                                        thisMoveIsChess,
                                        thisMoveHasHitFigure,
                                        maxPossibleMovesBuffer
                                )
                        )
                    }

                }
                MoveResult.CHECKMATE -> {
                    val secondaryMateEval = strategy.getCheckmateMaterialEvaluation(game, forWhite)
                    minEvaluationSelector.propose(
                        CheckmateSelf(
                            depth + 1,
                            secondaryMateEval
                        )
                    )
                }
                MoveResult.THREE_TIMES_SAME_POSITION -> {
                    minEvaluationSelector.propose(ThreeFoldRepetition)
                }
                MoveResult.STALEMATE -> {
                    minEvaluationSelector.propose(Stalemate)
                }
                else -> {
                    minEvaluationSelector.propose(Draw)
                }
            }
            game.undo()
        }


        //minValue
        return minPrimaryEvaluations.getMin(game, forWhite, strategy, minEvaluationSelector)
    }

    private fun getMax(game: ChessGameInterface,
                       forWhite: Boolean,
                       depth: Int,
                       lastMoveIsChess: Boolean,
                       lastMoveHasHitFigure: Boolean,
                       maxPossibleMovesBuffer: Collection<Move>
    ): Evaluation {
        assert(maxPossibleMovesBuffer.isNotEmpty()) {
            "maxPossibleMovesBuffer mustn't be empty. history: ${game.getCompleteHistory()}"
        }

        val maxEvaluationSelector =
            Selector(LowestEvaluationFirstComparator)
        val maxPrimaryEvaluations = MaxPreliminaryEvaluatedMoves()

        for (move in maxPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMax: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            when(game.move(move)) {
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
                        maxPrimaryEvaluations.add(move, strategy.getPreliminaryEvaluation(game, forWhite))
                    } else {
                        maxEvaluationSelector.propose(
                                getMin(
                                        game,
                                        forWhite,
                                        depth,
                                        thisMoveIsChess,
                                        thisMoveHasHitFigure,
                                        minPossibleMovesBuffer
                                )
                        )
                    }
                }
                MoveResult.CHECKMATE -> {
                    game.undo()
                    return CheckmateOther(depth + 1)
                }
                MoveResult.THREE_TIMES_SAME_POSITION -> {
                    maxEvaluationSelector.propose(ThreeFoldRepetition)
                }
                MoveResult.STALEMATE -> {
                    maxEvaluationSelector.propose(Stalemate)
                }
                else -> {
                    maxEvaluationSelector.propose(Draw)
                }
            }
            game.undo()
        }

        //maxValue
        return maxPrimaryEvaluations.getMax(game, forWhite, strategy, maxEvaluationSelector)
    }

    companion object {
        private val emptyMoveList: List<Move> = emptyList()
    }
}