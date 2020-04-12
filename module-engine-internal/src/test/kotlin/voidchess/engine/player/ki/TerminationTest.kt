package voidchess.engine.player.ki

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.engine.board.EngineChessGameImpl
import voidchess.common.board.move.Move
import voidchess.engine.player.ki.evaluation.*

import java.text.DecimalFormat
import java.text.NumberFormat

import org.testng.Assert.assertEquals
import voidchess.engine.board.EngineChessGame
import voidchess.initChessGame
import voidchess.toManualConfig
import kotlin.system.exitProcess


internal class TerminationTest {
    @DataProvider
    private fun getPlayData(): Array<Array<Any>> = arrayOf(
                arrayOf("black 0 King-white-g1-2 Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false Pawn-white-e4-false Bishop-black-b7 King-black-g8-2", listOf("e3-f2")),
                arrayOf("518", listOf("c2-c3")), arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false King-black-b7-3-false", emptyList<Any>()),
                arrayOf("black 0 King-white-h1-3 Pawn-white-c7-false Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false Knight-black-a7 King-black-b7-3-false", emptyList<Any>()),
                arrayOf("621", listOf("f2-f3")), arrayOf("black 0 Bishop-white-b1 King-white-h1-3 Pawn-black-f7-false King-black-e8-0 Rook-black-f8-0", emptyList<Any>()),
                arrayOf("white 0 Rook-black-e1-8 Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 Knight-white-g4 Queen-black-e5 King-black-g7-3", emptyList<Any>()),
                arrayOf("314", emptyList<Any>()), arrayOf("621", listOf("g2-g3", "f7-f6", "c2-c3", "g8-b3", "d1-c2")),
                arrayOf("707", listOf("e1-f3", "b7-b6", "f1-e3", "g7-g6")))

    @Test(dataProvider = "getPlayData")
    fun testPlay(chess960IndexOrDesc: String, moveDescs: List<String>) {

        val game: EngineChessGame
        val moveArray = moveDescs.toTypedArray()
        game = try {
            val chess960 = chess960IndexOrDesc.toInt()
            initChessGame(chess960, *moveArray)
        } catch (e: NumberFormatException) {
            initChessGame(chess960IndexOrDesc, *moveArray)
        }

        testTermination(game)
    }

    @Test
    fun testInvarianz() {
        val des = "black 0 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false " + "Pawn-black-b7-false King-black-e8-0 Rook-black-h8-3"

        val game = initChessGame(des, "b7-b5")
        testTermination(game)
        val newDes = "white 1 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false " + "Pawn-black-b5-true King-black-e8-0 Rook-black-h8-3"
        assertEquals(game.toString(), newDes)
    }

    private fun testTermination(game: EngineChessGame, pruner: SearchTreePruner = PrunerWithIrreversibleMoves(1, 1, 2, 2), staticEvaluation: EvaluatingStatically = EvaluatingAsIsNow) {
        val numberFormat = NumberFormat.getPercentInstance()
        val dynamicEvaluation = EvaluatingMinMax(pruner, staticEvaluation)

        val possibleMoves = game.getAllMoves()
        val numberOfPossibleMoves = possibleMoves.size.toDouble()
        var moveIndex = 0
        try {
            for (move in possibleMoves) {
                dynamicEvaluation.evaluateMove(game, move)
                println(numberFormat.format(++moveIndex / numberOfPossibleMoves))
            }
        } catch (e: Exception) {
            val gameString = game.toString()
            throw RuntimeException(
                    e.toString() + "-after Moves:"
                            + game.completeHistory + " -leading to position:"
                            + gameString)
        } catch (e: AssertionError) {
            val extendedE = AssertionError(e.message + " History:" + game.completeHistory)
            extendedE.stackTrace = e.stackTrace
            throw extendedE
        }

    }

