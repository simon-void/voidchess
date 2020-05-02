package voidchess.engine.evaluation

import org.testng.annotations.Test
import voidchess.engine.board.EngineChessGameImpl
import voidchess.common.board.move.Move

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import voidchess.common.engine.NumericalEvaluation
import voidchess.copyGameWithInvertedColors
import voidchess.engine.evaluation.leaf.MiddleGameEval
import voidchess.initChessGame
import voidchess.mirrorRow
import voidchess.toManualConfig


internal class MinMaxEvalTest {
    @DataProvider(name = "gameWithObviousEvalProvider")
    fun obviousEvalProvider(): Array<Array<Any>> {
        val easyPruner = AllMovesOrNonePruner(1, 4, 3)
        return arrayOf(
                arrayOf(
                        EngineChessGameImpl("black 0 King-white-e3-4 King-black-g6-6 Pawn-white-a7-false".toManualConfig()),
                        easyPruner,
                        Move.byCode("g6-g5"),
                        -10.0..-8.0,
                        "pawn promotion")
        )
    }

    @Test(dataProvider = "gameWithObviousEvalProvider")
    fun testMinMaxEvaluatesToExpectedRange(game: EngineChessGameImpl, pruner: SearchTreePruner, move: Move, expectedEvalRange: ClosedRange<Double>, msg: String) {
        val dynamicEvaluation = MinMaxEval(pruner,
            MiddleGameEval
        )
        val (_, evaluation) = dynamicEvaluation.evaluateMove(game, move, null, BestResponseSet())
        require(evaluation is NumericalEvaluation)

        assertTrue(
                evaluation.numericValue in expectedEvalRange,
                "eval should be in range $expectedEvalRange but was: ${evaluation.numericValue}. msg: $msg")

        val (_, evaluationOfColorInverted) =
            dynamicEvaluation.evaluateMove(game.copyGameWithInvertedColors(), move.mirrorRow(), null, BestResponseSet())
        require(evaluationOfColorInverted is NumericalEvaluation)

        assertTrue(
                evaluationOfColorInverted.numericValue in expectedEvalRange,
                "eval should be in range $expectedEvalRange but was: ${evaluationOfColorInverted.numericValue}. msg: $msg")
    }

    @Test
    fun testEvaluateMoveHasNoSideEffects() {
        val des = "black 0 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false"
        val game = EngineChessGameImpl(des.toManualConfig())
        val dynamicEvaluation = MinMaxEval()

        dynamicEvaluation.evaluateMove(game, Move.byCode("a6-b6"), null, BestResponseSet())
        // invariance: evaluateMove must not change the game configuration
        assertEquals(game.toString(), des)
    }

    @Test
    fun testMinMaxScheme() {
        val game = initChessGame(518, "d2-d3", "d7-d6", "c1-g5")

        val dynamicEvaluation = MinMaxEval()

        val (_, value) = dynamicEvaluation.evaluateMove(game, Move.byCode("e7-e6"), null, BestResponseSet()) // the queen can be taken via g5-d8
        require(value is NumericalEvaluation)

        // The best move for white is obviously taking the queen
        // so the value of the move e7-e6 should be around -6
        // because a queen(9P) is exchanged against a bishop(3P).
        // Actually worse than than -6 because the white queen will probably move to d2 to gain space with the
        // expected sequence (after e7-e6) g5-d8 e8-d8 d1-d2
        val combinedValue = value.numericValue
        assertTrue(
                combinedValue < -6.0 && combinedValue > -7.5, // the queen mobility isn't worth a pawn so combined value shouldn't be worse than -7
                "Min-Max-computation out of bounds. expected value [-6, -7.5] but is: $combinedValue")
    }


