package voidchess.common

import voidchess.common.board.*
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.common.board.move.PositionProgression
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.Figure
import kotlin.test.assertEquals


fun initChessBoard(gameDes: String): ChessBoard =
    ArrayChessBoard(gameDes.toManualConfig())

fun initChessBoard(chess960: Int, vararg moveCodes: String): ChessBoard = ArrayChessBoard(
    StartConfig.Chess960Config(chess960)
).apply {
    for (moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

fun ChessBoard.getPossibleMovesFrom(posCode: String): List<Move> {
    val figure = getFigure(Position.byCode(posCode))
    return figure.getPossibleMoves(this)
}

fun ChessBoard.isMovable(from: Position, to: Position, isWhite: Boolean): Boolean {
    val figure = this.getFigure(from)
    assertEquals(isWhite, figure.isWhite, "color")
    return figure.isMovable(to, this)
}

fun StaticChessBoard.assertFiguresKnowTherePosition() {
    val figuresWhichAreWrongAboutTheirPosition = mutableListOf<Pair<Position, Figure>>()
    for (row in 0..7) {
        for (column in 0..7) {
            val pos = Position[row, column]
            getFigureOrNull(pos)?.let { figure ->
                if (pos.notEqualsPosition(figure.position)) {
                    figuresWhichAreWrongAboutTheirPosition.add(pos to figure)
                }
            }
        }
    }
    if (figuresWhichAreWrongAboutTheirPosition.isNotEmpty()) {
        throw AssertionError("these figures are wrong about their position: " + figuresWhichAreWrongAboutTheirPosition.joinToString())
    }
}

fun PositionProgression.toList(): List<Position> {
    val list = mutableListOf<Position>()
    forEachReachablePos { position -> list.add(position) }
    return list
}

fun Collection<Move>.toFromPosAsStringSet(): Set<String> = asSequence().map { it.from.toString() }.toSet()
fun Collection<Move>.toTargetPosAsStringSet(): Set<String> = asSequence().map { it.to.toString() }.toSet()

fun String.toManualConfig(): StartConfig.ManualConfig {
    val gameDesc = this
    val gameDescParts = this.split(" ").filter { it.isNotEmpty() }
    check(gameDescParts.size >= 4) { "expected gameDescription, found something else: $gameDesc" }
    val isWhiteTurn = gameDescParts[0] == "white"
    val numberOfMovesSinceHitFigure = gameDescParts[1].toInt()
    val figureStates = gameDescParts.filterIndexed { index, _ -> index > 1 }
    return StartConfig.ManualConfig(isWhiteTurn, numberOfMovesSinceHitFigure, figureStates)
}

fun Int.toChess960Config(): StartConfig.Chess960Config {
    val chess960Index = this
    check(chess960Index in 0 until 960) { "expected value to be within 0-959 but was: $chess960Index" }
    return StartConfig.Chess960Config(chess960Index)
}

fun Figure.getPossibleMoves(game: ChessBoard): List<Move> {
    val moves = mutableListOf<Move>()
    this.forPossibleMoves(game) {
        moves.add(it)
    }
    return moves
}

fun Figure.getPossibleTakingMoves(game: ChessBoard): List<Move> {
    val moves = mutableListOf<Move>()
    this.forPossibleTakingMoves(game) {
        moves.add(it)
    }
    return moves
}

fun Figure.getReachableMoves(game: ChessBoard): List<Move> {
    val moves = mutableListOf<Move>()
    this.forReachableMoves(game) {
        moves.add(it)
    }
    return moves
}

fun Figure.getCriticalMoves(game: ChessBoard): Set<Move> {
    val moves = mutableSetOf<Move>()
    this.forCriticalMoves(game, moves)
    return moves
}

fun Figure.getReachableCheckingMoves(game: ChessBoard): List<Move> {
    val moves = mutableListOf<Move>()
    this.forReachableCheckingMoves(game) {
        moves.add(it)
    }
    return moves
}