    internal fun testTermination(game: EngineChessGameImpl, pruner: SearchTreePruner, move: Move) {
        val initDescription = game.toString()
        val dynamicEvaluation = EvaluatingMinMax(pruner, EvaluatingAsIsNow)

        val possibleMoves = game.getAllMoves()

        if (!possibleMoves.contains(move)) {
            throw RuntimeException("suggested move:" + move.toString()
                    + "not possible in initial position: " + initDescription)
        }

        try {
            dynamicEvaluation.evaluateMove(game, move)
            // Invariante: evaluateMove darf game nicht ändern
            val msg = "after Move:" + move.toString() + " History:" + game.shortTermHistory
            assertEquals(game.toString(), initDescription, msg)
        } catch (e: Exception) {
            val gameToString = game.toString()
            throw RuntimeException(e.toString() + "-after Moves:" + game.shortTermHistory + " -leading to position:"
                    + gameToString)
        } catch (e: AssertionError) {
            val extendedE = AssertionError(e.message + " History:" + game.shortTermHistory)
            extendedE.stackTrace = e.stackTrace
            throw extendedE
        }

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 1) {
                when (args[0]) {
                    "benchmark1" -> benchmark(false)
                    "benchmark2" -> benchmark(true)
                }
            } else {
                loadTest()
            }
        }

        private fun benchmark(longTest: Boolean) {
            val moves: Array<String> = mutableListOf<String>().apply {
                add("e2-e4")
                add("e7-e5")
                add("g1-f3")
                add("b8-c6")
                add("f1-b5")
                add("f8-c5")
                if(longTest) {
                    add("d2-d3")
                    add("d7-d6")
                    add("b1-c3")
                    add("c8-g4")
                }
            }.toTypedArray()

            val game = initChessGame(518, *moves)

            val pruner = PrunerWithIrreversibleMoves(1, 2, 4, 2)
            val staticEvaluation = EvaluatingAsIsNow//new EvaluatingToConstant();//
            loadTest(game, pruner, staticEvaluation, "Benchmark" + if (longTest) "2" else "1")
        }

        private fun loadTest() {
            // Loadtest
            println("Loadtest: Start")

            // Grundaufstellung ohne Bauern
            val des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                    + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                    + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
            loadTest(des)

            // Grundaufstellung mit Bauern vor König und ohne Läufer
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 "
//                    + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false " + "Rook-black-a8-0 Knight-black-b8 "
//                    + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0")
            // loadTest( des );

            // Grundaufstellung mit Bauern vor König und ohne Königsläufer
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//                    + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//                    + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0")
            // loadTest( des );

            // Grundaufstellung mit Bauern vor König
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//                    + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//                    + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
            // loadTest( des );

            // Grundaufstellung mit Bauern vor König und ohne Damen
//            des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//                    + "King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
//                    + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//                    + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//                    + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//                    + "King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
            // loadTest( des );

            // Zeit von
            // Grundaufstellung mit Bauern vor König und ohne Königsläufer
            // ist in etwa so groß wie
            // Grundaufstellung mit Bauern vor König und ohne Dame!!! Warum?
            exitProcess(0)
        }

        private fun loadTest(des: String) {
            val game = EngineChessGameImpl(des.toManualConfig())
            val pruner = PrunerWithIrreversibleMoves(2, 3, 4, 3)
            val staticEvaluation = EvaluatingAsIsNow
            loadTest(game, pruner, staticEvaluation, "Loadtest")
        }

        private fun loadTest(game: EngineChessGame, pruner: SearchTreePruner, staticEvaluation: EvaluatingStatically, type: String) {
            val decimalFormat = DecimalFormat("#.0")
            val computerPlayer = TerminationTest()

            try {
                println("$type: Berechnung gestartet")
                val time = System.currentTimeMillis()
                computerPlayer.testTermination(game, pruner, staticEvaluation)
                println("Dauer: " + decimalFormat.format((System.currentTimeMillis() - time) / 1000.0) + "s")
            } catch (e: RuntimeException) {
                println(type + "fehler:" + e.message)
            } finally {
                println("$type: Ende")
            }
        }
    }

}
