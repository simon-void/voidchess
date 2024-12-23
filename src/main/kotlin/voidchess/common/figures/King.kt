package voidchess.common.figures

import voidchess.common.board.ChessBoard
import voidchess.common.board.StaticChessBoard
import voidchess.common.inner.board.check.AttackLines
import voidchess.common.inner.board.check.BoundLine
import voidchess.common.inner.board.check.CheckLine
import voidchess.common.inner.board.check.isInCheck
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.math.abs
import kotlin.math.sign


class King : CastlingFigure {
    var didCastling: Boolean = false
    private val groundRow = if (isWhite) 0 else 7

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.KING) {
        didCastling = false
    }

    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int, didCastling: Boolean) : super(isWhite, startPosition, stepsTaken, FigureType.KING) {
        this.didCastling = didCastling
    }

    override fun isReachable(toPos: Position, game: StaticChessBoard): Boolean {
        val horizontalDifference = abs(position.row - toPos.row)
        val verticalDifference = abs(position.column - toPos.column)
        if (horizontalDifference <= 1 && verticalDifference <= 1) {
            val figure = game.getFigureOrNull(toPos)
            if (figure==null || hasDifferentColor(figure)) return true
        }
        if (isShortCastlingReachable(toPos, game)) return true
        return isLongCastlingReachable(toPos, game)
    }

    private fun isShortCastlingReachable(to: Position, game: StaticChessBoard): Boolean {
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

    private fun isLongCastlingReachable(to: Position, game: StaticChessBoard): Boolean {
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
                            if (!middleFigure.canCastle() || middleFigure is Rook) {
                                return false
                            }
                        }
                        column += step
                    }
                    middlePosition = Position[groundRow, 3]
                    val middleFigure = game.getFigureOrNull(middlePosition)
                    if (middleFigure!=null) {
                        if (!middleFigure.canCastle() || middleFigure is Rook) {
                            return false
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    fun canNotMoveThereBecauseOfCheck(to: Position, game: ChessBoard, attackLines: AttackLines): Boolean {
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
        game: ChessBoard
    ): Boolean {
        assert(kingPos.row == rookPos.row)

        // king doesn't move to rookPosition so let's determine the real column target
        val updatedToColumn = if (rookPos.column - kingPos.column > 0) 6 else 2

        val columnRange =
                if(updatedToColumn>kingPos.column)
                    (kingPos.column+1)..updatedToColumn
                else updatedToColumn ..< kingPos.column

        for (column in columnRange) {
            if (isKingCheckAt(Position[kingPos.row, column], game)) return true
        }
        return false
    }

    private fun isKingCheckAt(to: Position, game: ChessBoard): Boolean =
        game.simulateSimplifiedMove(this, to) { boardAfterMove ->
            boardAfterMove.isInCheck(this)
        }

    override fun forReachableMoves(game: StaticChessBoard, informOf: MoveInformer) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleMoves(..)")
    override fun forReachableTakingMoves(game: StaticChessBoard, informOf: MoveInformer) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleTakingMoves(..)")
    override fun forPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, informOf: MoveInformer) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleMoves(..)")
    override fun forPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, informOf: MoveInformer) = throw UnsupportedOperationException("King doesn't support this method. Use getPossibleMoves(..)")

    override fun forPossibleMoves(game: ChessBoard, informOf: MoveInformer) {
        val attackLines = game.getCachedAttackLines()
        if(attackLines.noCheck) {
            for(direction in Direction.entries) {
                position.step(direction)?.let { possibleKingPos->
                    if(isAccessible(game, possibleKingPos) && !isKingCheckAt(possibleKingPos, game)) {
                        informOf(Move[position, possibleKingPos])
                    }
                }
            }
            if(canCastle()) {
                forPossibleCastlingMovesAssertNoCheckAndCanCastle(game, informOf)
            }
        } else {
            // isSingleCheck || isDoubleCheck
            Direction.entries.forEach directionLoop@ { direction ->
                position.step(direction)?.let { possibleKingPos->
                    for(checkLine in attackLines.checkLines) {
                        if(checkLine.keepsKingInCheckIfHeMovesTo(direction)) {
                            return@directionLoop
                        }
                    }
                    if (isAccessible(game, possibleKingPos) && !isKingCheckAt(possibleKingPos, game)) {
                        informOf(Move[position, possibleKingPos])
                    }
                }

            }
        }
    }

    private fun forPossibleCastlingMovesAssertNoCheckAndCanCastle(game: ChessBoard, informOf: MoveInformer) {
        if (canCastle()) {
            for (column in position.column + 1..7) {
                val pos = Position[position.row, column]
                val figure = game.getFigureOrNull(pos)
                if (figure != null && figure.canCastle() && isShortCastlingReachable(pos, game)) {
                    if (!isKingAtCheckWhileOrAfterCastling(position, pos, game)) {
                        informOf(Move[position, pos])
                    }
                    break
                }
            }
            for (column in position.column - 1 downTo 0) {
                val pos = Position[position.row, column]
                val figure = game.getFigureOrNull(pos)
                if (figure != null && figure.canCastle() && isLongCastlingReachable(pos, game)) {
                    if (!isKingAtCheckWhileOrAfterCastling(position, pos, game)) {
                        informOf(Move[position, pos])
                    }
                    break
                }
            }
        }
    }

    // the king ignores the 'OrCheck'-part (because he can't go setting the other king in check)
    override fun forPossibleTakingMoves(game: ChessBoard, informOf: MoveInformer) {
        val attackLines = game.getCachedAttackLines()
        Direction.entries.forEach directionLoop@ { direction ->
            position.step(direction)?.let { possibleKingPos->
                for(checkLine in attackLines.checkLines) {
                    if(checkLine.keepsKingInCheckIfHeMovesTo(direction)) {
                        return@directionLoop
                    }
                }
                game.getFigureOrNull(possibleKingPos)?.let { figure ->
                    if (figure.isWhite!=isWhite && !isKingCheckAt(possibleKingPos, game)) {
                        informOf(Move[position, possibleKingPos])
                    }
                }
            }

        }
    }

    override fun forCriticalMoves(game: ChessBoard, result: MutableSet<Move>) {
        // taking moves
        forPossibleTakingMoves(game) {
            result.add(it)
        }
        // plus castling
        if (canCastle()) {
            val attackLines = game.getCachedAttackLines()
            if(attackLines.noCheck) {
                forPossibleCastlingMovesAssertNoCheckAndCanCastle(game) {
                    result.add(it)
                }
            }
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        val attackLines = game.getCachedAttackLines()
        directionLoop@ for(direction in Direction.entries) {
            val directKingNeighbourPos = position.step(direction) ?: continue@directionLoop
            for (checkLine in attackLines.checkLines) {
                if(checkLine.keepsKingInCheckIfHeMovesTo(direction)) {
                    continue@directionLoop
                }
            }
            val figureAtNeighbourPos = game.getFigureOrNull(directKingNeighbourPos)
            if(figureAtNeighbourPos==null || hasDifferentColor(figureAtNeighbourPos)) {
                if(!isKingCheckAt(directKingNeighbourPos, game)) {
                    return true
                }
            }
        }

        // in some chess960 positions with the king directly next to a rook, there might be no normal moves but castling is still possible
        // (the king has to be directly next to the king because otherwise a normal move would be possible if castling to a side was possible)
        if(canCastle()) {
            for(side in listOf(Direction.LEFT, Direction.RIGHT)) {
                val sidePos = position.step(side) ?: throw IllegalStateException("king shouldn't be at outer side ($position) and be able to castle")
                game.getFigureOrNull(sidePos)?.let { figure ->
                    if(figure.canCastle() && !isKingAtCheckWhileOrAfterCastling(position, sidePos, game)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    override fun countReachableMoves(game: StaticChessBoard): Int {
        var count = 0
        val minRow = (position.row - 1).coerceAtLeast(0)
        val minColumn = (position.column - 1).coerceAtLeast(0)
        val maxRow = (position.row + 1).coerceAtMost(7)
        val maxColumn = (position.column + 1).coerceAtMost(7)

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

    override fun undoMove(oldPosition: Position) {
        super.undoMove(oldPosition)

        if (stepsTaken == 0) {
            didCastling = false
        }
    }

    override fun toString() = if (didCastling) "${super.toString()}-true" else super.toString()
}
