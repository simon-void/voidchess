package voidchess

import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.common.board.other.StartConfig
import voidchess.common.helper.splitAndTrim
import voidchess.common.helper.toChess960Config
import voidchess.engine.board.EngineChessGameImpl
import voidchess.engine.board.EngineChessGame


internal fun initEngineChessGame(startConfig: StartConfig, vararg moveCodes: String): EngineChessGame =
    EngineChessGameImpl(
        startConfig,
        moveCodes.map { Move.byCode(it) }
    )

internal fun initEngineChessGame(chess960: Int, vararg moveCodes: String): EngineChessGame = initEngineChessGame(chess960.toChess960Config(), *moveCodes)
internal fun initEngineChessGame(des: String, vararg moveCodes: String): EngineChessGame = initEngineChessGame(des.toManualConfig(), *moveCodes)

internal fun Position.mirrorRow() = Position[7 - row, column]

internal fun Move.mirrorRow() = Move[from.mirrorRow(), to.mirrorRow()]

internal val EngineChessGame.completeMoveHistory: List<Move>
    get() = this.completeHistory.let { history ->
        if (history.isBlank()) {
            emptyList<Move>()
        } else {
            history.split(",").map { Move.byCode(it) }
        }
    }

internal fun EngineChessGameImpl.copyGameWithInvertedColors(): EngineChessGameImpl {
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

    return EngineChessGameImpl(copyDef.toManualConfig())
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
