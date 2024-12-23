package voidchess.engine.inner.evaluation.leaf

import voidchess.common.board.forAllFigures
import voidchess.common.board.move.Position
import voidchess.common.figures.*
import voidchess.common.engine.Ongoing
import voidchess.engine.inner.board.EngineChessGame


internal data object MiddleGameEval : StaticEval() {

    override fun getNumericEvaluation(
        game: EngineChessGame,
        forWhite: Boolean,
        isWhiteTurn: Boolean // not used in this case
    ): Ongoing {
        val prelimEval = getPreliminaryEvaluation(
            game,
            forWhite,
            isWhiteTurn
        )
        return addSecondaryEvaluationTo(
            prelimEval,
            game,
            forWhite
        )
    }

    fun getPreliminaryEvaluation(game: EngineChessGame, forWhite: Boolean, isWhiteTurn: Boolean) =
        evaluateFigures(game, forWhite, isWhiteTurn)

    fun getSecondaryEvaluation(game: EngineChessGame, forWhite: Boolean) = evaluateRuledArea(
        game,
        forWhite
    ) + evaluatePosition(game, forWhite)

    fun addSecondaryEvaluationTo(prelimEval: Double, game: EngineChessGame, forWhite: Boolean) =
            Ongoing(
                    prelimEval + getSecondaryEvaluation(
                            game,
                            forWhite
                    )
            )

    override fun getSecondaryCheckmateEvaluation(
        game: EngineChessGame,
        forWhite: Boolean,
        isWhiteTurn: Boolean
    ) = evaluateFigures(
        game,
        forWhite,
        isWhiteTurn
    ) + evaluatePosition(game, forWhite)

    private fun evaluateRuledArea(game: EngineChessGame, forWhite: Boolean): Double {
        val (whiteMoves, blackMoves) = game.countReachableMoves()

        return VALUE_OF_AREA * if (forWhite)
            whiteMoves - blackMoves
        else
            blackMoves - whiteMoves
    }

    private fun evaluatePosition(game: EngineChessGame, forWhite: Boolean): Double {
        var whiteEvaluation = 0.0
        var blackEvaluation = 0.0
        var foundWhiteQueen = false
        var foundBlackQueen = false

        game.forAllFigures {figure->
            val pos = figure.position
            if (figure is Pawn) {
                if (figure.isWhite) {
                    whiteEvaluation += evaluatePawn(
                        game,
                        pos,
                        true
                    )
                }else{
                    blackEvaluation += evaluatePawn(
                        game,
                        pos,
                        false
                    )
                }
            } else if (figure is Knight) {
                val value = evaluateKnight(pos)
                if (figure.isWhite) {
                    whiteEvaluation += value
                }else{
                    blackEvaluation += value
                }
            } else if (figure is Bishop) {
                val value =
                    evaluateBishop(game, figure)
                if (figure.isWhite) {
                    whiteEvaluation += value
                }else{
                    blackEvaluation += value
                }
            } else if (figure is Queen) {
                if (figure.isWhite) {
                    foundWhiteQueen = true
                }else {
                    foundBlackQueen = true
                }
            }
        }

        whiteEvaluation += evaluateKing(
            game,
            game.whiteKing,
            foundBlackQueen
        )
        blackEvaluation += evaluateKing(
            game,
            game.blackKing,
            foundWhiteQueen
        )

        return if (forWhite)
            whiteEvaluation - blackEvaluation
        else
            blackEvaluation - whiteEvaluation
    }

    private fun evaluateBishop(game: EngineChessGame, bishop: Figure): Double {
        val isWhite = bishop.isWhite
        val startRow = if (isWhite) 0 else 7
        val bishopRow = bishop.position.row

        if (bishopRow == startRow) {
            return BISHOP_ON_START_POSITION_PUNISHMENT
        }


        val blockingRow = if (isWhite) 2 else 5
        val bishopColumn = bishop.position.column

        if (bishopRow == blockingRow && (bishopColumn == 3 || bishopColumn == 4)) {
            val possiblePawnPos = Position[if (isWhite) 1 else 6, bishopColumn]
            if(containsPawnOfColor(
                    game,
                    possiblePawnPos,
                    isWhite
                )
            ) {
                return BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT
            }
        }

        return 0.0
    }

    private fun evaluateKnight(pos: Position) =
            if (pos.row == 0 || pos.row == 7 || pos.column == 0 || pos.column == 7) BORDER_KNIGHT_PUNISHMENT
            else 0.0

    private fun evaluatePawn(game: EngineChessGame, pos: Position, isWhite: Boolean) =
            evaluatePawnPosition(
                pos,
                isWhite
            ) + evaluatePawnDefense(
                game,
                pos,
                isWhite
            )

    private fun evaluatePawnPosition(pos: Position, isWhite: Boolean): Double {

        return MOVES_GONE_VALUE * if( isWhite) pos.row-1 else 6-pos.row
    }

