package voidchess.engine.evaluation.leaf

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.forAllFigures
import voidchess.common.board.move.Position
import voidchess.common.figures.Figure
import voidchess.common.player.ki.evaluation.Ongoing
import voidchess.engine.board.EngineChessGame

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
        var containsOnlyPawns = true

        game.forAllFigures { figure ->
            if (!figure.isKing()) {
                if (figure.isWhite) {
                    if (figure.isPawn()) {
                        whiteFigures += PAWN_VALUE
                    } else {
                        containsOnlyPawns = false
                        when {
                            figure.isRook() -> whiteFigures += ROOK_VALUE
                            figure.isKnight() -> whiteFigures += KNIGHT_VALUE
                            figure.isBishop() -> whiteFigures += BISHOP_VALUE
                            figure.isQueen() -> whiteFigures += QUEEN_VALUE
                        }
                    }
                } else {
                    if (figure.isPawn()) {
                        blackFigures += PAWN_VALUE
                    } else {
                        containsOnlyPawns = false
                        when {
                            figure.isRook() -> blackFigures += ROOK_VALUE
                            figure.isKnight() -> blackFigures += KNIGHT_VALUE
                            figure.isBishop() -> blackFigures += BISHOP_VALUE
                            figure.isQueen() -> blackFigures += QUEEN_VALUE
                        }
                    }
                }
            }
        }

        if (containsOnlyPawns) {
            // add extra value for passed pawns
            val whiteKingPos = game.whiteKing.position
            val blackKingPos = game.blackKing.position

            game.forAllFigures { figure ->
                if(figure.isPawn()) {
                    if(game.isPassedPawn(figure.position, figure.isWhite)) {
                        val pawnKingPos: Position
                        val opposingKingPos: Position
                        if(figure.isWhite) {
                            pawnKingPos = whiteKingPos
                            opposingKingPos = blackKingPos
                        }else{
                            pawnKingPos = blackKingPos
                            opposingKingPos = whiteKingPos
                        }
                        val passedPawnBonus = evaluatePassedPawn(figure, pawnKingPos, opposingKingPos, isWhiteTurn)
                        if(figure.isWhite) {
                            whiteFigures += passedPawnBonus
                        }else{
                            blackFigures += passedPawnBonus
                        }
                    }
                }
            }
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
    val nextPawnPosRow = if(isPawnWhite) {
        pawnPos.row + 1
    } else {
        pawnPos.row - 1
    }

    for (column in (pawnPos.column-1).coerceAtLeast(0)..(pawnPos.column+1).coerceAtMost(7)) {
        val rowProgression: IntProgression = if(isPawnWhite) {
            nextPawnPosRow..6
        }else{
            1..nextPawnPosRow
        }
        for(row in rowProgression) {
            this.getFigureOrNull(Position[row, column])?.let { figure ->
                if(figure.isPawn()&&figure.isWhite!=isPawnWhite) {
                    return false
                }
            }
        }
    }
    return true
}

private fun evaluatePassedPawn(
    pawn: Figure,
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
            when {
                figure.isPawn() -> numberOfWhitePawns++
                figure.isRook() -> numberOfWhiteRooks++
                figure.isKnight() -> numberOfWhiteKnights++
                figure.isBishop() -> numberOfWhiteBishops++
                figure.isQueen() -> numberOfWhiteQueens++
            }
        }else{
            when {
                figure.isPawn() -> numberOfBlackPawns++
                figure.isRook() -> numberOfBlackRooks++
                figure.isKnight() -> numberOfBlackKnights++
                figure.isBishop() -> numberOfBlackBishops++
                figure.isQueen() -> numberOfBlackQueens++
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