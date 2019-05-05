package voidchess.player.ki

import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.ChessGameSupervisorDummy
import voidchess.board.move.Move
import voidchess.helper.RuntimeFacade
import voidchess.player.ki.evaluation.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


fun main(args: Array<String>) {
    if (args.size != 1) {
        throw IllegalArgumentException("one parameter expected but ${args.size} were provided")
    }

    val namedOptions = mapOf(
            "benchmark" to { benchmark(true) },
            "benchmark2" to { benchmark(false) },
            "loadTest" to { loadTest() }
    )
    args[0].let { param ->
        val option = namedOptions[param]
                ?: throw IllegalArgumentException(
                        "unknown option: $param, available options: ${namedOptions.keys.joinToString(separator = ", ")}")
        option()
    }
}

class ComputerPlayerTest {
    @Test
    fun testPlay() {
        var des = ("black 0 King-white-g1-2 "
                + "Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false "
                + "Pawn-white-e4-false "
                + "Bishop-black-b7 "
                + "King-black-g8-2")

        var game = ChessGame(des)
        game.move(Move.byCode("e3-f2"))
        testTermination(game)

        game = ChessGame(ChessGameSupervisorDummy)
        game.move(Move.byCode("c2-c3"))
        testTermination(game)

        des = "black 0 King-white-h1-3 Pawn-white-c7-false " + "King-black-b7-3-false"

        game = ChessGame(des)
        testTermination(game)

        des = ("black 0 King-white-h1-3 Pawn-white-c7-false "
                + "Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
                + "Knight-black-a7 King-black-b7-3-false")

        game = ChessGame(des)
        testTermination(game)

        game = ChessGame(621)
        game.move(Move.byCode("f2-f3"))
        testTermination(game)

        des = "black 0 Bishop-white-b1 King-white-h1-3 " + "Pawn-black-f7-false King-black-e8-0 Rook-black-f8-0"

        game = ChessGame(des)
        testTermination(game)

        des = ("white 0 Rook-black-e1-8 "
                + "Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 "
                + "Knight-white-g4 Queen-black-e5 King-black-g7-3")

        game = ChessGame(des)
        testTermination(game)

        game = ChessGame(314)
        testTermination(game)

        game = ChessGame(621)
        game.move(Move.byCode("g2-g3"))
        game.move(Move.byCode("f7-f6"))
        game.move(Move.byCode("c2-c3"))
        game.move(Move.byCode("g8-b3"))
        game.move(Move.byCode("d1-c2"))
        testTermination(game)

        game = ChessGame(707)
        game.move(Move.byCode("e1-f3"))
        game.move(Move.byCode("b7-b6"))
        game.move(Move.byCode("f1-e3"))
        game.move(Move.byCode("g7-g6"))
        testTermination(game)
    }

    @Test
    fun testInvariance() {
        val des = "black 0 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false " + "Pawn-black-b7-false King-black-e8-0 Rook-black-h8-3"

        val game = ChessGame(des)
        game.move(Move.byCode("b7-b5"))
        testTermination(game)
        val newDes = "white 1 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false " + "Pawn-black-b5-true King-black-e8-0 Rook-black-h8-3"
        assertEquals(game.toString(), newDes)
    }
}

private fun benchmark(useFasterPruner: Boolean) {
    val game = ChessGame()
    game.move(Move.byCode("e2-e4"))
    game.move(Move.byCode("e7-e5"))
    game.move(Move.byCode("g1-f3"))
    game.move(Move.byCode("b8-c6"))
    game.move(Move.byCode("f1-b5"))
    game.move(Move.byCode("f8-c5"))
    //        game.move(Move.byCode("d2-d3"));
    //        game.move(Move.byCode("d7-d6"));
    //        game.move(Move.byCode("b1-c3"));
    //        game.move(Move.byCode("c8-g4"));
    val prunerL2 = SimplePruner(2, 3, 2)
    val prunerL3 = SimplePruner(2, 4, 3)
    val staticEvaluation = StaticEvaluation//new ConstantEvaluation();//
    loadTest(game, if (useFasterPruner) prunerL2 else prunerL3, staticEvaluation, "Benchmark" + if (useFasterPruner) "L2" else "L3")
}

