package voidchess.engine.evaluation.leaf

import voidchess.common.board.forAllFigures
import voidchess.common.board.move.Position
import voidchess.common.figures.*
import voidchess.common.engine.Ongoing
import voidchess.engine.board.EngineChessGame

internal abstract class StaticEval {
    abstract fun getNumericEvaluation(
        game: FixedBoard,
        forWhite: Boolean,
        isWhiteTurn: Boolean
    ): Ongoing

    open fun getSecondaryCheckmateEvaluation(
        game: FixedBoard,
        forColour: Colour,
        isWhiteTurn: Boolean
    ): Double =
        evaluateFigures(game, forColour, isWhiteTurn)

    protected fun evaluateFigures(
        game: FixedBoard,
        forColour: Colour,
        isWhiteTurn: Boolean
    ): Double {
        var whiteFigures = 0.0
        var blackFigures = 0.0
        var noQueensLeftOnBoard = true
        var numberOfWhiteRooksKnightsOrBishops = 0
        var numberOfBlackRooksKnightsOrBishops = 0

        game.forAllFigures { figure, _ ->
            if (!figure.isKing()) {
                if (figure.colour.isWhite) {
                    if (figure.isPawn()) {
                        whiteFigures += PAWN_VALUE
                    } else {
                        numberOfWhiteRooksKnightsOrBishops++
                        when {
                            figure.isRook() -> whiteFigures += ROOK_VALUE
                            figure.isKnight() -> whiteFigures += KNIGHT_VALUE
                            figure.isBishop() -> whiteFigures += BISHOP_VALUE
                            figure.isQueen() -> {
                                whiteFigures += QUEEN_VALUE
                                noQueensLeftOnBoard = false
                                numberOfWhiteRooksKnightsOrBishops--
                            }
                        }
                    }
                } else {
                    if (figure.isPawn()) {
                        blackFigures += PAWN_VALUE
                    } else {
                        numberOfBlackRooksKnightsOrBishops++
                        when {
                            figure.isRook() -> blackFigures += ROOK_VALUE
                            figure.isKnight() -> blackFigures += KNIGHT_VALUE
                            figure.isBishop() -> blackFigures += BISHOP_VALUE
                            figure.isQueen() -> {
                                blackFigures += QUEEN_VALUE
                                noQueensLeftOnBoard = false
                                numberOfBlackRooksKnightsOrBishops--
                            }
                        }
                    }
                }
            }
        }

        if (noQueensLeftOnBoard && numberOfWhiteRooksKnightsOrBishops<3 && numberOfBlackRooksKnightsOrBishops<3) {
            // add extra value for passed pawns
            var whitePassedPawnsBonus = 0.0
            var blackPassedPawnsBonus = 0.0
            val hasWhiteRookKnightOrBishopLeft = numberOfWhiteRooksKnightsOrBishops != 0
            val hasBlackRookKnightOrBishopLeft = numberOfBlackRooksKnightsOrBishops != 0
            val whiteKingPos = game.whiteKingPos
            val blackKingPos = game.blackKingPos

            game.forAllFigures { figure, posIndex ->
                if(figure.isPawn()) {
                    val pawnPos = Position.byIndex(posIndex)
                    val pawnColour = figure.colour
                    if(game.isPassedPawn(pawnPos, pawnColour)) {
                        val pawnKingPos: Position
                        val opposingKingPos: Position
                        val hasOpponentRookKnightOrBishopLeft: Boolean
                        if(pawnColour.isWhite) {
                            pawnKingPos = whiteKingPos
                            opposingKingPos = blackKingPos
                            hasOpponentRookKnightOrBishopLeft = hasBlackRookKnightOrBishopLeft
                        }else{
                            pawnKingPos = blackKingPos
                            opposingKingPos = whiteKingPos
                            hasOpponentRookKnightOrBishopLeft = hasWhiteRookKnightOrBishopLeft
                        }
                        val passedPawnBonus = if(hasOpponentRookKnightOrBishopLeft) {
                            evaluatePassedPawnWithOpponentLightFigures(pawnPos, pawnColour)
                        } else {
                            evaluatePassedPawnWithoutOpponentLightFigures(
                                pawnPos,
                                pawnColour,
                                pawnKingPos,
                                opposingKingPos,
                                isWhiteTurn
                            )
                        }
                        if(pawnColour.isWhite) {
                            whitePassedPawnsBonus += passedPawnBonus
                        }else{
                            blackPassedPawnsBonus += passedPawnBonus
                        }
                    }
                }
            }
            whiteFigures += if(hasBlackRookKnightOrBishopLeft) whitePassedPawnsBonus/numberOfBlackRooksKnightsOrBishops else whitePassedPawnsBonus
            blackFigures += if(hasWhiteRookKnightOrBishopLeft) blackPassedPawnsBonus/numberOfWhiteRooksKnightsOrBishops else blackPassedPawnsBonus
        }

        return if (forColour.isWhite)
            whiteFigures - blackFigures
        else
            blackFigures - whiteFigures
    }
}

