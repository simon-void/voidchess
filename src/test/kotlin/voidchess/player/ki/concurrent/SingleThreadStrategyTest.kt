package voidchess.player.ki.concurrent

import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.ChessGameInterface
import voidchess.board.move.Move
import voidchess.player.ki.evaluation.*
import java.util.NavigableSet

import org.testng.Assert.*


class SingleThreadStrategyTest {
    private lateinit var strategy: SingleThreadStrategy

    //best move with matt
    //color inverted matt
    //best move with value
    //best matt move in 2
    val bestMoveBoardsProvider: Array<Array<Any>>
        @DataProvider(name = "bestMoveProvider")
        get() = arrayOf(arrayOf(1, ChessGame("black 0 King-white-h1-3 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f4-2 King-black-e8-0"), Move.byCode("f4-f1")), arrayOf(1, ChessGame("white 0 King-black-h8-3 Pawn-black-g7-false Pawn-black-h7-false Rook-white-f4-2 King-white-e1-0"), Move.byCode("f4-f8")), arrayOf(1, ChessGame("white 0 King-white-e1-0 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f2-2 King-black-e8-0"), Move.byCode("e1-f2")), arrayOf(2, ChessGame("white 0 Bishop-white-a1 Rook-black-b2-2 Knight-black-c2 Pawn-white-d6-false King-white-g6-8 King-black-h8-4"), Move.byCode("d6-d7")), arrayOf(1, ChessGame(518, "d2-d3", "d7-d6", "c1-g5", "e7-e6"), Move.byCode("g5-d8")))

    //worst move goes into matt
    //color inverted worst move goes into matt
    //worst move by value
    val worstMoveBoardsProvider: Array<Array<Any>>
        @DataProvider(name = "worstMoveProvider")
        get() = arrayOf(arrayOf(1, ChessGame("white 0 King-white-g1-2 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f4-2 King-black-e8-0"), Move.byCode("g1-h1")), arrayOf(1, ChessGame("black 0 King-black-g8-2 Pawn-black-g7-false Pawn-black-h7-false Rook-white-f4-2 King-white-e1-0"), Move.byCode("g8-h8")), arrayOf(1, ChessGame("white 0 King-white-e1-0 Rook-black-f2-2 Queen-black-d1 King-black-e8-0"), Move.byCode("e1-f2")), arrayOf(1, ChessGame(518, "d2-d3", "d7-d6", "c1-g5"), Move.byCode("e7-e6")))

    val findBestResultData: Array<Array<Any>>
        @DataProvider
        get() {
            val mattIn2 = ChessGame("white 0 King-white-e3-2 Rook-white-e4-2 Bishop-white-f6 Rook-black-d8-2 Knight-black-c8 King-black-g8-1 Pawn-black-d7-false Pawn-black-e5-false Pawn-black-f7-false")
            val pattIn1 = ChessGame("white 0 King-white-a1-9 Rook-white-h2-2 Pawn-white-g2-false Rook-black-b2-5 King-black-g8-1 Pawn-black-b4-false Pawn-black-c3-false Pawn-black-g3-false Pawn-black-f7-false Pawn-black-g7-false")
            val twoMovesAheadPruner = PrunerWithIrreversibleMoves(2, 2, 2, 2)
            return arrayOf(arrayOf(mattIn2, twoMovesAheadPruner, CheckmateOther(2)), arrayOf(pattIn1, twoMovesAheadPruner, Draw))
        }

    @BeforeMethod
    fun setup() {
        strategy = SingleThreadStrategy { _, _ ->Unit}
    }

    @Test
    fun testSortOrder() {
        val game = ChessGame(518)
        val moves = evaluate(1, game).descendingIterator()

        var betterMove = moves.next()
        while (moves.hasNext()) {
            val worseMove = moves.next()
            assertTrue(
                    betterMove.value.compareTo(worseMove.value) >= 0,
                    "earlier move should be at least as good"
            )
            betterMove = worseMove
        }
    }

    @Test(dataProvider = "bestMoveProvider")
    fun testFindBestMoveIn(depth: Int, game: ChessGame, bestMove: Move) {
        val computedMove = getBestMoveInIn(depth, game).move
        assertEquals(computedMove, bestMove, "best move")
    }

    @Test(dataProvider = "worstMoveProvider")
    fun testFindWorstMoveIn(depth: Int, game: ChessGame, worstMove: Move) {
        val computedMove = getWorstMoveIn(depth, game).move
        assertEquals(computedMove, worstMove, "worst move")
    }

    @Test(dataProvider = "getFindBestResultData", dependsOnMethods = ["testFindBestMoveIn"])
    fun testFindBestResultIn(game: ChessGame, pruner: SearchTreePruner, expectedResult: Evaluated) {
        val resultSet = evaluate(pruner, game)
        val actualBestMoveResult = resultSet.last().value
        assertEquals(actualBestMoveResult, expectedResult, "expected result")
    }

    private fun getBestMoveInIn(depth: Int, game: ChessGameInterface): EvaluatedMove {
        val moves = evaluate(depth, game)
        return moves.last()
    }

    private fun getWorstMoveIn(depth: Int, game: ChessGameInterface): EvaluatedMove {
        val moves = evaluate(depth, game)
        return moves.first()
    }

    private fun evaluate(depth: Int, game: ChessGameInterface): NavigableSet<EvaluatedMove> {
        val pruner = PrunerWithIrreversibleMoves(depth, depth, depth + 1, depth + 1)
        return evaluate(pruner, game)
    }

    private fun evaluate(pruner: SearchTreePruner, game: ChessGameInterface): NavigableSet<EvaluatedMove> {
        val staticEval = StaticEvaluation
        val dynEval = DynamicEvaluation(pruner, staticEval)
        return strategy.evaluatePossibleMoves(game, dynEval)
    }
}
