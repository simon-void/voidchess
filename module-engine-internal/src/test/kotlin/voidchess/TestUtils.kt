package voidchess

import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.board.other.ChessGameSupervisor
import voidchess.common.board.other.StartConfig
import voidchess.common.helper.splitAndTrim
import voidchess.engine.board.ChessGame
import voidchess.engine.board.ChessGameInterface
import java.util.*


internal class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) :
    ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

internal fun initChessGame(chess960: Int, vararg moveCodes: String): ChessGameInterface =
    ChessGame(StartConfig.Chess960Config(chess960)).apply {
        for (moveCode in moveCodes) {
            move(Move.byCode(moveCode))
        }
    }

internal fun ChessGameInterface.moves(moveCodes: Iterable<String>) {
    for (moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun Position.mirrorRow() = Position[7 - row, column]

internal fun Move.mirrorRow() = Move[from.mirrorRow(), to.mirrorRow()]

internal fun ChessGame.copyGameWithInvertedColors(): ChessGame {
    val intermediate = "switching"
    val copyDef = toString()
        // switch white and black
        .replace("white", intermediate)
        .replace("black", "white")
        .replace(intermediate, "black")
        // mirror positions
        .splitAndTrim(' ')
        .joinToString(" ") { token ->
            if (token.contains('-')) {
                val figureDef = ArrayList(token.splitAndTrim('-'))
                figureDef[2] = Position.byCode(figureDef[2]).mirrorRow().toString()
                figureDef.joinToString("-")
            } else token
        }

    return ChessGame(copyDef.toManualConfig())
}

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