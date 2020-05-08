package voidchess

import voidchess.united.board.*
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.*


internal fun initChessGame(chess960: Int, vararg moveCodes: String): CentralChessGame = CentralChessGameImpl(
    chess960.toChess960Config()
).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun initChessGame(des: String, vararg moveCodes: String): CentralChessGame = CentralChessGameImpl(
    des.toManualConfig()
).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
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