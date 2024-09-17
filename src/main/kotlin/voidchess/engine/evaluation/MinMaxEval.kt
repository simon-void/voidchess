package voidchess.engine.evaluation

import voidchess.common.board.StaticChessBoard
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

        for (move in minPossibleMovesBuffer.shuffle(movesToTryFirst, game)) {

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

        for (move in maxPossibleMovesBuffer.shuffle(movesToTryFirst, game)) {

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
private fun ArrayList<Move>.shuffle(movesToTryFirst: BestResponseSet, board: StaticChessBoard): ArrayList<Move> = this.apply{
    val movesAndImportance = ArrayList<Pair<Move, Int>>(this.size)
    var movesWithFigureHit = 0
    this.forEach { move ->
        // a move is more important
        // - if it's in movesToTryFirst (ordered by how often that move occurs in movesToTryFirst) -> 6+occurrence
        // - if a figure is being hit (ordered by which figure is hit: pawn:1...queen:5,king:6)
        // - otherwise the importance is 0
        val importance: Int = movesToTryFirst.getOccurrence(move)?.let { occurrence ->
            6 + occurrence
        } ?: board.getFigureOrNull(move.to)?.let { figure ->
            movesWithFigureHit++
            figure.type.index
        } ?: 0
        movesAndImportance.add(move to importance)
    }
    movesAndImportance.sortByDescending { it.second }

    // scramble positions with figureType.index = null
    (movesWithFigureHit + movesToTryFirst.size).let {movesWithImportance ->
//        if (movesWithImportance < this.size / 8) {
            this.scrambleFrom(movesWithImportance)
//        }
    }

    this.clear()
    movesAndImportance.mapTo(this) { it.first }
}

private inline fun <reified T> ArrayList<T>.scrambleFrom(startIndex: Int) {
    val pivotIndex = startIndex+(this.lastIndex-startIndex)/2
    if(pivotIndex-startIndex>3) {
        for (i in startIndex ..< pivotIndex step 2) {
            val temp = this[i]
            val inverseI = 2*pivotIndex-i
            this[i] = this[inverseI]
            this[inverseI] = temp
        }
    }
}