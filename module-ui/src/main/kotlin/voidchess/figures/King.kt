package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.AttackLines
import voidchess.board.check.CheckSearch
import voidchess.board.getFirstFigureInDir
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.math.sign


class King : CastlingFigure {
    var didCastling: Boolean = false
    private val groundRow = if (isWhite) 0 else 7

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition,
        FigureType.KING
    ) {
        didCastling = false
    }

    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int, didCastling: Boolean) : super(isWhite, startPosition, stepsTaken,
        FigureType.KING
    ) {
        this.didCastling = didCastling
    }

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val horizontalDifference = abs(position.row - toPos.row)
        val verticalDifference = abs(position.column - toPos.column)
        if (horizontalDifference <= 1 && verticalDifference <= 1) {
            val figure = game.getFigureOrNull(toPos)
            if (figure==null || hasDifferentColor(figure)) return true
        }
        if (isShortCastlingReachable(toPos, game)) return true
        return isLongCastlingReachable(toPos, game)
    }

    private fun isShortCastlingReachable(to: Position, game: BasicChessBoard): Boolean {
        val toFigure = game.getFigureOrNull(to)
        if (canCastle() &&
                toFigure is Rook &&
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

    private fun isLongCastlingReachable(to: Position, game: BasicChessBoard): Boolean {
        val toFigure = game.getFigureOrNull(to)
        if (canCastle() &&
                toFigure is Rook &&
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
                else updatedToColumn until kingPos.column

        for (column in columnRange) {
            if (isKingCheckAt(Position[kingPos.row, column], game)) return true
        }
        return false
    }

    private fun isKingCheckAt(to: Position, game: ChessBoard): Boolean {
        val from = position
        val figureTaken = game.move(this, to)
        val isCheck = CheckSearch.isCheck(game, this)
        game.undoMove(this, from, figureTaken)
        return isCheck
    }

    override fun getReachableMoves(game: BasicChessBoard): Collection<Move> = ArrayList<Move>(8).apply{
        val normalMoves: List<Position> = Direction.values().mapNotNull { direction ->
            position.step(direction)?.let {neighbouringPos ->
                val figure = game.getFigureOrNull(neighbouringPos)
                if (figure==null || hasDifferentColor(figure)) {
                    neighbouringPos
                }else{
                    null
                }
            }
        }
        addAll(normalMoves.map { Move[position, it] })

        if(canCastle()) {
            val firstFigureToRight: Figure? = game.getFirstFigureInDir(Direction.RIGHT, position)
            if(firstFigureToRight is Rook && firstFigureToRight.canCastle()) {
                add(Move[position,firstFigureToRight.position])
            }
            val firstFigureToLeft: Figure? = game.getFirstFigureInDir(Direction.LEFT, position)
            if(firstFigureToLeft is Rook && firstFigureToLeft.canCastle()) {
                add(Move[position,firstFigureToLeft.position])
            }
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        val attackLines = game.getCachedAttackLines(isWhite)
        directionLoop@ for(direction in Direction.values()) {
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

    override fun undoMove(oldPosition: Position) {
        super.undoMove(oldPosition)

        if (stepsTaken == 0) {
            didCastling = false
        }
    }

    override fun toString() = if (didCastling) "${super.toString()}-true" else super.toString()
}
