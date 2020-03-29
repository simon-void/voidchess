package voidchess

import voidchess.board.*
import voidchess.board.move.*
import voidchess.helper.splitAndTrim
import java.util.*


internal class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) : ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

internal fun initSimpleChessBoard(gameDes: String): ChessBoard = ArrayChessBoard(gameDes)

internal fun initSimpleChessBoard(chess960: Int): ChessBoard = ArrayChessBoard().apply { init(chess960) }

internal fun initChessBoard(chess960: Int, vararg moveCodes: String): ChessGameInterface = ChessGame(chess960).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun ChessBoard.getPossibleMovesFrom(posCode: String): List<Move> {
    val moveList = LinkedList<Move>()
    val figure = getFigure(Position.byCode(posCode))
    figure.getPossibleMoves(this, moveList)
    return moveList
}

internal fun ChessGameInterface.getPossibleMovesFrom(posCode: String): List<Move> {
    val allMovesList = getAllMoves()
    val fromPos = Position.byCode(posCode)
    return allMovesList.filter { move -> move.from==fromPos }
}

internal fun ChessGameInterface.moves(moveCodes: Iterable<String>) {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun PositionProgression.toList(): List<Position> {
    val list = LinkedList<Position>()
    forEachReachablePos { position -> list.add(position) }
    return list
}

internal fun Collection<Move>.toFromPosAsStringSet(): Set<String> = asSequence().map { it.from.toString() }.toSet()
internal fun Collection<Move>.toTargetPosAsStringSet(): Set<String> = asSequence().map { it.to.toString() }.toSet()

internal fun Position.mirrorRow() = Position[7-row, column]

internal fun Move.mirrorRow() = Move[from.mirrorRow(), to.mirrorRow()]

internal fun ChessGame.copyGameWithInvertedColors(): ChessGame {
    val intermidiate = "switching"
    val copyDef = toString()
            // switch white and black
            .replace("white", intermidiate)
            .replace("black", "white")
            .replace(intermidiate, "black")
            // mirror positions
            .splitAndTrim(' ')
            .joinToString(" ") { token ->
                if (token.contains('-')) {
                    val figureDef = ArrayList<String>(token.splitAndTrim('-'))
                    figureDef[2] = Position.byCode(figureDef[2]).mirrorRow().toString()
                    figureDef.joinToString("-")
                } else token
            }

    return ChessGame(copyDef)
}