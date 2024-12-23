package voidchess.app

import voidchess.app.inner.board.CentralChessGame
import voidchess.app.inner.board.CentralChessGameImpl
import voidchess.common.board.move.*
import voidchess.common.board.other.StartConfig
import voidchess.common.integration.ColdPromise
import voidchess.common.helper.RuntimeFacade
import voidchess.common.integration.ColdPromiseImpl
import voidchess.common.integration.ComputerPlayerUI
import voidchess.common.integration.TableAdapter


class Table (
    private val engineAdapter: EngineAdapter,
) : TableAdapter {

    private val game: CentralChessGame = CentralChessGameImpl()

    fun postConstruct(computerPlayerUI: ComputerPlayerUI) {
        engineAdapter.postConstruct(computerPlayerUI, game)
    }

    override fun computerStartsGame(startConfig: StartConfig): ColdPromise<ComputerMoveResult> {
        engineAdapter.gameStarts()
        game.initGame(startConfig)

        return getComputerMovePromise()
    }

    override fun humanStartsGame(startConfig: StartConfig) {
        game.initGame(startConfig)
        engineAdapter.gameStarts()
    }

    override fun move(move: Move): HumanMoveResult = getHumanMoveResult(move)

    private fun getHumanMoveResult(move: Move): HumanMoveResult {
        val moveResultType = game.move(move)
        val extendedMove = game.getLatestExtendedMove()

        return if (moveResultType == MoveResultType.NO_END) {
            HumanMoveResult.Ongoing(
                extendedMove,
                getComputerMovePromise()
            )
        } else {
            engineAdapter.gameEnds(moveResultType, false)
            HumanMoveResult.GameEnds(extendedMove)
        }
    }

    private fun getComputerMovePromise(): ColdPromise<ComputerMoveResult> =
        ColdPromiseImpl {
            val move = engineAdapter.play()
            val moveResultType = game.move(move)
            val extendedMove = game.getLatestExtendedMove()

            if (game.isCheck) engineAdapter.gaveCheck()

            if (moveResultType == MoveResultType.NO_END) {
                ComputerMoveResult.Ongoing(extendedMove)
            } else {
                engineAdapter.gameEnds(moveResultType, true)
                ComputerMoveResult.GameEnds(extendedMove)
            }.also {
                RuntimeFacade.collectGarbage()
            }
        }

    override fun resign() {
        engineAdapter.gameEnds(MoveResultType.RESIGN, true)
    }
}
