package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.board.MoveResult
import voidchess.helper.Move
import voidchess.player.ki.evaluation.*

import java.util.*


class DynamicEvaluation(var pruner: SearchTreePruner, var strategy: StaticEvaluationInterface) {
    private val possibleMovesBufferSize = 40

    constructor() : this(SimplePruner(), StaticEvaluation) {}

    fun evaluateMove(game: ChessGameInterface, move: Move): Evaluated {
        val depth = 0
        val forWhite = game.isWhiteTurn
        val thisMoveHasHitFigure = game.hasHitFigure()
        val thisMoveIsChess = game.isCheck(!forWhite)

        val interactiveSupervisor = game.suspendInteractiveSupervisor()
        val endOption = game.move(move)

        val result = when {
            endOption === MoveResult.NO_END -> {
                val minPossibleMovesBuffer = ArrayList<Move>(possibleMovesBufferSize)
                getMin(game, forWhite, depth, thisMoveIsChess, thisMoveHasHitFigure, minPossibleMovesBuffer)
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
                       lastMove_hasHitFigure: Boolean,
                       minPossibleMovesBuffer: MutableList<Move>): Evaluated {
        val thisMoveHasHitFigure = game.hasHitFigure()
        val thisMoveIsChess = game.isCheck(!forWhite)

        if (pruner.stopMinDynamicEvaluation(depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure,
                        lastMove_isChess,
                        lastMove_hasHitFigure)) {
            return strategy.getPrimaryEvaluation(game, forWhite)
        }

        minPossibleMovesBuffer.clear()
        game.getPossibleMoves(minPossibleMovesBuffer)
        val primaryEvaluations = TreeSet<EvaluatedMove>()
        val maxPossibleMovesBuffer = ArrayList<Move>(possibleMovesBufferSize)

        for (move in minPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to)!!.isKing()) {
                "getMin: ${game.getFigure(move.from)} hits King white Move $move"
            }

            val endOption = game.move(move)

            val primaryEval = when {
                endOption === MoveResult.NO_END -> getMax(game,
                        forWhite,
                        depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure,
                        maxPossibleMovesBuffer)
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
                       lastMove_hasHitFigure: Boolean,
                       maxPossibleMovesBuffer: MutableList<Move>): Evaluated {
        val depth = oldDepth + 1

        val thisMoveHasHitFigure = game.hasHitFigure()
        val thisMoveIsChess = game.isCheck(forWhite)
        if (pruner.stopMaxDynamicEvaluation(depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure,
                        lastMove_isChess,
                        lastMove_hasHitFigure)) {
            return strategy.getPrimaryEvaluation(game, forWhite)
        }

        // TODO sort this out!
        val movesWithPrimaryEvaluation = TreeSet<EvaluatedMove>()

        maxPossibleMovesBuffer.clear()
        game.getPossibleMoves(maxPossibleMovesBuffer)
        val primaryEvaluations = TreeSet<EvaluatedMove>()
        val minPossibleMovesBuffer = ArrayList<Move>(possibleMovesBufferSize)

        for (move in maxPossibleMovesBuffer) {

            assert(game.isFreeArea(move.to) || !game.getFigure(move.to)!!.isKing()) {
                "getMax: ${game.getFigure(move.from)} hits King white Move $move"
            }

            val endOption = game.move(move)

            val primaryEval = when {
                endOption === MoveResult.NO_END -> getMin(game,
                        forWhite,
                        depth,
                        thisMoveIsChess,
                        thisMoveHasHitFigure,
                        minPossibleMovesBuffer)
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
}