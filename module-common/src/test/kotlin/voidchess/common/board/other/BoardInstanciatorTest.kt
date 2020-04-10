package voidchess.common.board.other

import org.testng.annotations.Test
import voidchess.common.board.StartConfig
import voidchess.common.board.StartConfigTest
import voidchess.common.board.move.Position
import voidchess.common.figures.*
import kotlin.test.assertEquals


class BoardInstanciatorTest {
    private val sortedClassicSetupFigureStates = StartConfigTest.classicFigureStates.sorted()

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