package voidchess.central.player

import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.other.StartConfig
import voidchess.common.integration.HumanPlayer
import voidchess.common.integration.TableAdapter
import voidchess.central.Table
import voidchess.common.board.move.ExtendedMove


class UiPlayer : Player, TableAdapter {
    private lateinit var table: Table
    private lateinit var humanPlayer: HumanPlayer

    internal fun postConstruct(
        table: Table,
        humanPlayer: HumanPlayer
    ) {
        this.table = table
        this.humanPlayer = humanPlayer
    }

    override fun moved(move: Move): ExtendedMove {
        return table.move(move)
    }

    override fun resign() {
        table.stopGame(MoveResult.RESIGN)
    }

    override fun startGame(startConfig: StartConfig, humanIsWhite: Boolean) {
        table.startGame(startConfig, humanIsWhite)
    }

    override fun makeFirstMove() {
        humanPlayer.playFirstMove()
    }

    override fun makeAMove(opponentsMove: ExtendedMove) {
        humanPlayer.playAfter(opponentsMove)
    }

    override fun gameEnds(endOption: MoveResult, lastMoveByWhite: Boolean) {
        humanPlayer.gameEnds()
    }

    override fun gaveCheck() {}
}