/**
 * it is assumed that only pawns (and kings) remain on the board.
 */
private fun FixedBoard.isPassedPawn(pawnPos: Position, pawnColour: Colour): Boolean {
    val rowProgression: IntProgression = if(pawnColour.isWhite) {
        6 downTo pawnPos.row + 1
    }else{
        1 until pawnPos.row
    }
    val minColumn = (pawnPos.column-1).coerceAtLeast(0)
    val maxColumn = (pawnPos.column+1).coerceAtMost(7)

    for(row in rowProgression) {
        for (column in minColumn..maxColumn) {
            val content = this.getContent(Position.getIndex(row, column))
            if(content.isPawn() && content.hasOppositeColour(pawnColour)) {
                return false
            }
        }
    }
    return true
}

private fun evaluatePassedPawnWithOpponentLightFigures(
    pawnPos: Position, pawnColour: Colour
): Double {
    val pawnPosRow = pawnPos.row

    val pawnDistanceToPromotionPos: Int = if(pawnColour.isWhite) {
        (7-pawnPosRow).coerceAtMost(5) // because of double jump
    } else {
        (pawnPosRow).coerceAtMost(5) // because of double jump
    }

    return (6-pawnDistanceToPromotionPos) * .2
}

private fun evaluatePassedPawnWithoutOpponentLightFigures(
    pawnPos: Position,
    pawnColour: Colour,
    pawnsKingPos: Position,
    opposingKingPos: Position,
    isWhiteTurn: Boolean
): Double {
    val isWhitePawn = pawnColour.isWhite

    val promotionPos: Position
    val pawnDistanceToPromotionPos: Int = if(isWhitePawn) {
        promotionPos = Position[7, pawnPos.column]
        (7-pawnPos.row).coerceAtMost(5) // because of double jump
    } else {
        promotionPos = Position[0, pawnPos.column]
        (pawnPos.row).coerceAtMost(5) // because of double jump
    }
    var passedPawnBonus: Double = (6-pawnDistanceToPromotionPos) * .2

    val loosingKingDistanceToPromotionPos: Int =
        opposingKingPos.distanceTo(promotionPos).let { loosingKingDistanceToPromotionPos ->
            if (isWhitePawn == isWhiteTurn) {
                loosingKingDistanceToPromotionPos
            } else {
                loosingKingDistanceToPromotionPos - 1 // it's the kings turn to move next
            }
        }

    if(pawnDistanceToPromotionPos<loosingKingDistanceToPromotionPos) {
        // pawn can't be stopped from promotion
        passedPawnBonus += 2.0
    } else {
        // can pawn at least be guarded?
        val guardDistance = pawnsKingPos.distanceTo(pawnPos)
        val takeDistance = opposingKingPos.distanceTo(pawnPos).let { loosingKingDistanceToPawn ->
            if (isWhitePawn == isWhiteTurn) {
                loosingKingDistanceToPawn
            } else {
                loosingKingDistanceToPawn - 1 // it's the kings turn to move next
            }
        }
        if(guardDistance<=takeDistance) {
            passedPawnBonus += .5
        }
    }

    // make sure the opposing king wants to be as close as possible to the pawn
    passedPawnBonus -= (7-opposingKingPos.distanceTo(pawnPos))*.1

    return passedPawnBonus
}

