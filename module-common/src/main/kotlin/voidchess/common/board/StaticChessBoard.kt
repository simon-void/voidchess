package voidchess.common.board

import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position
import voidchess.common.figures.*


interface StaticChessBoard {
    val whiteKingPos: Position
    val blackKingPos: Position
    val enpassantPos: Position?
    val didWhiteCastle: Boolean
    val didBlackCastle: Boolean

    fun isFreeArea(pos: Position): Boolean
    fun getFigureOrNull(pos: Position): Figure?
}

fun StaticChessBoard.getFirstFigureInDir(direction: Direction, startPos: Position): Pair<Figure,Position>? {
    startPos.forEachPosInLine(direction) { pos->
        getFigureOrNull(pos)?.let { figure -> return figure to pos } ?: return@forEachPosInLine false
    }
    return null
}

fun StaticChessBoard.getKingPos(isWhiteKing: Boolean): Position = if (isWhiteKing) { whiteKingPos } else { blackKingPos }

fun StaticChessBoard.getFigure(pos: Position) = getFigureOrNull(pos) ?: throw AssertionError("no figure at $pos")

inline fun StaticChessBoard.forAllFigures(informOfFigure: (Figure)->Unit) {
    for (linearIndex in 0..63) getFigureOrNull(Position.byIndex(linearIndex))?.let(informOfFigure)
}

inline fun StaticChessBoard.forAllFiguresOfColor(isWhite: Boolean, informOfFigure: (Figure)->Unit) {
    if(isWhite) {
        for (linearIndex in 0..63) getFigureOrNull(Position.byIndex(linearIndex))?.let {
            if (it.isWhite) {
                informOfFigure(it)
            }
        }
    }else{
        for (linearIndex in 63 downTo 0) getFigureOrNull(Position.byIndex(linearIndex))?.let {
            if (!it.isWhite) {
                informOfFigure(it)
            }
        }
    }
}

inline val StaticChessBoard.figureCount: Int get() {
    var figureCounter = 0
    this.forAllFigures { figureCounter++ }
    return figureCounter
}

val StaticChessBoard.isDrawBecauseOfLowMaterial: Boolean
    get() {
        if (this.figureCount > 6) {
            return false
        }
        var numberOfWhiteBishops = 0
        var numberOfBlackBishops = 0
        var numberOfWhiteKnights = 0
        var numberOfBlackKnights = 0

        this.forAllFigures { figure ->
            if (figure is Pawn
                || figure is Rook
                || figure is Queen
            ) {
                return false
            } else if (figure is Bishop) {
                if (figure.isWhite)
                    numberOfWhiteBishops++
                else
                    numberOfBlackBishops++
            } else if (figure is Knight) {
                if (figure.isWhite)
                    numberOfWhiteKnights++
                else
                    numberOfBlackKnights++
            }
        }

        if (numberOfWhiteBishops > 1 || numberOfBlackBishops > 1) {
            return false
        }
        if (numberOfWhiteKnights > 2 || numberOfBlackKnights > 2) {
            return false
        }
        if (numberOfWhiteBishops == 1 && numberOfWhiteKnights > 0) {
            return false
        }
        return numberOfBlackBishops == 0 || numberOfBlackKnights == 0
    }

fun StaticChessBoard.canBeHitEnpassant(possibleFigurePos: Position): Boolean =
    this.enpassantPos?.equalsPosition(possibleFigurePos) ?: false