package voidchess.engine.player.ki.evaluation

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import voidchess.engine.board.ChessGame
import voidchess.common.board.move.Position
import voidchess.toManualConfig
import java.util.*

internal class EvaluatingSpaceTest {
    private val evaluation = EvaluatingSpace()

    @Test
    fun testEvaluation() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0"
        val game = ChessGame(des.toManualConfig())

        val evaluateForWhite = evaluation.getPreliminaryEvaluation(game, true)
        val evaluateForBlack = evaluation.getPreliminaryEvaluation(game, false)

        val combinedEvalForWhite = evaluation.addSecondaryEvaluationTo(evaluateForWhite, game, true)
        val combinedEvalForBlack = evaluation.addSecondaryEvaluationTo(evaluateForBlack, game, false)

        assertTrue(combinedEvalForWhite.fullEvaluation < 0)
        assertTrue(combinedEvalForBlack.fullEvaluation > 0)
        assertEquals(combinedEvalForBlack.fullEvaluation, -combinedEvalForWhite.fullEvaluation)


        des = "black 0 King-white-e1-0 Rook-black-b8-0 King-black-e8-0"
        val game2 = ChessGame(des.toManualConfig())

        val evaluateForWhite2 = evaluation.getPreliminaryEvaluation(game2, true)
        assertTrue(evaluateForWhite2 < evaluateForWhite)
    }

    @Test
    fun testCountRestSpace() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0"
        var game = ChessGame(des.toManualConfig())

        val otherPositions = LinkedList<Position>()
        otherPositions.add(Position.byCode("e8"))
        otherPositions.add(Position.byCode("a8"))
        val kingPos = Position.byCode("e1")

        assertEquals(evaluation.countRestSpace(game, game.getFigureOrNull(kingPos)!!, otherPositions), 48)

        des = "black 0 King-white-e1-0 Rook-black-e7-0 King-black-e8-0"
        game = ChessGame(des.toManualConfig())

        otherPositions.clear()
        otherPositions.add(Position.byCode("e8"))
        otherPositions.add(Position.byCode("e7"))

        assertEquals(evaluation.countRestSpace(game, game.getFigureOrNull(kingPos)!!, otherPositions), 43)
    }
}
