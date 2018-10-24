package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.check.AttackLines
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.check.CheckSearch
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.math.sign


class King : CastlingFigure {
    private var didCastling: Boolean = false
    private val groundRow = if (isWhite) 0 else 7

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.KING) {
        didCastling = false
    }

    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int, didCastling: Boolean) : super(isWhite, startPosition, stepsTaken, FigureType.KING) {
        this.didCastling = didCastling
    }

    override fun isReachable(toPos: Position, game: BasicChessGameInterface): Boolean {
        val horizontalDifference = Math.abs(position.row - toPos.row)
        val verticalDifference = Math.abs(position.column - toPos.column)
        if (horizontalDifference <= 1 && verticalDifference <= 1) {
            val figure = game.getFigureOrNull(toPos)
            if (figure==null || hasDifferentColor(figure)) return true
        }
        if (isShortCastlingReachable(toPos, game)) return true
        return isLongCastlingReachable(toPos, game)
    }

    private fun isShortCastlingReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val toFigure = game.getFigureOrNull(to)
        if (canCastle() &&
                toFigure!=null &&
                to.column > position.column) {
            if (toFigure.canCastle() && toFigure.isWhite == isWhite) {
                if (position.column == 6) {
                    if (!game.isFreeArea(Position[groundRow, 5])) {
                        return false
                    }
                } else {
                    //Die Felder bis zur g-Spalte müssen bis auf den Turm leer sein
                    for (column in position.column + 1..6) {
                        val middlePosition = Position[groundRow, column]
                        val middleFigure = game.getFigureOrNull(middlePosition)
                        if (middleFigure!=null && !middleFigure.canCastle()) {
                            return false
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    private fun isLongCastlingReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val toFigure = game.getFigureOrNull(to)
        if (canCastle() &&
                toFigure!=null &&
                to.column < position.column) {


            if (toFigure.canCastle() && toFigure.isWhite == isWhite) {
                //kommt der König auf die c-Linie?
                if (position.column == 1) {        //auf der a-Linie kann der König nicht stehen, da dort Turm sein muß
                    if (!game.isFreeArea(Position[groundRow, 2])) {
                        return false
                    }
                } else if (position.column > 2) {
                    //Die Felder bis zur c-Spalte müssen bis auf den Turm leer sein
                    for (column in position.column - 1 downTo 2) {
                        val middlePosition = Position[groundRow, column]
                        val middleFigure = game.getFigureOrNull(middlePosition)
                        if (middleFigure!=null && !middleFigure.canCastle()) {
                            return false
                        }
                    }
                }
                //kommt der Turm auf die d-Linie?
                if (to.column != 3) {
                    val step = (3 - to.column).sign
                    var middlePosition: Position
                    var column = to.column + step
                    while (column != 3) {
                        middlePosition = Position[groundRow, column]
                        val middleFigure = game.getFigureOrNull(middlePosition)
                        if (middleFigure!=null) {
                            if (!middleFigure.canCastle() || middleFigure.isRook()) {
                                return false
                            }
                        }
                        column += step
                    }
                    middlePosition = Position[groundRow, 3]
                    val middleFigure = game.getFigureOrNull(middlePosition)
                    if (middleFigure!=null) {
                        if (!middleFigure.canCastle() || middleFigure.isRook()) {
                            return false
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    fun canNotMoveThereBecauseOfCheck(to: Position, game: SimpleChessBoardInterface, attackLines: AttackLines): Boolean {
        val toFigure = game.getFigureOrNull(to)
        val wantsToCastle = toFigure!=null && toFigure.canCastle()
        if (wantsToCastle) {
            if(attackLines.isCheck) {
                return true
            }
            return isKingAtCheckWhileOrAfterCastling(position, to, game)
        }

        // normal move, castling has been taken care of
        if(attackLines.noCheck) {
            return isKingCheckAt(to, game)
        }
        val directionTo = position.getDirectionTo(to) ?: throw IllegalArgumentException(
                "$to is not next to king's position on: $position"
        )
        for (checkLine in attackLines.checkLines) {
            if(checkLine.keepsKingInCheckIfHeMovesTo(directionTo)) {
                return true
            }
        }
        return isKingCheckAt(to, game)
    }

    private fun isKingAtCheckWhileOrAfterCastling(
            kingPos: Position,
            rookPos: Position,
            game: SimpleChessBoardInterface
    ): Boolean {
        assert(kingPos.row == rookPos.row)

        // king doesn't move to rookPosition so let's determine the real column target
        val updatedToColumn = if (rookPos.column - kingPos.column > 0) 6 else 2

        val columnRange =
                if(updatedToColumn>kingPos.column)
                    (kingPos.column+1)..updatedToColumn
                else updatedToColumn..(kingPos.column-1)

        for (column in columnRange) {
            if (isKingCheckAt(Position[kingPos.row, column], game)) return true
        }
        return false
    }

    private fun isKingCheckAt(to: Position, game: SimpleChessBoardInterface): Boolean {
        val from = position
        val figureTaken = game.move(this, to)
        val isCheck = CheckSearch.isCheck(game, this)
        game.undoMove(this, from, figureTaken)
        return isCheck
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleKingMoves(..)")
    override fun getPossibleMovesWhileUnboundAndCheck(game: SimpleChessBoardInterface, checkLine: CheckLine, result: MutableList<Move>) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleKingMoves(..)")
    override fun getPossibleMovesWhileBoundAndNoCheck(game: SimpleChessBoardInterface, boundLine: BoundLine, result: MutableList<Move>) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleKingMoves(..)")

    override fun getPossibleMoves(game: SimpleChessBoardInterface, result: MutableList<Move>) {
        val attackLines = game.getAttackLines(isWhite)

        if(attackLines.noCheck) {
            for(direction in Direction.values()) {
                position.step(direction)?.let { possibleKingPos->
                    if(isAccessible(game, possibleKingPos) && !isKingCheckAt(possibleKingPos, game)) {
                        result.add(Move[position, possibleKingPos])
                    }
                }
            }

            if(canCastle()) {
                for (column in position.column + 1..7) {
                    val pos = Position[position.row, column]
                    val figure = game.getFigureOrNull(pos)
                    if (figure!=null && figure.canCastle() && isShortCastlingReachable(pos, game)) {
                        if(!isKingAtCheckWhileOrAfterCastling(position, pos, game)) {
                            result.add(Move[position, pos])
                        }
                        break
                    }
                }
                for (column in position.column - 1 downTo 0) {
                    val pos = Position[position.row, column]
                    val figure = game.getFigureOrNull(pos)
                    if (figure!=null && figure.canCastle() && isLongCastlingReachable(pos, game)) {
                        if(!isKingAtCheckWhileOrAfterCastling(position, pos, game)) {
                            result.add(Move[position, pos])
                        }
                        break
                    }
                }
            }
        } else {
            // isSingleCheck || isDoubleCheck
            Direction.values().forEach directionLoop@ {direction ->
                position.step(direction)?.let { possibleKingPos->
                    for(checkLine in attackLines.checkLines) {
                        if(checkLine.keepsKingInCheckIfHeMovesTo(direction)) {
                            return@directionLoop
                        }
                    }
                    if (isAccessible(game, possibleKingPos) && !isKingCheckAt(possibleKingPos, game)) {
                        result.add(Move[position, possibleKingPos])
                    }
                }

            }
        }
    }

    override fun isSelectable(game: SimpleChessBoardInterface): Boolean {
        val minRow = Math.max(position.row - 1, 0)
        val minColumn = Math.max(position.column - 1, 0)
        val maxRow = Math.min(position.row + 1, 7)
        val maxColumn = Math.min(position.column + 1, 7)

        for (row in minRow..maxRow) {
            for (column in minColumn..maxColumn) {
                val checkPosition = Position[row, column]
                if (isMovable(checkPosition, game)) {
                    return true
                }
            }
        }

        if (position.column + 2 < 8) {
            val shortCastling = Position[position.row, position.column + 2]
            if (isMovable(shortCastling, game)) {
                return true
            }
        }

        if (position.column - 2 >= 0) {
            val longCastling = Position[position.row, position.column - 2]
            if (isMovable(longCastling, game)) {
                return true
            }
        }

        return false
    }

    override fun countReachableMoves(game: BasicChessGameInterface): Int {
        var count = 0
        val minRow = Math.max(position.row - 1, 0)
        val minColumn = Math.max(position.column - 1, 0)
        val maxRow = Math.min(position.row + 1, 7)
        val maxColumn = Math.min(position.column + 1, 7)

        for (row in minRow..maxRow) {
            for (column in minColumn..maxColumn) {
                val checkPosition = Position[row, column]
                if (isReachable(checkPosition, game)) {
                    count++
                }
            }
        }

        if (position.column + 2 < 8) {
            val shortCastling = Position[position.row, position.column + 2]
            if (isReachable(shortCastling, game)) {
                count++
            }
        }

        if (position.column - 2 >= 0) {
            val longCastling = Position[position.row, position.column - 2]
            if (isReachable(longCastling, game)) {
                count++
            }
        }

        return count
    }

    fun didCastling() = didCastling

    fun performCastling() {
        didCastling = true
    }

    override fun undoMove(oldPosition: Position) {
        super.undoMove(oldPosition)

        if (stepsTaken == 0) {
            didCastling = false
        }
    }

    override fun toString() = if (didCastling) "${super.toString()}-true" else super.toString()
}
