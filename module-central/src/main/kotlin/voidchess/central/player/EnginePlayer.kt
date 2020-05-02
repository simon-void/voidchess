package voidchess.central.player

import voidchess.central.board.CentralChessGame
import voidchess.common.board.move.MoveResult
import voidchess.common.helper.RuntimeFacade
import voidchess.common.engine.Engine
import voidchess.common.engine.EngineAnswer
import voidchess.common.engine.EvaluatedMove
import voidchess.engine.KaiEngine
import voidchess.common.integration.ComputerPlayerUI
import voidchess.central.Table
import voidchess.common.board.move.ExtendedMove
import voidchess.common.engine.EngineConfig
import voidchess.common.integration.Thumb
import kotlin.system.measureTimeMillis

class EnginePlayer: Player {

    private val engine: Engine = KaiEngine(::setProgress)
    private var isWhite = false
    private lateinit var ui: ComputerPlayerUI
    private lateinit var table: Table
    private lateinit var game: CentralChessGame

    internal fun postConstruct(
        table: Table,
        ui: ComputerPlayerUI,
        game: CentralChessGame
    ) {
        this.ui = ui
        this.table = table
        this.game = game
    }

    private fun play() {
        fun <T: Any> ensureMinimumDurationInMs(minimumDuration: Int, f: ()->T): T {
            // TODO in Kotlin 1.4 because of contracts "lateinit var" should be replacable by "val"
            lateinit var result: T
            val lookUpDurationInMillies = measureTimeMillis {
                result = f()
            }
            val milliSecondsToWait = minimumDuration - lookUpDurationInMillies
            if (milliSecondsToWait > 0) {
                runCatching { Thread.sleep(milliSecondsToWait) }
            }
            return result
        }

        ui.setBubbleText(null)
        ui.showThoughts(true)

        // for ergonomic reasons lets set the minimum successful look-up time to 300ms
        val chosenMove: EvaluatedMove = ensureMinimumDurationInMs(300) {
            nextMove()
        }

        ui.setValue(chosenMove.value)
        table.move(chosenMove.move)

        RuntimeFacade.collectGarbage()
    }

    //lets see if the library contains a next move, else we compute the next move
    private fun nextMove(): EvaluatedMove {
        val movesSoFar: List<String> = game.getCompleteHistory().split(',').let {
            if (it.size == 1 && it.first() == "") {
                emptyList()
            } else {
                it
            }
        }
        val evaluatedMove = when (
            val engineAnswer = engine.evaluateMovesBestMoveFirst(movesSoFar, game.startConfig)
            ) {
            is EngineAnswer.Success -> engineAnswer.evaluatedMove
            is EngineAnswer.Error -> throw IllegalStateException("Engine threw an exception: ${engineAnswer.errorMsg}")
        }
        run {
            // since the engine is a potentially external component the suggested move has to be checked
            val move = evaluatedMove.move
            if(!game.isMovable(move.from, move.to)) throw IllegalStateException("illegal move $move! Figure can't move that way.")
        }
        return evaluatedMove
    }

    internal fun gameStarts(computerIsWhite: Boolean) {
        isWhite = computerIsWhite
        ui.reset()
    }

    override fun makeFirstMove() {
        play()
    }

    override fun makeAMove(opponentsMove: ExtendedMove) {
        play()
    }

    override fun gameEnds(endOption: MoveResult, lastMoveByWhite: Boolean) {
        ui.showThoughts(false)
        when (endOption) {
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
            else -> ui.setBubbleText(endOption.toString())
        }
    }

    override fun gaveCheck() {
        ui.setBubbleText("check")
    }

    private fun setProgress(computedMoves: Int, totalMoves: Int) {
        ui.setProgress(computedMoves, totalMoves)
    }

    fun getEngineConfig(): EngineConfig = engine.getConfig()
}

