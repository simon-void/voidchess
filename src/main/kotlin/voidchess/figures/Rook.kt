package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.getKing
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


class Rook : CastlingFigure {

    constructor(isWhite: Boolean, startPosition: Position) : super(isWhite, startPosition, FigureType.ROOK)
    constructor(isWhite: Boolean, startPosition: Position, stepsTaken: Int) : super(isWhite, startPosition, stepsTaken, FigureType.ROOK)

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isDiagonal) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP, informOf)
        forEachReachablePos(game, Direction.LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN, informOf)
        forEachReachablePos(game, Direction.RIGHT, informOf)
    }

    private inline fun forEachReachableTakeableEndPos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forReachableTakeableEndPos(game, Direction.UP, informOf)
        forReachableTakeableEndPos(game, Direction.LEFT, informOf)
        forReachableTakeableEndPos(game, Direction.DOWN, informOf)
        forReachableTakeableEndPos(game, Direction.RIGHT, informOf)
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

    override fun getReachableCheckingMoves(game: ChessBoard, result: MutableCollection<Move>) {
        val opponentKingPos = game.getKing(!isWhite).position
        val currentPos = position
        val possiblePos1 = Position[currentPos.row, opponentKingPos.column]
        val possiblePos2 = Position[opponentKingPos.row, currentPos.column]
        // check position 1
        if(isReachable(possiblePos1, game)) {
            val figureTaken = game.move(this, possiblePos1)
            if(isReachable(opponentKingPos, game)) {
                result.add(Move[currentPos, possiblePos1])
            }
            game.undoMove(this, currentPos, figureTaken)
        }
        // check position 2
        if(isReachable(possiblePos2, game)) {
            val figureTaken = game.move(this, possiblePos2)
            if(isReachable(opponentKingPos, game)) {
                result.add(Move[currentPos, possiblePos2])
            }
            game.undoMove(this, currentPos, figureTaken)
        }
    }

    override fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableCollection<Move>) {
        when {
            checkLine.posProgression.hasSinglePos -> {
                addMoveIfReachable(checkLine.attackerPos, game, result)
            }
            checkLine.isDiagonalCheck -> {
                var hasAlreadyAddedAPosition = false
                checkLine.posProgression.forEachReachablePos {diagonalPos->
                    if(addMoveIfReachable(diagonalPos, game, result)) {
                        // a rook can only intersect with a diagonal attacker at max two points
                        if(hasAlreadyAddedAPosition) return
                        else hasAlreadyAddedAPosition = true
                    }
                }
            }
            else -> { // isStraightCheck!
                checkLine.posProgression.forEachReachablePos {straightPos->
                    if(addMoveIfReachable(straightPos, game, result)) {
                        // a rook can only intersect with a straight attacker at one point
                        return
                    }
                }
            }
        }
    }

    override fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableCollection<Move>) {
        if(boundLine.boundFigureToAttackerDirection.isStraight) {
            boundLine.possibleMovesToAttacker.forEachReachablePos {posBetweenThisAndAttacker->
                result.add(Move[position, posBetweenThisAndAttacker])
            }
            boundLine.possibleMovesToKing.forEachReachablePos {posBetweenThisAndKing->
                result.add(Move[position, posBetweenThisAndKing])
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
}
