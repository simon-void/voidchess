package voidchess.player

import voidchess.board.ChessGameInterface
import voidchess.board.move.MoveResult
import voidchess.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.helper.RuntimeFacade
import voidchess.common.player.ki.Engine
import voidchess.engine.player.ki.KaiEngine
import voidchess.ui.ComputerPlayerUI
import voidchess.ui.TableInterface
import voidchess.ui.Thumb


class ComputerPlayer(private val table: TableInterface, private val game: ChessGameInterface, private val ui: ComputerPlayerUI) :
    PlayerInterface {
    private val engine: Engine =
        KaiEngine(ui::setProgress)
    private var isWhite = false



    override fun play() {
        ui.setBubbleText(null)
        ui.showThoughts(true)
        val chosenMove = nextMove()
        ui.setValue(chosenMove.value)
        table.move(chosenMove.move)

        RuntimeFacade.collectGarbage()
    }

    //lets see if the library contains a next move, else we compute the next move
    private fun nextMove() = engine.evaluateMovesBestMoveFirst(game.getCompleteHistory().split(','), game.startConfig)

    override fun askForPawnPromotionType(pawnPosition: Position) = PawnPromotion.QUEEN

    override fun gameStarts() {
        ui.reset()
    }

    override fun gameEnds(endoption: MoveResult, lastMoveByWhite: Boolean) {
        ui.showThoughts(false)
        when (endoption) {
            MoveResult.DRAW -> ui.setBubbleText("draw")
            MoveResult.STALEMATE -> ui.setBubbleText("stalemate")
            MoveResult.CHECKMATE -> {
                val didIWin = lastMoveByWhite == isWhite
                if (didIWin) {
                    ui.setBubbleText("checkmate")
                    ui.setThumb(Thumb.DOWN)
                } else {
                    ui.setBubbleText("good game")
                    ui.setThumb(Thumb.UP)
                }
            }
            MoveResult.THREE_TIMES_SAME_POSITION -> ui.setBubbleText("draw because of\n3x repetition")
            MoveResult.FIFTY_MOVES_NO_HIT -> ui.setBubbleText("draw because of\n50-move rule")
            MoveResult.RESIGN -> ui.setBubbleText("you're already\nresigning ?!")
            else -> ui.setBubbleText(endoption.toString())
        }
    }

    override fun gaveCheck() {
        ui.setBubbleText("check")
    }

    override fun setColor(isWhite: Boolean) {
        this.isWhite = isWhite
    }

    fun getEngineSpec() = engine.getSpec()

    fun setOption(name: String, value: String) {
        engine.setOption(name, value)
    }
}