    // code to test search tree functionality which has been removed currently (see branch feature/searchTree, which benchmarks 100x slower)

//    @Test(dataProvider = "provide moves to evaluate")
//    fun `test evaluateMove`(game: ChessGame, move: Move, pruner: SearchTreePruner) {
//        val dynamicEvaluation = EvaluatingMinMax(pruner, EvaluatingAsIsNow)
//        val root = dynamicEvaluation.evaluateMove(game, move)
//
//        assertEquals(root.latestMove, move)
//
//        fun verify(searchTree: SearchTree, expectedIsOpponent: Boolean, pathToHere: LinkedList<Move>) {
//            val history = pathToHere.joinToString()
//            assertEquals(searchTree.isThisOpponentsMove, expectedIsOpponent, "validate isThisOpponentsMove after $history")
//            assertEquals(searchTree.isLeaf, searchTree.subTrees.isEmpty(), "validate isLeaf after $history")
//            if(!searchTree.isLeaf) {
//                assertEquals(searchTree.moveResult, MoveResult.NO_END)
//            }
//
//            searchTree.subTrees.forEach { subTree->
//                pathToHere.add(subTree.latestMove)
//                verify(subTree, !expectedIsOpponent, pathToHere)
//                val undoneMove = pathToHere.removeLast()
//                assertEquals(undoneMove, subTree.latestMove)
//            }
//        }
//
//        verify(root, false, LinkedList<Move>().apply{add(root.latestMove)})
//    }
//
//    @DataProvider
//    fun `provide moves to evaluate`(): Array<Array<Any>> {
//        val game1 = ChessGame("white 0 King-white-g1-2 Pawn-white-g2-false Pawn-white-h2-false Rook-black-f4-2 King-black-e8-0")
//        val move1 = Move.byCode("g1-h1")
//        val game2 = ChessGame("white 0 King-white-g3-8 Rook-white-a4-2 King-black-g1-9")
//        val move2 = Move.byCode("a4-a1")
//        val game3 = ChessGame("white 0 King-white-g3-8 Rook-black-h3-2 King-black-g1-9")
//        val move3 = Move.byCode("g3-h3")
//        val game4 = ChessGame("black 9 King-white-c8-8 King-black-a8-9 Pawn-white-h4-false Pawn-black-h6-false Pawn-white-a6-false Pawn-white-b6-false Pawn-black-a7-false")
//        val move4 = Move.byCode("h6-h5")
//        val minimalPruner = SimplePruner(1,2,2)
//        return arrayOf(
//                arrayOf( game1, move1, minimalPruner),
//                arrayOf( game1.copyGameWithInvertedColors(), move1.mirrorRow(), minimalPruner),
//                arrayOf( game2, move2, minimalPruner),
//                arrayOf( game3, move3, minimalPruner),
//                arrayOf( game4, move4, minimalPruner)
//        )
//    }
//
//    @Test(dataProvider = "provide games to check next moves in")
//    fun `test expectedNextMoves`(game: ChessGame, move: Move, pruner: SearchTreePruner, expectedNextMoves: String) {
//        val dynamicEvaluation = EvaluatingMinMax(pruner, EvaluatingAsIsNow)
//        val root = dynamicEvaluation.evaluateMove(game, move)
//
//        val actualExpectedMoves = root.expectedNextMoves().joinToString()
//        assertEquals(actualExpectedMoves, expectedNextMoves)
//    }
//
//    @DataProvider
//    fun `provide games to check next moves in`(): Array<Array<Any>> {
//        val game1 = ChessGame("white 8 Pawn-white-b2-false Pawn-black-b3-false Pawn-white-d3-false Pawn-black-d4-false Pawn-white-f4-false Pawn-black-f5-false King-white-f3-2 Rook-white-h2-2 King-black-g1-8")
//        val move1 = Move.byCode("h2-d2")
//        val full3MovesPruner = FullMovePruner(3)
//        val game2 = ChessGame("black 8 Pawn-white-c2-false Pawn-black-c3-false Pawn-black-c4-false Pawn-white-e4-false Pawn-black-e5-false Pawn-white-g4-false Pawn-black-g5-false Pawn-white-h5-false Pawn-black-h6-false King-white-e3-2 Rook-white-h2-2 King-black-f1-8")
//        val move2 = Move.byCode("f1-g1")
//        val full4MovesPruner = FullMovePruner(4)
//        return arrayOf(
//                arrayOf( game1, move1, full3MovesPruner, "g1-h1, f3-g3, h1-g1, d2-d1"),
//                arrayOf( game2, move2, full4MovesPruner, "h2-f2, g1-h1, e3-f3, h1-g1, f3-g3, g1-h1, f2-f1")
//        )
//    }
}
