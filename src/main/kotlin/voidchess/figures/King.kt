package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.helper.CheckSearch
import voidchess.helper.Move
import voidchess.helper.Position

/**
 * @author stephan
 */
class King : RochadeFigure {
    private var didRochade: Boolean = false
    private val groundRow = if (isWhite) 0 else 7

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.KING) {
        didRochade = false
    }

    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int, didRochade: Boolean) : super(isWhite, startPosition, stepsTaken, FigureType.KING) {
        this.didRochade = didRochade
    }

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val horizontalDifference = Math.abs(position.row - to.row)
        val verticalDifference = Math.abs(position.column - to.column)
        if (horizontalDifference <= 1 && verticalDifference <= 1) {
            val figure = game.getFigure(to)
            if (figure==null || hasDifferentColor(figure)) return true
        }
        if (isShortRochadeReachable(to, game)) return true
        return isLongRochadeReachable(to, game)
    }

    private fun isShortRochadeReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val toFigure = game.getFigure(to)
        if (canParticipateInRochade() &&
                toFigure!=null &&
                to.column > position.column) {
            if (toFigure.canParticipateInRochade() && toFigure.isWhite == isWhite) {
                if (position.column == 6) {
                    if (!game.isFreeArea(Position.get(groundRow, 5))) {
                        return false
                    }
                } else {
                    //Die Felder bis zur g-Spalte müssen bis auf den Turm leer sein
                    for (column in position.column + 1..6) {
                        val middlePosition = Position.get(groundRow, column)
                        val middleFigure = game.getFigure(middlePosition)
                        if (middleFigure!=null && !middleFigure.canParticipateInRochade()) {
                            return false
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    private fun isLongRochadeReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val toFigure = game.getFigure(to)
        if (canParticipateInRochade() &&
                toFigure!=null &&
                to.column < position.column) {


            if (toFigure.canParticipateInRochade() && toFigure.isWhite == isWhite) {
                //kommt der König auf die c-Linie?
                if (position.column == 1) {        //auf der a-Linie kann der König nicht stehen, da dort Turm sein muß
                    if (!game.isFreeArea(Position.get(groundRow, 2))) {
                        return false
                    }
                } else if (position.column > 2) {
                    //Die Felder bis zur c-Spalte müssen bis auf den Turm leer sein
                    for (column in position.column - 1 downTo 2) {
                        val middlePosition = Position.get(groundRow, column)
                        val middleFigure = game.getFigure(middlePosition)
                        if (middleFigure!=null && !middleFigure.canParticipateInRochade()) {
                            return false
                        }
                    }
                }
                //kommt der Turm auf die d-Linie?
                if (to.column != 3) {
                    val step = CheckSearch.signum(3 - to.column)
                    var middlePosition: Position
                    var column = to.column + step
                    while (column != 3) {
                        middlePosition = Position.get(groundRow, column)
                        val middleFigure = game.getFigure(middlePosition)
                        if (middleFigure!=null) {
                            if (!middleFigure.canParticipateInRochade() || middleFigure.isRock()) {
                                return false
                            }
                        }
                        column += step
                    }
                    middlePosition = Position.get(groundRow, 3)
                    val middleFigure = game.getFigure(middlePosition)
                    if (middleFigure!=null) {
                        if (!middleFigure.canParticipateInRochade() || middleFigure.isRock()) {
                            return false
                        }
                    }
                }
                return true
            }
        }
        return false
    }

    override fun isPassiveBound(to: Position, game: SimpleChessBoardInterface): Boolean {
        var realTo = to
        val toFigure = game.getFigure(to)
        if (toFigure!=null && toFigure.canParticipateInRochade()) {
            val column = if (to.column - position.column > 0) 6 else 2
            realTo = Position.get(to.row, column)
            if (CheckSearch.isCheck(game, position)) return true
            if (isKingAtCheckInbetweenRochade(position, realTo, game)) return true
        }
        return isKingCheckAt(realTo, game)
    }

    private fun isKingAtCheckInbetweenRochade(
            from: Position,
            to: Position,
            game: SimpleChessBoardInterface
    ): Boolean {
        assert(from.row == to.row)

        val step = CheckSearch.signum(to.column - from.column)
        var column = from.column + step
        while (column != to.column) {
            if (isKingCheckAt(Position.get(from.row, column), game)) return true
            column += step
        }
        return false
    }

    private fun isKingCheckAt(to: Position, game: SimpleChessBoardInterface): Boolean {
        val from = position
        val figureTaken = game.move(this, to)
        val isCheck = CheckSearch.isCheck(game, to)
        game.undoMove(this, from, figureTaken)
        return isCheck
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        val minRow = Math.max(position.row - 1, 0)
        val minColumn = Math.max(position.column - 1, 0)
        val maxRow = Math.min(position.row + 1, 7)
        val maxColumn = Math.min(position.column + 1, 7)

        for (row in minRow..maxRow) {
            for (column in minColumn..maxColumn) {
                val checkPosition = Position.get(row, column)
                if (!checkPosition.equalsPosition(position)) {
                    val figure = game.getFigure(checkPosition)
                    if (figure==null || figure.isWhite != isWhite) {
                        result.add(Move.get(position, checkPosition))
                    }
                }
            }
        }

        if (canParticipateInRochade()) {
            for (column in position.column + 1..7) {
                val pos = Position.get(position.row, column)
                val figure = game.getFigure(pos)
                if (figure!=null && figure.canParticipateInRochade() && isReachable(pos, game)) {
                    result.add(Move.get(position, pos))
                }
                break
            }
            for (column in position.column - 1 downTo 0) {
                val pos = Position.get(position.row, column)
                val figure = game.getFigure(pos)
                if (figure!=null && figure.canParticipateInRochade() && isReachable(pos, game)) {
                    result.add(Move.get(position, pos))
                }
                break
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
                val checkPosition = Position.get(row, column)
                if (isMovable(checkPosition, game)) {
                    return true
                }
            }
        }

        if (position.column + 2 < 8) {
            val shortRochade = Position.get(position.row, position.column + 2)
            if (isMovable(shortRochade, game)) {
                return true
            }
        }

        if (position.column - 2 >= 0) {
            val longRochade = Position.get(position.row, position.column - 2)
            if (isMovable(longRochade, game)) {
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
                val checkPosition = Position.get(row, column)
                if (isReachable(checkPosition, game)) {
                    count++
                }
            }
        }

        if (position.column + 2 < 8) {
            val shortRochade = Position.get(position.row, position.column + 2)
            if (isReachable(shortRochade, game)) {
                count++
            }
        }

        if (position.column - 2 >= 0) {
            val longRochade = Position.get(position.row, position.column - 2)
            if (isReachable(longRochade, game)) {
                count++
            }
        }

        return count
    }

    fun didRochade() = didRochade

    fun performRochade() {
        didRochade = true
    }

    override fun undoMove(oldPosition: Position) {
        super.undoMove(oldPosition)

        if (stepsTaken == 0) {
            didRochade = false
        }
    }

    override fun toString() = if (didRochade) "${super.toString()}-true" else super.toString()
}
