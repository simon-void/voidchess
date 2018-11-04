package voidchess

import voidchess.board.*
import voidchess.board.move.*
import java.util.*


class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

fun initSimpleChessBoard(gameDes: String): ChessBoard = ArrayChessBoard(gameDes)

fun initSimpleChessBoard(chess960: Int): ChessBoard = ArrayChessBoard().apply { init(chess960) }

fun initChessBoard(chess960: Int, vararg moveCodes: String): ChessGameInterface = ChessGame(chess960).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

fun ChessBoard.getPossibleMovesFrom(posCode: String): List<Move> {
    val moveList = LinkedList<Move>()
    val figure = getFigure(Position.byCode(posCode))
    figure.getPossibleMoves(this, moveList)
    return moveList
}

fun ChessGameInterface.getPossibleMovesFrom(posCode: String): List<Move> {
    val allMovesList = LinkedList<Move>()
    getPossibleMoves(allMovesList)
    val fromPos = Position.byCode(posCode)
    return allMovesList.filter { move -> move.from==fromPos }
}

fun ChessGameInterface.moves(moveCodes: Iterable<String>) {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

fun PositionProgression.toList(): List<Position> {
    val list = LinkedList<Position>()
    forEachReachablePos { position -> list.add(position) }
    return list
}