package voidchess.engine.evaluation

import voidchess.common.board.getFigure
import voidchess.common.board.move.ExtendedMove
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResultType
import voidchess.common.engine.*
import voidchess.common.figures.King
import voidchess.engine.board.EngineChessGame
import voidchess.engine.evaluation.leaf.StaticEval


typealias EnsureNotCanceled = ()-> Unit
private val ensureNothing: EnsureNotCanceled = {}

internal class MinMaxEval(
    private var pruner: SearchTreePruner,
    private var strategy: StaticEval
) {
    fun evaluateMove(
            game: EngineChessGame,
            move: Move,
            currentMaxOneLevelUp: Evaluation?,
            movesToTryFirst: BestResponseSet,
            ensureNotCanceled: EnsureNotCanceled = ensureNothing
    ): Pair<Move?, Evaluation> {
        val depth = 0
        val forWhite = game.isWhiteTurn

        return game.withMove(move) { moveResult ->
            when (moveResult) {
                MoveResultType.NO_END -> getMin(
                        game,
                        forWhite,
                        depth,
                        game.isCheck,
                        game.latestExtendedMove,
                        game.getAllMoves(),
                        currentMaxOneLevelUp,
                        movesToTryFirst,
                        ensureNotCanceled
                )
                MoveResultType.CHECKMATE -> null to CheckmateOther(0)
                MoveResultType.THREE_TIMES_SAME_POSITION -> null to ThreeFoldRepetition
                MoveResultType.STALEMATE -> null to Stalemate
                else -> null to Draw
            }
        }
    }

    private fun getMin(
            game: EngineChessGame,
            forWhite: Boolean,
            depth: Int,
            lastMoveIsChess: Boolean,
            lastMoveExtendedMove: ExtendedMove,
            minPossibleMovesBuffer: ArrayList<Move>,
            currentMaxOneLevelUp: Evaluation?,
            movesToTryFirst: BestResponseSet,
            ensureNotCanceled: EnsureNotCanceled
    ): Pair<Move, Evaluation> {
        assert(minPossibleMovesBuffer.isNotEmpty()) {
            "minPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }
        ensureNotCanceled()

        var currentMinEvaluation: Evaluation? = null
        var currentBestMove: Move? = null
        val bestResponses = BestResponseSet.unsynced()
        val newDepth = depth + 1

        for (move in minPossibleMovesBuffer.shuffle(movesToTryFirst)) {

            assert(game.isFreeArea(move.to) || game.getFigure(move.to) !is King) {
                "getMin: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val latestEvaluation: Evaluation = game.withMove(move) { moveResult ->
                when (moveResult) {
                    MoveResultType.NO_END -> {
                        val thisMoveIsChess = game.isCheck
                        val thisMoveExtendedMove = game.latestExtendedMove

                        if (pruner.continueMaxDynamicEvaluationBy(
                                        newDepth,
                                        thisMoveIsChess,
                                        lastMoveIsChess,
                                        thisMoveExtendedMove,
                                        lastMoveExtendedMove
                                )) {
                            val maxPossibleMovesBuffer = game.getAllMoves()
                            val (bestResponse, eval) = getMax(
                                    game,
                                    forWhite,
                                    newDepth,
                                    thisMoveIsChess,
                                    thisMoveExtendedMove,
                                    maxPossibleMovesBuffer,
                                    currentMinEvaluation,
                                    bestResponses,
                                    ensureNotCanceled
                            )
                            bestResponses.add(bestResponse)
                            eval
                        } else {
                            strategy.getNumericEvaluation(game, forWhite, forWhite)
                        }
                    }
                    MoveResultType.CHECKMATE -> {
                        stopLookingForBetterMove = true
                        val secondaryMateEval = strategy.getSecondaryCheckmateEvaluation(game, forWhite, forWhite)
                        CheckmateSelf(
                                newDepth,
                                secondaryMateEval
                        )
                    }
                    MoveResultType.THREE_TIMES_SAME_POSITION -> ThreeFoldRepetition
                    MoveResultType.STALEMATE -> Stalemate
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
            lastMoveExtendedMove: ExtendedMove,
            maxPossibleMovesBuffer: ArrayList<Move>,
            currentMinOneLevelUp: Evaluation?,
            movesToTryFirst: BestResponseSet,
            ensureNotCanceled: EnsureNotCanceled
    ): Pair<Move, Evaluation> {
        assert(maxPossibleMovesBuffer.isNotEmpty()) {
            "maxPossibleMovesBuffer mustn't be empty. history: ${game.completeHistory}"
        }
        ensureNotCanceled()

        var currentMaxEvaluation: Evaluation? = null
        var currentBestMove: Move? = null
        val bestResponses = BestResponseSet.unsynced()

        for (move in maxPossibleMovesBuffer.shuffle(movesToTryFirst)) {

            assert(game.isFreeArea(move.to) || game.getFigure(move.to) !is King) {
                "getMax: ${game.getFigureOrNull(move.from)} hits King white Move $move"
            }

            var stopLookingForBetterMove = false

            val latestEvaluation: Evaluation = game.withMove(move) { moveResult ->
                when (moveResult) {
                    MoveResultType.NO_END -> {
                        val thisMoveIsChess = game.isCheck
                        val thisMoveExtendedMove = game.latestExtendedMove

                        if (pruner.continueMinDynamicEvaluationBy(
                                        depth,
                                        thisMoveIsChess,
                                        lastMoveIsChess,
                                        thisMoveExtendedMove,
                                        lastMoveExtendedMove
                                )) {
                            val minPossibleMovesBuffer = game.getAllMoves()
                            val (bestResponse, eval) = getMin(
                                    game,
                                    forWhite,
                                    depth,
                                    thisMoveIsChess,
                                    thisMoveExtendedMove,
                                    minPossibleMovesBuffer,
                                    currentMaxEvaluation,
                                    bestResponses,
                                    ensureNotCanceled
                            )
                            bestResponses.add(bestResponse)
                            eval
                        } else {
                            strategy.getNumericEvaluation(game, forWhite, !forWhite)
                        }
                    }
                    MoveResultType.CHECKMATE -> {
                        stopLookingForBetterMove = true
                        CheckmateOther(depth)
                    }
                    MoveResultType.THREE_TIMES_SAME_POSITION -> ThreeFoldRepetition
                    MoveResultType.STALEMATE -> Stalemate
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

// this makes Alpha-Beta-Pruning 10-25% faster (due to increasing the chance of finding good moves earlier)
private fun ArrayList<Move>.shuffle(movesToTryFirst: BestResponseSet): ArrayList<Move> {
    val maxIndex = this.size - 1
    if (maxIndex > 2) {
        for (i in 0 ..< (maxIndex / 2) - 1 step 2) {
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