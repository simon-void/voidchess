package voidchess.engine.evaluation.leaf

import voidchess.common.board.move.Position
import voidchess.common.engine.Ongoing


internal object MiddleGameEval : StaticEval() {

    override fun getNumericEvaluation(
        game: FixedBoard,
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

    fun getPreliminaryEvaluation(game: FixedBoard, forWhite: Boolean, isWhiteTurn: Boolean) =
        evaluateFigures(game, Colour(forWhite), isWhiteTurn)

    fun getSecondaryEvaluation(game: FixedBoard, forWhite: Boolean) = evaluateRuledArea(
        game,
        forWhite
    ) + evaluatePosition(game, Colour(forWhite))

    fun addSecondaryEvaluationTo(prelimEval: Double, game: FixedBoard, forWhite: Boolean) =
            Ongoing(
                    prelimEval + getSecondaryEvaluation(
                            game,
                            forWhite
                    )
            )

    override fun getSecondaryCheckmateEvaluation(
        game: FixedBoard,
        forColour: Colour,
        isWhiteTurn: Boolean
    ) = evaluateFigures(
        game,
        forColour,
        isWhiteTurn
    ) + evaluatePosition(game, forColour)

    private fun evaluateRuledArea(game: FixedBoard, forWhite: Boolean): Double {
//        val (whiteMoves, blackMoves) = game.countReachableMoves()
//
//        return VALUE_OF_AREA * if (forWhite)
//            whiteMoves - blackMoves
//        else
//            blackMoves - whiteMoves
        return 0.0
    }

    private fun evaluatePosition(game: FixedBoard, forColour: Colour): Double {
        var whiteEvaluation = 0.0
        var blackEvaluation = 0.0
        var foundWhiteQueen = false
        var foundBlackQueen = false

        game.forAllFigures {figure, posIndex ->
            val pos = Position.byIndex(posIndex)
            val colour = figure.colour
            if (figure.isPawn()) {
                if (colour.isWhite) {
                    whiteEvaluation += evaluatePawn(
                        game,
                        pos,
                        Colour(true),
                    )
                }else{
                    blackEvaluation += evaluatePawn(
                        game,
                        pos,
                        Colour(false),
                    )
                }
            } else if (figure.isKnight()) {
                val value = evaluateKnight(pos)
                if (colour.isWhite) {
                    whiteEvaluation += value
                }else{
                    blackEvaluation += value
                }
            } else if (figure.isBishop()) {
                val value =
                    evaluateBishop(game, pos, colour)
                if (colour.isWhite) {
                    whiteEvaluation += value
                }else{
                    blackEvaluation += value
                }
            } else if (figure.isQueen()) {
                if (colour.isWhite) {
                    foundWhiteQueen = true
                }else {
                    foundBlackQueen = true
                }
            }
        }

        whiteEvaluation += evaluateKing(
            game,
            KingInfo(
                pos = game.whiteKingPos,
                colour = Colour(true),
                didCastle = game.didWhiteKingCastle,
            ),
            foundBlackQueen
        )
        blackEvaluation += evaluateKing(
            game,
            KingInfo(
                pos = game.blackKingPos,
                colour = Colour(false),
                didCastle = game.didBlackKingCastle,
            ),
            foundWhiteQueen
        )

        return if (forColour.isWhite)
            whiteEvaluation - blackEvaluation
        else
            blackEvaluation - whiteEvaluation
    }

    private fun evaluateBishop(game: FixedBoard, bishopPos: Position, bishopColour: Colour): Double {
        //val isWhite = bishopColour.isWhite
        val startRow = if (bishopColour.isWhite) 0 else 7
        val bishopRow = bishopPos.row

        if (bishopRow == startRow) {
            return BISHOP_ON_START_POSITION_PUNISHMENT
        }


        val blockingRow = if (bishopColour.isWhite) 2 else 5
        val bishopColumn = bishopPos.column

        if (bishopRow == blockingRow && (bishopColumn == 3 || bishopColumn == 4)) {
            val possiblePawnPos = Position[if (bishopColour.isWhite) 1 else 6, bishopColumn]
            if(containsPawnOfColor(
                    game,
                    possiblePawnPos,
                    bishopColour
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

    private fun evaluatePawn(game: FixedBoard, pos: Position, colour: Colour) =
            evaluatePawnPosition(
                pos,
                colour
            ) + evaluatePawnDefense(
                game,
                pos,
                colour
            )

    private fun evaluatePawnPosition(pos: Position, colour: Colour): Double {

        return MOVES_GONE_VALUE * if(colour.isWhite) pos.row-1 else 6-pos.row
    }

    private fun evaluatePawnDefense(game: FixedBoard, pos: Position, colour: Colour): Double {

        val forwardRow = if (colour.isWhite) pos.row + 1 else pos.row - 1
        val backwardRow = if (colour.isWhite) pos.row - 1 else pos.row + 1

        if (pos.column != 0) {
            val leftForwardPosition = Position[forwardRow, pos.column - 1]
            val leftBackwardPosition = Position[backwardRow, pos.column - 1]
            val leftPosition = Position[pos.row, pos.column - 1]
            if (containsPawnOfColor(
                    game,
                    leftForwardPosition,
                    colour
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    leftBackwardPosition,
                    colour
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    leftPosition,
                    colour
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
                    colour
                )
                            || containsPawnOfColor(
                    game,
                    rightForwardPosition,
                    colour
                )
                            || containsPawnOfColor(
                    game,
                    rightBackwardPosition,
                    colour
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
                    colour
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    rightBackwardPosition,
                    colour
                )
            ) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(
                    game,
                    rightPosition,
                    colour
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
                    colour
                )
                            || containsPawnOfColor(
                    game,
                    leftForwardPosition,
                    colour
                )
                            || containsPawnOfColor(
                    game,
                    leftBackwardPosition,
                    colour
                ))) {
                return UNPROTECTED_BORDER_PAWN_VALUE
            }
        }

        return 0.0
    }

    private fun containsPawnOfColor(game: FixedBoard, pos: Position, colour: Colour): Boolean = game.getContent(pos.index).let {
        it.isPawn() && it.hasSameColour(colour)
    }


    private fun evaluateKing(game: FixedBoard, kingInfo: KingInfo, queenOfOppositeColorStillOnBoard: Boolean): Double {
        var value = evaluateCastling(
            kingInfo.didCastle,
            queenOfOppositeColorStillOnBoard
        )
        value += evaluateKingDefense(
            game,
            kingInfo,
            queenOfOppositeColorStillOnBoard
        )
        return value
    }

    private fun evaluateCastling(didKingCastle: Boolean, queenOfOppositeColorStillOnBoard: Boolean) =
            if (queenOfOppositeColorStillOnBoard && !didKingCastle) NOT_YET_CASTLED_PUNISHMENT
            else 0.0

    private fun evaluateKingDefense(game: FixedBoard, kingInfo: KingInfo, queenOfOppositeColorStillOnBoard: Boolean): Double {
        val kingColour = kingInfo.colour
        val kingsRow = kingInfo.pos.row
        val groundRow = if (kingColour.isWhite) 0 else 7

        if (kingInfo.didCastle && kingsRow == groundRow) {
            val kingsColumn = kingInfo.pos.column
            val secondRow = if (kingColour.isWhite) 1 else 6
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
                        kingColour
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

private data class KingInfo(
    val pos: Position,
    val colour: Colour,
    val didCastle: Boolean,
)