private fun loadTest() {
    // Loadtest
    println("Loadtest: Start")

    // Grundaufstellung ohne Bauern
    var des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
            + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
            + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
            + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
    loadTest(des)

    // Grundaufstellung mit Bauern vor König und ohne Läufer
//    des = ("white 0 Rook-white-a1-0 Knight-white-b1 "
//            + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
//            + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//            + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false " + "Rook-black-a8-0 Knight-black-b8 "
//            + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Grundaufstellung mit Bauern vor König und ohne Königsläufer
//    des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//            + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
//            + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//            + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//            + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//            + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Grundaufstellung mit Bauern vor König
//    des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//            + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
//            + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//            + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//            + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//            + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Grundaufstellung mit Bauern vor König und ohne Damen
//    des = ("white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
//            + "King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
//            + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
//            + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
//            + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
//            + "King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
    // loadTest( des );

    // Zeit von
    // Grundaufstellung mit Bauern vor König und ohne Königsläufer
    // ist in etwa so groß wie
    // Grundaufstellung mit Bauern vor König und ohne Dame!!! Warum?
}

private fun loadTest(des: String) {
    val game = ChessGame(des)
    val pruner = SimplePruner(2, 3, 2)
    val staticEvaluation = StaticEvaluation
    loadTest(game, pruner, staticEvaluation, "Loadtest")
}

private fun loadTest(game: ChessGame, pruner: SearchTreePruner, staticEvaluation: StaticEvaluationInterface, type: String) {
    val decimalFormat = DecimalFormat("#.0")

    try {
        println("$type: Berechnung gestartet")
        val time = System.currentTimeMillis()
        testTermination(game, pruner, staticEvaluation)
        println("Dauer: " + decimalFormat.format((System.currentTimeMillis() - time) / 1000.0) + "s")
        RuntimeFacade.printMemoryUsage("Speicherverbrauch used/total")
    } catch (e: RuntimeException) {
        println(type + "fehler:" + e.message)
    } finally {
        println("$type: Ende")
    }
}

private fun testTermination(game: ChessGame, pruner: SearchTreePruner = SimplePruner(1, 2, 2), staticEvaluation: StaticEvaluationInterface = StaticEvaluation) {
    val numberFormat = NumberFormat.getPercentInstance()
    val dynamicEvaluation = DynamicEvaluation(pruner, staticEvaluation)

    val possibleMoves = LinkedList<Move>()
    game.getPossibleMoves(possibleMoves)
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
                        + game.history + " -leading to position:"
                        + gameString)
    } catch (e: AssertionError) {
        val extendedE = AssertionError(e.message + " History:" + game.history)
        extendedE.stackTrace = e.stackTrace
        throw extendedE
    }

}

private fun testTermination(game: ChessGame, pruner: SearchTreePruner, move: Move) {
    val initDescription = game.toString()
    val dynamicEvaluation = DynamicEvaluation(pruner, StaticEvaluation)

    val possibleMoves = LinkedList<Move>()
    game.getPossibleMoves(possibleMoves)

    if (!possibleMoves.contains(move)) {
        throw RuntimeException("suggested move:" + move.toString()
                + "not possible in initial position: " + initDescription)
    }

    try {
        dynamicEvaluation.evaluateMove(game, move)
        // Invariante: evaluateMove darf game nicht ändern
        val msg = "after Move:" + move.toString() + " History:" + game.history
        assertEquals(game.toString(), initDescription, msg)
    } catch (e: Exception) {
        val gameToString = game.toString()
        throw RuntimeException(e.toString() + "-after Moves:" + game.history + " -leading to position:"
                + gameToString)
    } catch (e: AssertionError) {
        val extendedE = AssertionError(e.message + " History:" + game.history)
        extendedE.stackTrace = e.stackTrace
        throw extendedE
    }
}
