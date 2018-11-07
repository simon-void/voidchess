package voidchess.player.ki.evaluation

import voidchess.board.ChessGameInterface
import voidchess.board.getFigure
import voidchess.board.move.Move
import voidchess.board.move.MoveResult
import java.util.*
import java.util.Collections.emptyList

class DynamicEvaluation(var pruner: SearchTreePruner, var strategy: StaticEvaluationInterface) {

    constructor() : this(PrunerWithIrreversibleMoves(), StaticEvaluation)

    fun evaluateMove(game: ChessGameInterface, move: Move): Evaluated {
        val depth = 0
        val forWhite = game.isWhiteTurn
        val thisMoveHasHitFigure = game.hasHitFigure()
        val thisMoveIsChess = game.isCheck(!forWhite)

        val interactiveSupervisor = game.suspendInteractiveSupervisor()
        val endOption = game.move(move)

        val result = when {
            endOption === MoveResult.NO_END -> {
                getMin(game, forWhite, depth, thisMoveIsChess, thisMoveHasHitFigure)
            }
            endOption === MoveResult.CHECKMATE -> CheckmateOther(depth + 1)
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
                       lastMove_hasHitFigure: Boolean
    ): Evaluated {
        val thisMoveHasHitFigure = game.hasHitFigure()
        val thisMoveIsChess = game.isCheck(!forWhite)

        val minPossibleMovesBuffer =
                when (pruner.continueMinDynamicEvaluationBy(depth,
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

        if(minPossibleMovesBuffer.isEmpty()) {
            return strategy.getPrimaryEvaluation(game, forWhite)
        }

        val primaryEvaluations = TreeSet<EvaluatedMove>()

        for (move in minPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMin: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            val endOption = game.move(move)

            val primaryEval = when {
                endOption === MoveResult.NO_END -> getMax(
                        game,
                        forWhite,
                        depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure
                )
                endOption === MoveResult.CHECKMATE -> {
                    game.undo()
                    return CheckmateSelf(depth + 1)
                }
                else -> Draw
            }

            primaryEvaluations.add(EvaluatedMove(move, primaryEval))

            game.undo()
        }

        //now byCode a secondaryEvaluation for the Moves with the smallest primary Evaluation
        val combinedEvaluations = TreeSet<Evaluated>()
        val minPrimary = primaryEvaluations.first().value

        for (evaluatedMove in primaryEvaluations) {
            val primaryEval = evaluatedMove.value
            if (primaryEval.isCloseToByPrimary(minPrimary)) {
                game.move(evaluatedMove.move)
                //add secondary Evaluation
                strategy.addSecondaryEvaluation(game, forWhite, primaryEval)
                combinedEvaluations.add(primaryEval)
                game.undo()
            } else {
                //the set is sorted so once an element is to big, all the rest are
                break
            }
        }

        //minValue
        return combinedEvaluations.first()
    }

    private fun getMax(game: ChessGameInterface,
                       forWhite: Boolean,
                       oldDepth: Int,
                       lastMove_isChess: Boolean,
                       lastMove_hasHitFigure: Boolean
    ): Evaluated {
        val depth = oldDepth + 1

        val thisMoveHasHitFigure = game.hasHitFigure()
        val thisMoveIsChess = game.isCheck(forWhite)

        val maxPossibleMovesBuffer =
                when (pruner.continueMaxDynamicEvaluationBy(depth,
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

        if(maxPossibleMovesBuffer.isEmpty()) {
            return strategy.getPrimaryEvaluation(game, forWhite)
        }

        val primaryEvaluations = TreeSet<EvaluatedMove>()

        for (move in maxPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to).isKing()) {
                "getMax: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            val endOption = game.move(move)

            val primaryEval = when {
                endOption === MoveResult.NO_END -> getMin(
                        game,
                        forWhite,
                        depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure
                )
                endOption === MoveResult.CHECKMATE -> {
                    game.undo()
                    return CheckmateOther(depth + 1)
                }
                else -> Draw
            }

            primaryEvaluations.add(EvaluatedMove(move, primaryEval))

            game.undo()
        }

        //now byCode a secondaryEvaluation for the Moves with the smallest primary Evaluation
        val combinedEvaluations = TreeSet<Evaluated>()
        val maxPrimary = primaryEvaluations.last().value

        for (evaluatedMove in primaryEvaluations.descendingSet()) {
            val primaryEval = evaluatedMove.value
            if (primaryEval.isCloseToByPrimary(maxPrimary)) {
                game.move(evaluatedMove.move)
                //add secondary Evaluation
                strategy.addSecondaryEvaluation(game, forWhite, primaryEval)
                combinedEvaluations.add(primaryEval)
                game.undo()
            } else {
                //the set is sorted in reverse so once an element is to small, all the rest are
                break
            }
        }

        //maxValue
        return combinedEvaluations.last()
    }

    companion object {
        private val emptyMoveList: List<Move> = emptyList<Move>()
    }
}