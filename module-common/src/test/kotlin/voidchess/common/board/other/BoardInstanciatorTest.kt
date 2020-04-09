package voidchess.common.board.other

import org.testng.annotations.Test
import voidchess.common.board.StartConfig
import voidchess.common.board.move.Position
import voidchess.common.figures.*
import kotlin.test.assertEquals


class ChessBoardTest {
    private val sortedClassicSetupFigureStates = ("Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 " +
            "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 " +
            "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false " +
            "Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
            "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false " +
            "Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false " +
            "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 " +
            "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
        .split(" ").sorted()

    @Test
    fun testInstanciateByManual() {
        val instanciator = StartConfig.ManualConfig(false, 12, sortedClassicSetupFigureStates).boardInstanciator()
        val posAndNullableFigures: List<Pair<Position, Figure?>> = instanciator.generateInitialSetup().toList()
        assertClassicalSetup(posAndNullableFigures)
    }

    @Test
    fun testInstanciateByClassic() {
        val instanciator = StartConfig.ClassicConfig.boardInstanciator()
        val posAndNullableFigures: List<Pair<Position, Figure?>> = instanciator.generateInitialSetup().toList()
        assertClassicalSetup(posAndNullableFigures)
    }

    @Test
    fun testInstanciateByChess960() {
        val instanciator = StartConfig.Chess960Config(518).boardInstanciator()
        val posAndNullableFigures: List<Pair<Position, Figure?>> = instanciator.generateInitialSetup().toList()
        assertClassicalSetup(posAndNullableFigures)
    }

    private fun assertClassicalSetup(posAndNullableFigures: List<Pair<Position, Figure?>>) {
        assertEquals(64, posAndNullableFigures.size, "number of fields on the board")
        val numberOfUniquePos = posAndNullableFigures.map { it.first }.toSet().size
        assertEquals(64, numberOfUniquePos, "number of unique positions on the board")
        val posAndFigures: List<Pair<Position, Figure>> = posAndNullableFigures.mapNotNull { (pos, nullableFigure)->
            nullableFigure?.let { pos to it }
        }
        posAndFigures.forEach { (pos, figure)->
            assertEquals(pos, figure.position, "position of generated figure $figure")
        }
        val figures = posAndFigures.map { it.second }
        assertEquals(32, figures.size, "number of figures on the board")
        assertEquals(16, figures.filter { it.isWhite }.size, "number of white figures on the board")
        assertEquals(16, figures.filterIsInstance<Pawn>().size, "number of pawns on the board")
        assertEquals(4, figures.filterIsInstance<Rook>().size, "number of rook on the board")
        assertEquals(4, figures.filterIsInstance<Knight>().size, "number of knights on the board")
        assertEquals(4, figures.filterIsInstance<Bishop>().size, "number of bishops on the board")
        assertEquals(2, figures.filterIsInstance<Queen>().size, "number of queens on the board")
        assertEquals(2, figures.filterIsInstance<King>().size, "number of kings on the board")

        val sortedGeneratedFigureStates = figures.map { it.toString() }.sorted()
        assertEquals(sortedClassicSetupFigureStates, sortedGeneratedFigureStates, "sorted figure states")
    }
}