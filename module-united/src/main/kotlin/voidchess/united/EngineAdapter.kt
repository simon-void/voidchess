package voidchess.united

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import voidchess.united.board.CentralChessGame
import voidchess.common.board.move.MoveResultType
import voidchess.common.engine.Engine
import voidchess.common.engine.EngineAnswer
import voidchess.common.engine.EvaluatedMove
import voidchess.engine.KaiEngine
import voidchess.common.integration.ComputerPlayerUI
import voidchess.common.board.move.Move
import voidchess.common.engine.EngineConfig
import voidchess.common.integration.Thumb
import kotlin.system.measureTimeMillis

class EngineAdapter {

    private val engine: Engine = KaiEngine(::setProgress)
    private lateinit var ui: ComputerPlayerUI
    private lateinit var game: CentralChessGame

    internal fun postConstruct(
        ui: ComputerPlayerUI,
        game: CentralChessGame
    ) {
        this.ui = ui
        this.game = game
    }

    suspend fun play(): Move = coroutineScope {
        suspend fun <T: Any> ensureMinimumDurationInMs(minimumDuration: Int, f: suspend ()->T): T {
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

        val ergonomicMinMoveDurationInMS = 300
        val chosenMove: EvaluatedMove = ensureMinimumDurationInMs(ergonomicMinMoveDurationInMS) {
            nextMove()
        }

        ui.setValue(chosenMove.value)

        chosenMove.move
    }

    //lets see if the library contains a next move, else we compute the next move
    private suspend fun nextMove(): EvaluatedMove {
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

    internal fun gameStarts() {
        ui.reset()
    }

    internal fun gameEnds(endOption: MoveResultType, lastMoveComputer: Boolean) {
        ui.showThoughts(false)
        when (endOption) {
            MoveResultType.DRAW -> ui.setBubbleText("draw")
            MoveResultType.STALEMATE -> ui.setBubbleText("stalemate")
            MoveResultType.CHECKMATE -> {
                if (lastMoveComputer) {
                    ui.setBubbleText("checkmate")
                    ui.setThumb(Thumb.DOWN)
                } else {
                    ui.setBubbleText("good game")
                    ui.setThumb(Thumb.UP)
                }
            }
            MoveResultType.THREE_TIMES_SAME_POSITION -> ui.setBubbleText("draw because of\n3x repetition")
            MoveResultType.FIFTY_MOVES_NO_HIT -> ui.setBubbleText("draw because of\n50-move rule")
            MoveResultType.RESIGN -> ui.setBubbleText("you're already\nresigning ?!")
            else -> ui.setBubbleText(endOption.toString())
        }
    }

    internal fun gaveCheck() {
        ui.setBubbleText("check")
    }

    private suspend fun setProgress(computedMoves: Int, totalMoves: Int) = coroutineScope {
        if(isActive) {
            ui.setProgress(computedMoves, totalMoves)
        }
    }

    fun getEngineConfig(): EngineConfig = engine.getConfig()
}

