package voidchess.common.figures

import voidchess.common.board.BasicChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getKing
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.math.abs


class Knight(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.KNIGHT, false, false) {

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val horizontalDifference = abs(position.row - toPos.row)
        val verticalDifference = abs(position.column - toPos.column)

        if((horizontalDifference != 2 || verticalDifference != 1) && (horizontalDifference != 1 || verticalDifference != 2)) {
            return false
        }

        val figure = game.getFigureOrNull(toPos)
        return figure == null || hasDifferentColor(figure)
    }

    override fun getReachableMoves(game: BasicChessBoard, result: MutableCollection<Move>) {
        forEachReachablePos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getReachableTakingMoves(game: BasicChessBoard, result: MutableCollection<Move>) {
        forEachReachableTakeableEndPos(game) {
            result.add(Move[position, it])
        }
    }

    override fun getCriticalMoves(game: ChessBoard, result: MutableSet<Move>) {
        fun doesFork(game: ChessBoard, knightPos: Position): Boolean {
            var heavyFigureCounter = 0
            knightPos.forEachKnightPos { attackedPos ->
                game.getFigureOrNull(attackedPos)?.let { figure ->
                    if (hasDifferentColor(figure)) {
                        when (figure) {
                            is King, is Queen, is Rook -> heavyFigureCounter++
                        }
                    }
                }
            }
            return heavyFigureCounter > 1
        }
        fun couldJump(from: Position, to: Position): Boolean {
            val horizontalDifference = abs(from.row - to.row)
            val verticalDifference = abs(from.column - to.column)

            return (horizontalDifference == 2 && verticalDifference == 1) || (horizontalDifference == 1 && verticalDifference == 2)
        }

        val opponentKingPos = game.getKing(!isWhite).position
        forEachPossiblePos(game) {
            if(containsFigureToTake(game, it) || doesFork(game, it) || couldJump(it, opponentKingPos)) {
                result.add(Move[position, it])
            }
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableCollection<Move>) {
        // the accessibility of the target field doesn't need to be checked because
        // all checkInterceptPositions are guaranteed to be either empty
        // or to contain the attacker (who has a different color)
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            val horizontalDifference = abs(position.row - checkInterceptPos.row)
            val verticalDifference = abs(position.column - checkInterceptPos.column)
            if(horizontalDifference+verticalDifference==3 && horizontalDifference!=0 && verticalDifference!=0) {
                result.add(Move[position, checkInterceptPos])
            }
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableCollection<Move>) {
        // a bound move can't move at all!
    }

    override fun getPossibleTakingMoves(game: ChessBoard, result: MutableCollection<Move>) {
        val attackLines = game.getCachedAttackLines()
        // a bound knight can't move at all, so only consider cases where he isn't
        if(attackLines.boundLineByBoundFigurePos[position]==null) {
            when {
                attackLines.noCheck -> getReachableTakingMoves(game, result)
                attackLines.isSingleCheck -> {
                    val attackerPos = attackLines.checkLines.first().attackerPos
                    if(isReachable(attackerPos, game)) {
                        result.add(Move[position, attackerPos])
                    }
                }
            }
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: BasicChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            if(isAccessible(game, pos)) informOf(pos)
        }
    }

    private inline fun forEachPossiblePos(game: ChessBoard, informOf: (Position) -> Unit) {
        val attackLines = game.getCachedAttackLines()
        // a bound knight can't move at all
        if(attackLines.boundLineByBoundFigurePos[position]==null) {
            when {
                attackLines.noCheck -> forEachReachablePos(game, informOf)
                attackLines.isSingleCheck -> {
                    attackLines.checkLines.first().posProgression.forEachReachablePos { checkLinePos ->
                        if(isReachable(checkLinePos, game)) {
                            informOf(checkLinePos)
                        }
                    }
                }
            }
        }
    }

    private inline fun forEachReachableTakeableEndPos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            game.getFigureOrNull(pos)?.let {figure->
                if(figure.isWhite!=isWhite) informOf(pos)
            }
        }
    }
}
