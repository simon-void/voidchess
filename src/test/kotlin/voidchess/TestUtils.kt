package voidchess

import org.mockito.Mockito
import voidchess.board.*
import voidchess.board.move.LastMoveProvider
import voidchess.board.move.Move
import voidchess.board.move.PawnPromotion
import voidchess.board.move.Position
import java.util.*


class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

fun initSimpleChessBoard(gameDes: String): SimpleChessBoardInterface = SimpleArrayBoard(gameDes, Mockito.mock(LastMoveProvider::class.java))

fun initSimpleChessBoard(chess960: Int): SimpleChessBoardInterface = SimpleArrayBoard(Mockito.mock(LastMoveProvider::class.java)).apply { init(chess960) }

fun initChessBoard(chess960: Int, vararg moveCodes: String): ChessGameInterface = ChessGame(chess960).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

fun SimpleChessBoardInterface.getPossibleMovesFrom(posCode: String): List<Move> {
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