private const val PAWN_VALUE = 1.0
private const val ROOK_VALUE = 4.5
private const val KNIGHT_VALUE = 3.0
private const val BISHOP_VALUE = 3.1
private const val QUEEN_VALUE = 9.0

internal data class GameInventory(
    val numberOfWhitePawns: Int,
    val numberOfWhiteRooks: Int,
    val numberOfWhiteKnights: Int,
    val numberOfWhiteBishops: Int,
    val numberOfWhiteQueens: Int,
    val numberOfBlackPawns: Int,
    val numberOfBlackRooks: Int,
    val numberOfBlackKnights: Int,
    val numberOfBlackBishops: Int,
    val numberOfBlackQueens: Int
) {
    val areOnlyPawnsLeft: Boolean = numberOfWhiteQueens == 0 &&
            numberOfWhiteRooks == 0 && numberOfWhiteKnights == 0 && numberOfWhiteBishops == 0 &&
            numberOfBlackQueens == 0 && numberOfBlackRooks == 0 && numberOfBlackKnights == 0 && numberOfBlackBishops == 0

    val arePawnsLeft: Boolean = numberOfWhitePawns != 0 || numberOfBlackPawns != 0

    val isOnlySinglePawnLeft: Boolean = (numberOfWhitePawns + numberOfBlackPawns) == 1

    val hasOneSideOnlyKingLeft: Boolean = (numberOfWhiteQueens == 0 && numberOfWhiteRooks == 0 &&
            numberOfWhiteKnights == 0 && numberOfWhiteBishops == 0 && numberOfWhitePawns == 0) ||
            (numberOfBlackQueens == 0 && numberOfBlackRooks == 0 && numberOfBlackKnights == 0 &&
                    numberOfBlackBishops == 0 && numberOfBlackPawns == 0)

    val isQueenLeft: Boolean = numberOfWhiteQueens != 0 || numberOfBlackQueens != 0

    val isRookLeft: Boolean = numberOfWhiteRooks != 0 || numberOfBlackRooks != 0
}

internal fun EngineChessGame.getInventory(): GameInventory {
    var numberOfWhitePawns = 0
    var numberOfWhiteRooks = 0
    var numberOfWhiteKnights = 0
    var numberOfWhiteBishops = 0
    var numberOfWhiteQueens = 0
    var numberOfBlackPawns = 0
    var numberOfBlackRooks = 0
    var numberOfBlackKnights = 0
    var numberOfBlackBishops = 0
    var numberOfBlackQueens = 0

    this.forAllFigures { figure ->
        if (figure.isWhite) {
            when (figure) {
                is Pawn -> numberOfWhitePawns++
                is Rook -> numberOfWhiteRooks++
                is Knight -> numberOfWhiteKnights++
                is Bishop -> numberOfWhiteBishops++
                is Queen -> numberOfWhiteQueens++
            }
        }else{
            when (figure) {
                is Pawn -> numberOfBlackPawns++
                is Rook -> numberOfBlackRooks++
                is Knight -> numberOfBlackKnights++
                is Bishop -> numberOfBlackBishops++
                is Queen -> numberOfBlackQueens++
            }
        }
    }

    return GameInventory(
        numberOfWhitePawns = numberOfWhitePawns,
        numberOfWhiteRooks = numberOfWhiteRooks,
        numberOfWhiteKnights = numberOfWhiteKnights,
        numberOfWhiteBishops = numberOfWhiteBishops,
        numberOfWhiteQueens = numberOfWhiteQueens,
        numberOfBlackPawns = numberOfBlackPawns,
        numberOfBlackRooks = numberOfBlackRooks,
        numberOfBlackKnights = numberOfBlackKnights,
        numberOfBlackBishops = numberOfBlackBishops,
        numberOfBlackQueens = numberOfBlackQueens
    )
}