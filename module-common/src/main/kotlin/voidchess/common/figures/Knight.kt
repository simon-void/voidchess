package voidchess.common.figures

import voidchess.common.board.StaticChessBoard
import voidchess.common.board.ChessBoard
import voidchess.common.board.check.BoundLine
import voidchess.common.board.check.CheckLine
import voidchess.common.board.getKingPos
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.math.abs


class Knight(isWhite: Boolean) : Figure(
    isWhite = isWhite,
    type = FigureType.KNIGHT,
) {

    override fun isReachable(position: Position, toPos: Position, game: StaticChessBoard): Boolean {
        val horizontalDifference = abs(position.row - toPos.row)
        val verticalDifference = abs(position.column - toPos.column)

        if((horizontalDifference != 2 || verticalDifference != 1) && (horizontalDifference != 1 || verticalDifference != 2)) {
            return false
        }

        val figure = game.getFigureOrNull(toPos)
        return figure == null || hasDifferentColor(figure)
    }

    override fun forReachableMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachablePos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forReachableTakingMoves(position: Position, game: StaticChessBoard, informOf: MoveInformer) {
        forEachReachableTakeableEndPos(position, game) {
            informOf(Move[position, it])
        }
    }

    override fun forCriticalMoves(position: Position, game: ChessBoard, result: MutableSet<Move>) {
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

        val opponentKingPos = game.getKingPos(!isWhite)
        forEachPossiblePos(position, game) { possiblePos ->
            if(containsFigureToTake(game, possiblePos) || doesFork(game, possiblePos) || couldJump(possiblePos, opponentKingPos)) {
                result.add(Move[position, possiblePos])
            }
        }
    }

    override fun forPossibleMovesWhileUnboundAndCheck(
        position: Position,
        game: ChessBoard,
        checkLine: CheckLine,
        informOf: MoveInformer
    ) {
        // the accessibility of the target field doesn't need to be checked because
        // all checkInterceptPositions are guaranteed to be either empty
        // or to contain the attacker (who has a different color)
        checkLine.posProgression.forEachReachablePos {checkInterceptPos->
            val horizontalDifference = abs(position.row - checkInterceptPos.row)
            val verticalDifference = abs(position.column - checkInterceptPos.column)
            if(horizontalDifference+verticalDifference==3 && horizontalDifference!=0 && verticalDifference!=0) {
                informOf(Move[position, checkInterceptPos])
            }
        }
    }

    override fun forPossibleMovesWhileBoundAndNoCheck(
        position: Position,
        game: ChessBoard,
        boundLine: BoundLine,
        informOf: MoveInformer
    ) {
        // a bound move can't move at all!
    }

    override fun forPossibleTakingMoves(position: Position, game: ChessBoard, informOf: MoveInformer) {
        val attackLines = game.getCachedAttackLines()
        // a bound knight can't move at all, so only consider cases where he isn't
        if(attackLines.boundLineByBoundFigurePos[position]==null) {
            when {
                attackLines.noCheck -> forReachableTakingMoves(position, game, informOf)
                attackLines.isSingleCheck -> {
                    val attackerPos = attackLines.checkLines.first().attackerPos
                    if(isReachable(position, attackerPos, game)) {
                        informOf(Move[position, attackerPos])
                    }
                }
            }
        }
    }

    override fun isSelectable(position: Position, game: ChessBoard): Boolean {
        forEachReachablePos(position, game) { reachablePos ->
            if (!isBound(position, reachablePos, game)) return true
        }
        return false
    }

    override fun countReachableMoves(position: Position, game: StaticChessBoard): Int {
        var reachableMovesCount = 0
        forEachReachablePos(position, game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }

    private inline fun forEachReachablePos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            if(isAccessible(game, pos)) informOf(pos)
        }
    }

    private inline fun forEachPossiblePos(position: Position, game: ChessBoard, informOf: (Position) -> Unit) {
        val attackLines = game.getCachedAttackLines()
        // a bound knight can't move at all
        if(attackLines.boundLineByBoundFigurePos[position]==null) {
            when {
                attackLines.noCheck -> forEachReachablePos(position, game, informOf)
                attackLines.isSingleCheck -> {
                    attackLines.checkLines.first().posProgression.forEachReachablePos { checkLinePos ->
                        if(isReachable(position, checkLinePos, game)) {
                            informOf(checkLinePos)
                        }
                    }
                }
            }
        }
    }

    private inline fun forEachReachableTakeableEndPos(position: Position, game: StaticChessBoard, informOf: (Position) -> Unit) {
        position.forEachKnightPos { pos ->
            game.getFigureOrNull(pos)?.let {figure->
                if(figure.isWhite!=isWhite) informOf(pos)
            }
        }
    }
}
