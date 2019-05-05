package voidchess.player.ki.evaluation

import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.move.Position

import java.util.LinkedList

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue

class StaticSpaceEvaluationTest {
    private val evaluation = StaticSpaceEvaluation()

    @Test
    fun testEvaluation() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0"
        val game = ChessGame(des)

        val evaluateForWhite = evaluation.getPrimaryEvaluation(game, true)
        val evaluateForBlack = evaluation.getPrimaryEvaluation(game, false)

        val combinedEvalForWhite = getCombinedEvaluation(evaluateForWhite)
        val combinedEvalForBlack = getCombinedEvaluation(evaluateForBlack)

        assertTrue(combinedEvalForWhite < 0)
        assertTrue(combinedEvalForBlack > 0)
        assertEquals(combinedEvalForBlack, -combinedEvalForWhite)


        des = "black 0 King-white-e1-0 Rook-black-b8-0 King-black-e8-0"
        val game2 = ChessGame(des)

        val evaluateForWhite2 = evaluation.getPrimaryEvaluation(game2, true)
        assertTrue(evaluateForWhite2 < evaluateForWhite)
    }

    private fun getCombinedEvaluation(evaluated: Evaluated): Double {
        val ongoing = evaluated as Ongoing
        ongoing.setSecondaryEvaluation(0.0)
        return ongoing.getCombinedEvaluation()
    }

    @Test
    fun testGetMaxFigureDistance() {
        assertEquals(evaluation.getKingDistance(Position.byCode("e1"), Position.byCode("e8")), 7)
        assertEquals(evaluation.getKingDistance(Position.byCode("b7"), Position.byCode("e8")), 4)
    }

    @Test
    fun testCountRestSpace() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0"
        var game = ChessGame(des)

        val otherPositions = LinkedList<Position>()
        otherPositions.add(Position.byCode("e8"))
        otherPositions.add(Position.byCode("a8"))
        val kingPos = Position.byCode("e1")

        assertEquals(evaluation.countRestSpace(game, game.getFigureOrNull(kingPos)!!, otherPositions), 48)

        des = "black 0 King-white-e1-0 Rook-black-e7-0 King-black-e8-0"
        game = ChessGame(des)

        otherPositions.clear()
        otherPositions.add(Position.byCode("e8"))
        otherPositions.add(Position.byCode("e7"))

        assertEquals(evaluation.countRestSpace(game, game.getFigureOrNull(kingPos)!!, otherPositions), 43)
    }
}
