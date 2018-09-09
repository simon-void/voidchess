package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.helper.Direction
import voidchess.helper.Move
import voidchess.helper.Position
import voidchess.image.ImageType

/**
 * @author stephan
 */
class Queen(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.QUEEN) {

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val direction = position.getDirectionTo(to)

        if (direction == null) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(to)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP, informOf)
        forEachReachablePos(game, Direction.LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN, informOf)
        forEachReachablePos(game, Direction.RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move.get(position, it))
        }
    }

    override fun isSelectable(game: SimpleChessBoardInterface): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: BasicChessGameInterface): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }

//    private val bishop: Bishop
//    private val rock: Rock
//
//    init {
//        bishop = Bishop(isWhite, startPosition)
//        rock = Rock(isWhite, startPosition)
//    }
//
//    override fun figureMoved(move: Move) {
//        bishop.figureMoved(move)
//        rock.figureMoved(move)
//        super.figureMoved(move)
//    }
//
//    override fun undoMove(oldPosition: Position) {
//        bishop.undoMove(oldPosition)
//        rock.undoMove(oldPosition)
//        super.undoMove(oldPosition)
//    }
//
//    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
//        return bishop.isReachable(to, game) || rock.isReachable(to, game)
//    }
//
//    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
//        bishop.getReachableMoves(game, result)
//        rock.getReachableMoves(game, result)
//    }
//
//    override fun isSelectable(game: SimpleChessBoardInterface): Boolean {
//        return rock.isSelectable(game) || bishop.isSelectable(game)
//    }
//
//    override fun countReachableMoves(game: BasicChessGameInterface): Int {
//        return rock.countReachableMoves(game) + bishop.countReachableMoves(game)
//    }
}
