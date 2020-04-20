package voidchess.engine.evaluation.leaf

import voidchess.common.board.forAllFigures
import voidchess.common.figures.Pawn
import voidchess.common.player.ki.evaluation.Ongoing
import voidchess.engine.board.EngineChessGame

internal abstract class StaticEval {
    abstract fun getNumericEvaluation(game: EngineChessGame, forWhite: Boolean): Ongoing

    open fun getSecondaryCheckmateEvaluation(game: EngineChessGame, forWhite: Boolean): Double =
        evaluateFigures(game, forWhite)

    protected fun evaluateFigures(game: EngineChessGame, forWhite: Boolean): Double {
        var whiteFigures = 0.0
        var blackFigures = 0.0

        game.forAllFigures {figure->
            if (figure.isWhite) {
                when {
                    figure.isPawn() -> whiteFigures += PAWN_VALUE
                    figure.isRook() -> whiteFigures += Rook_VALUE
                    figure.isKnight() -> whiteFigures += KNIGHT_VALUE
                    figure.isBishop() -> whiteFigures += BISHOP_VALUE
                    figure.isQueen() -> whiteFigures += QUEEN_VALUE
                }
            } else {
                when {
                    figure.isPawn() -> blackFigures += PAWN_VALUE
                    figure.isRook() -> blackFigures += Rook_VALUE
                    figure.isKnight() -> blackFigures += KNIGHT_VALUE
                    figure.isBishop() -> blackFigures += BISHOP_VALUE
                    figure.isQueen() -> blackFigures += QUEEN_VALUE
                }
            }
        }

        return if (forWhite)
            whiteFigures - blackFigures
        else
            blackFigures - whiteFigures
    }
}

private const val PAWN_VALUE = 1.0
private const val Rook_VALUE = 4.5
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