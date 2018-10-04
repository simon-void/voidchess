package voidchess.ui

import voidchess.board.MoveResult
import voidchess.helper.Move
import voidchess.player.PlayerInterface


interface TableInterface {
    fun startGame()
    fun stopGame(endoption: MoveResult)
    fun move(move: Move)
    fun setWhitePlayer(player: PlayerInterface)
    fun setBlackPlayer(player: PlayerInterface)
    fun switchPlayer()
}
