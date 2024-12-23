package voidchess.engine.inner.evaluation.leaf

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.forAllFigures
import voidchess.common.board.move.Position
import voidchess.common.figures.*
import voidchess.common.engine.Ongoing
import voidchess.engine.inner.board.EngineChessGame

internal abstract class StaticEval {
    abstract fun getNumericEvaluation(
        game: EngineChessGame,
        forWhite: Boolean,
        isWhiteTurn: Boolean
    ): Ongoing

    open fun getSecondaryCheckmateEvaluation(
        game: EngineChessGame,
        forWhite: Boolean,
        isWhiteTurn: Boolean
    ): Double =
        evaluateFigures(game, forWhite, isWhiteTurn)

    protected fun evaluateFigures(
        game: EngineChessGame,
        forWhite: Boolean,
        isWhiteTurn: Boolean
    ): Double {
        var whiteFigures = 0.0
        var blackFigures = 0.0
        var noQueensLeftOnBoard = true
        var numberOfWhiteRooksKnightsOrBishops = 0
        var numberOfBlackRooksKnightsOrBishops = 0

        game.forAllFigures { figure ->
            if (figure !is King) {
                if (figure.isWhite) {
                    if (figure is Pawn) {
                        whiteFigures += PAWN_VALUE
                    } else {
                        numberOfWhiteRooksKnightsOrBishops++
                        when (figure) {
                            is Rook -> whiteFigures += ROOK_VALUE
                            is Knight -> whiteFigures += KNIGHT_VALUE
                            is Bishop -> whiteFigures += BISHOP_VALUE
                            is Queen -> {
                                whiteFigures += QUEEN_VALUE
                                noQueensLeftOnBoard = false
                                numberOfWhiteRooksKnightsOrBishops--
                            }
                        }
                    }
                } else {
                    if (figure is Pawn) {
                        blackFigures += PAWN_VALUE
                    } else {
                        numberOfBlackRooksKnightsOrBishops++
                        when (figure) {
                            is Rook -> blackFigures += ROOK_VALUE
                            is Knight -> blackFigures += KNIGHT_VALUE
                            is Bishop -> blackFigures += BISHOP_VALUE
                            is Queen -> {
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
            val whiteKingPos = game.whiteKing.position
            val blackKingPos = game.blackKing.position

            game.forAllFigures { figure ->
                if(figure is Pawn) {
                    if(game.isPassedPawn(figure.position, figure.isWhite)) {
                        val pawnKingPos: Position
                        val opposingKingPos: Position
                        val hasOpponentRookKnightOrBishopLeft: Boolean
                        if(figure.isWhite) {
                            pawnKingPos = whiteKingPos
                            opposingKingPos = blackKingPos
                            hasOpponentRookKnightOrBishopLeft = hasBlackRookKnightOrBishopLeft
                        }else{
                            pawnKingPos = blackKingPos
                            opposingKingPos = whiteKingPos
                            hasOpponentRookKnightOrBishopLeft = hasWhiteRookKnightOrBishopLeft
                        }
                        val passedPawnBonus = if(hasOpponentRookKnightOrBishopLeft) {
                            evaluatePassedPawnWithOpponentLightFigures(figure)
                        } else {
                            evaluatePassedPawnWithoutOpponentLightFigures(
                                figure,
                                pawnKingPos,
                                opposingKingPos,
                                isWhiteTurn
                            )
                        }
                        if(figure.isWhite) {
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

        return if (forWhite)
            whiteFigures - blackFigures
        else
            blackFigures - whiteFigures
    }
}

/**
 * it is assumed that only pawns (and kings) remain on the board.
 */
private fun StaticChessBoard.isPassedPawn(pawnPos: Position, isPawnWhite: Boolean): Boolean {
    val rowProgression: IntProgression = if(isPawnWhite) {
        6 downTo pawnPos.row + 1
    }else{
        1 ..< pawnPos.row
    }
    val minColumn = (pawnPos.column-1).coerceAtLeast(0)
    val maxColumn = (pawnPos.column+1).coerceAtMost(7)

    for(row in rowProgression) {
        for (column in minColumn..maxColumn) {
            this.getFigureOrNull(Position[row, column])?.let { figure ->
                if(figure is Pawn && figure.isWhite!=isPawnWhite) {
                    return false
                }
            }
        }
    }
    return true
}

private fun evaluatePassedPawnWithOpponentLightFigures(
    pawn: Pawn
): Double {
    val pawnPosRow = pawn.position.row

    val pawnDistanceToPromotionPos: Int = if(pawn.isWhite) {
        (7-pawnPosRow).coerceAtMost(5) // because of double jump
    } else {
        (pawnPosRow).coerceAtMost(5) // because of double jump
    }

    return (6-pawnDistanceToPromotionPos) * .2
}

private fun evaluatePassedPawnWithoutOpponentLightFigures(
    pawn: Pawn,
    pawnsKingPos: Position,
    opposingKingPos: Position,
    isWhiteTurn: Boolean
): Double {
    val isWhitePawn = pawn.isWhite
    val pawnPos = pawn.position

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