package voidchess

import voidchess.board.*
import voidchess.common.board.move.*
import voidchess.figures.Figure
import voidchess.figures.King
import voidchess.figures.Knight
import java.util.*


class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) :
    ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

fun initSimpleChessBoard(gameDes: String): ChessBoard =
    ArrayChessBoard(gameDes)

fun initSimpleChessBoard(chess960: Int): ChessBoard = ArrayChessBoard().apply {
    init(chess960)
}

fun ChessGameInterface.getPossibleMovesFrom(posCode: String): Collection<Move> {
    val game = this
    val fromPos = Position.byCode(posCode)
    val figure = game.getFigureOrNull(fromPos) ?: return emptyList()
    val isWhite = figure.isWhite
    val moves = mutableListOf<Move>()

    fun fillWithPossibleKnightMoves() {
        fromPos.forEachKnightPos { toPos ->
            if (game.isMovable(fromPos, toPos, isWhite)) {
                moves.add(Move[fromPos, toPos])
            }
        }
    }

    fun fillWithPossibleKingMoves() {
        Direction.values().forEach { direction ->
            fromPos.forEachPosInLine(direction) { toPos ->
                val isHorizontal = direction.isHorizontal
                val isFreeArea = game.isFreeArea(toPos)
                val isMovable = game.isMovable(fromPos, toPos, isWhite)
                if (isMovable) {
                    moves.add(Move[fromPos, toPos])
                }
                return@forEachPosInLine !(isHorizontal && isFreeArea)
            }
        }
    }

    fun fillWithPossibleStraightMove() {
        Direction.values().forEach { direction ->
            fromPos.forEachPosInLine(direction) { toPos ->
                val isMovable = game.isMovable(fromPos, toPos, isWhite)
                if (isMovable) {
                    moves.add(Move[fromPos, toPos])
                }
                return@forEachPosInLine !isMovable
            }
        }
    }
    when (figure) {
        is Knight -> fillWithPossibleKnightMoves()
        is King -> fillWithPossibleKingMoves()
        else -> fillWithPossibleStraightMove()
    }
    return moves
}

fun Figure.isMovable(posCode: String, game: ChessGameInterface): Boolean = game.isMovable(position, Position.byCode(posCode), isWhite)

fun PositionProgression.toList(): List<Position> {
    val list = LinkedList<Position>()
    forEachReachablePos { position -> list.add(position) }
    return list
}

fun Position.mirrorRow() = Position[7 - row, column]