    private fun evaluatePawnDefense(game: EngineChessGame, pos: Position, isWhite: Boolean): Double {

        val forwardRow = if (isWhite) pos.row + 1 else pos.row - 1
        val backwardRow = if (isWhite) pos.row - 1 else pos.row + 1

        if (pos.column != 0) {
            val leftForwardPosition = Position[forwardRow, pos.column - 1]
            val leftBackwardPosition = Position[backwardRow, pos.column - 1]
            val leftPosition = Position[pos.row, pos.column - 1]
            if (containsPawnOfColor(
                    game,
                    leftForwardPosition,
                    isWhite
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    leftBackwardPosition,
                    isWhite
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    leftPosition,
                    isWhite
                )
            ) {
                return NEXT_TO_VALUE
            }
        } else {
            val rightPosition = Position[pos.row, 1]
            val rightForwardPosition = Position[forwardRow, 1]
            val rightBackwardPosition = Position[backwardRow, 1]
            if (!(containsPawnOfColor(
                    game,
                    rightPosition,
                    isWhite
                )
                            || containsPawnOfColor(
                    game,
                    rightForwardPosition,
                    isWhite
                )
                            || containsPawnOfColor(
                    game,
                    rightBackwardPosition,
                    isWhite
                ))) {
                return UNPROTECTED_BORDER_PAWN_VALUE
            }
        }

        if (pos.column != 7) {
            val rightForwardPosition = Position[forwardRow, pos.column + 1]
            val rightBackwardPosition = Position[backwardRow, pos.column + 1]
            val rightPosition = Position[pos.row, pos.column + 1]
            if (containsPawnOfColor(
                    game,
                    rightForwardPosition,
                    isWhite
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    rightBackwardPosition,
                    isWhite
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    rightPosition,
                    isWhite
                )
            ) {
                return NEXT_TO_VALUE
            }
        } else {
            val leftPosition = Position[pos.row, 6]
            val leftForwardPosition = Position[forwardRow, 6]
            val leftBackwardPosition = Position[backwardRow, 6]
            if (!(containsPawnOfColor(
                    game,
                    leftPosition,
                    isWhite
                )
                            || containsPawnOfColor(
                    game,
                    leftForwardPosition,
                    isWhite
                )
                            || containsPawnOfColor(
                    game,
                    leftBackwardPosition,
                    isWhite
                ))) {
                return UNPROTECTED_BORDER_PAWN_VALUE
            }
        }

        return 0.0
    }

    private fun containsPawnOfColor(game: EngineChessGame, pos: Position, white: Boolean): Boolean {
        val figure = game.getFigureOrNull(pos)
        return figure is Pawn && figure.isWhite == white
    }


    private fun evaluateKing(game: EngineChessGame, king: King, queenOfOppositeColorStillOnBoard: Boolean): Double {
        var value = evaluateCastling(
            king,
            queenOfOppositeColorStillOnBoard
        )
        value += evaluateKingDefense(
            game,
            king,
            queenOfOppositeColorStillOnBoard
        )
        return value
    }

    private fun evaluateCastling(king: King, queenOfOppositeColorStillOnBoard: Boolean) =
            if (queenOfOppositeColorStillOnBoard && !king.didCastling) NOT_YET_CASTLED_PUNISHMENT
            else 0.0

    private fun evaluateKingDefense(game: EngineChessGame, king: King, queenOfOppositeColorStillOnBoard: Boolean): Double {
        val isWhite = king.isWhite
        val kingsRow = king.position.row
        val groundRow = if (isWhite) 0 else 7

        if (king.didCastling && kingsRow == groundRow) {
            val kingsColumn = king.position.column
            val secondRow = if (isWhite) 1 else 6
            val defenseValue = if (queenOfOppositeColorStillOnBoard) {
                BIG_KING_DEFENSE_VALUE
            } else {
                SMALL_KING_DEFENSE_VALUE
            }
            val minColumn = (kingsColumn - 1).coerceAtLeast(0)
            val maxColumn = (kingsColumn + 1).coerceAtMost(7)
            var value = 0.0
            for (column in minColumn..maxColumn) {
                if (containsPawnOfColor(
                        game,
                        Position[secondRow, column],
                        isWhite
                    )
                ) {
                    value += defenseValue
                }
            }
            return value
        }

        return 0.0
    }

    private const val VALUE_OF_AREA = 0.015

    private const val BISHOP_ON_START_POSITION_PUNISHMENT = -0.45
    private const val BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT = -0.2

    private const val BORDER_KNIGHT_PUNISHMENT = -0.45

    private const val MOVES_GONE_VALUE = 0.2

    private const val DEFENSE_VALUE = 0.10                 // bonus, if a pawn is covered by or is covering another pawn
    private const val NEXT_TO_VALUE = 0.06                 // bonus, if a pawn has a neighbour
    private const val UNPROTECTED_BORDER_PAWN_VALUE = -0.2 // malus for an uncovered pawn on the sideline (a or h)

    private const val NOT_YET_CASTLED_PUNISHMENT = -0.1

    private const val BIG_KING_DEFENSE_VALUE = 0.5
    private const val SMALL_KING_DEFENSE_VALUE = 0.2
}


