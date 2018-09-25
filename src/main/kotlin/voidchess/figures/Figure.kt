package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.helper.*
import java.util.*


abstract class Figure constructor(
        //Farbe der Figur
        val isWhite: Boolean,
        var position: Position,
        val type: FigureType
) {

    //kodiert Name der Klasse + Farbe
    val typeInfo: Int = if (isWhite) type.index else (type.index + 7)
    private val reachableMoves = LinkedList<Move>()

    fun isPawn() = type == FigureType.PAWN
    fun isRook() = type == FigureType.ROOK
    fun isKnight() = type == FigureType.KNIGHT
    fun isBishop() = type == FigureType.BISHOP
    fun isQueen() = type == FigureType.QUEEN
    fun isKing() = type == FigureType.KING

    fun hasDifferentColor(other: Figure) = isWhite != other.isWhite

    open fun canBeHitByEnpasent() = false

    open fun canCastle(): Boolean {
        return false
    }

    open fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) position = move.to
    }

    open fun undoMove(oldPosition: Position) {
        position = oldPosition
    }

    abstract fun isReachable(to: Position, game: BasicChessGameInterface): Boolean
    abstract fun countReachableMoves(game: BasicChessGameInterface): Int
    abstract fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>)
    abstract fun isSelectable(game: SimpleChessBoardInterface): Boolean

    fun isMovable(to: Position, game: SimpleChessBoardInterface): Boolean {
        return isReachable(to, game) && !isBound(to, game)
    }

    fun getPossibleMoves(game: SimpleChessBoardInterface, result: MutableList<Move>) {
        reachableMoves.clear()
        getReachableMoves(game, reachableMoves)
        val checkStatus = game.getCheckStatus(isWhite)

        for (move in reachableMoves) {
            val checkPosition = move.to
            if (!isBound(checkPosition, game, checkStatus)) {
                result.add(move)
            }
        }
    }

    open fun isPassiveBound(to: Position, game: SimpleChessBoardInterface): Boolean {
        val kingPos = game.getKingPosition(isWhite)
        //falls diese Figur nicht mit dem König auf einer Vertikalen,Horizontalen oder
        //Diagonalen steht, ist sie nicht gebunden
        if (!kingPos.isStraightOrDiagonalTo(position)) {
            return false
        }

        val rowStep = CheckSearch.signum(kingPos.row - position.row)
        val columnStep = CheckSearch.signum(kingPos.column - position.column)

        var row = position.row + rowStep
        var column = position.column + columnStep
        var isToPositionInBetweenKingAndAttacker = false

        //falls eine Figure zwischen König und dieser steht, ist diese nicht gebunden
        while (row != kingPos.row || column != kingPos.column) {
            val middlePos = Position.get(row, column)
            if (!game.isFreeArea(middlePos)) return false
            // TODO maybe return false on next if?!?!
            if (middlePos.equalsPosition(to)) isToPositionInBetweenKingAndAttacker = true
            row += rowStep
            column += columnStep
        }

        row = position.row - rowStep
        column = position.column - columnStep

        //nur falls in Verlängerung der Königslinie eine feindliche Figur steht(Dame,Läufer,Turm)
        //und das Ziel des Zuges nicht auf dieser Linie liegt, ist diese Figur gebunden
        while (row in 0..7 && column in 0..7) {
            val middlePos = Position.get(row, column)
            if (middlePos.equalsPosition(to)) isToPositionInBetweenKingAndAttacker = true
            val content = game.getContent(middlePos)
            if (!content.isFreeArea) {
                val figure = content.figure
                if (hasDifferentColor(figure)) {
                    if (figure.isQueen() ||
                            (rowStep == 0 || columnStep == 0) && figure.isRook() ||
                            rowStep != 0 && columnStep != 0 && figure.isBishop()) {
                        return !isToPositionInBetweenKingAndAttacker
                    }
                }
                return false
            }
            row -= rowStep
            column -= columnStep
        }
        return false
    }

    //isReachalble=true wird vorrausgesetzt
    fun isBound(to: Position, game: SimpleChessBoardInterface): Boolean {
        val checkStatus = game.getCheckStatus(isWhite)
        return isBound(to, game, checkStatus)
    }

    private fun isBound(to: Position, game: SimpleChessBoardInterface, checkStatus: CheckStatus): Boolean {
        if (checkStatus.isDoubleCheck) { //Doppelschach
            return !isKing() || isPassiveBound(to, game)
        }
        return if (checkStatus.isCheck) {     //einfaches Schach
            if (isKing()) {
                isPassiveBound(to, game)
            } else {
                !checkStatus.checkInterceptPositions.contains(to) || isPassiveBound(to, game)
            }
        } else {                 //kein Schach
            isPassiveBound(to, game)
        }
    }

    protected inline fun forEachReachablePos(game: BasicChessGameInterface, direction: Direction, informOf: (Position) -> Unit) {
        var currentPos: Position? = position

        while (true) {
            // the initial currentPos is not null and after every assignment it is checked if currentPos is null, so !!. is fine
            currentPos = currentPos!!.step(direction)
            if (currentPos==null) return
            val figure = game.getFigure(currentPos)
            if (figure == null) {
                informOf(currentPos)
            } else {
                if (hasDifferentColor(figure)) {
                    informOf(currentPos)
                }
                return
            }
        }
    }

    override fun toString() = "${type.label}-${if (isWhite) "white" else "black"}-$position"
    override fun equals(other: Any?) = other is Figure && typeInfo == other.typeInfo && position.equalsPosition(other.position)
    override fun hashCode() = typeInfo
}