package voidchess

import voidchess.united.board.*
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.*
import voidchess.common.helper.toChess960Config


internal fun initCentralChessGame(chess960: Int, vararg moveCodes: String): CentralChessGame = CentralChessGameImpl(
    chess960.toChess960Config()
).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun initCentralChessGame(des: String, vararg moveCodes: String): CentralChessGame = CentralChessGameImpl(
    des.toManualConfig()
).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}
