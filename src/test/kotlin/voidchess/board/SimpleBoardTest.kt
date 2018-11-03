package voidchess.board

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.move.Position
import kotlin.test.*


class SimpleBoardTest {
    private val initial = "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 " +
            "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 " +
            "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false " +
            "Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
            "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false " +
            "Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false " +
            "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 " +
            "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0"

    @Test
    fun testInit() {
        val board = SimpleArrayBoard()
        var figure = board.getFigure(Position.byCode("a1"))
        assertTrue(figure.isRook())
        assertTrue(figure.isWhite)

        figure = board.getFigure(Position.byCode("e8"))
        assertTrue(figure.isKing())
        assertFalse(figure.isWhite)

        val nullFigure = board.getFigureOrNull(Position.byCode("e3"))
        assertNull(nullFigure)
    }

    @Test
    fun testInit960() {
        val board = SimpleArrayBoard()
        board.init(518)
        assertEquals(initial, board.toString())

        board.init(0)
        board.init(314)
        board.init(959)
        try {
            board.init(960)
            fail("AssertionError should have been thrown:Argument bigger than 959")
        } catch (e: AssertionError) {
        }

        try {
            board.init(-1)
            fail("AssertionError should have been thrown:Argument smaller than 0")
        } catch (e: AssertionError) {
        }

    }

    @Test
    fun testInitByDescriptionWithToManySpaces() {
        val board = SimpleArrayBoard()
        val des = "  white   0   Queen-white-c1     King-white-e1-0    King-black-e8-0     "
        board.init(des)
        assertTrue(board.getFigure(Position.byCode("c1")).isQueen())
    }

    @Test
    fun testIsFreeArea() {
        val board = SimpleArrayBoard()
        assertTrue(board.isFreeArea(Position.byCode("a3")))
        assertFalse(board.isFreeArea(Position.byCode("a2")))
    }

    @Test
    fun testSetFigure() {
        val board = SimpleArrayBoard()
        val figure = board.getFigure(Position.byCode("b1"))
        val to = Position.byCode("e4")
        board.setFigure(to, figure)
        assertTrue(board.getFigure(to).isKnight())
    }

    @Test
    fun testGetCachedAttackLines() {
        val board = SimpleArrayBoard()
        var attackLines = board.getCachedAttackLines(true)
        assertFalse(attackLines.isCheck)

        var des = "white 0 King-white-e1-0 Rook-white-h2-1 Queen-black-b4 King-black-e8-0"
        board.init(des)
        attackLines = board.getCachedAttackLines(true)
        assertTrue(attackLines.isCheck)
        assertFalse(attackLines.isDoubleCheck)
        assertEquals(3, attackLines.checkLines[0].posProgression.size)

        des = "white 0 King-white-e2-3 Knight-black-g1 Knight-white-f3 Rook-black-h2-12 King-black-g7-3"
        board.init(des)
        attackLines = board.getCachedAttackLines(true)
        assertTrue(attackLines.isCheck)
        assertTrue(attackLines.isDoubleCheck)
    }

    @Test(dataProvider = "getTestMoveUndoMoveInvarianceData")
    fun testMoveUndoMoveInvariance(fromCode: String, toCode: String, gameDes: String) {
        val board = SimpleArrayBoard()
        board.init(gameDes)
        val initialLongGameDescription = board.toString()
        val initialFigureCount = board.figureCount
        val from = Position.byCode(fromCode)
        val to = Position.byCode(toCode)

        val figure = board.getFigureOrNull(from)
        assertNotNull(figure, "figure to move")
        val canDoCastlingInitially = figure.canCastle()
        val willTakeFigure = !board.isFreeArea(to)

        val figureTaken = board.move(figure, to)

        val wasFigureTaken = figureTaken != null
        assertEquals(willTakeFigure, wasFigureTaken, "figure taken")
        assertEquals(if (wasFigureTaken) initialFigureCount - 1 else initialFigureCount, board.figureCount)
        assertEquals(figure, board.getFigureOrNull(to), "figure after move")
        assertEquals(to, figure.position, "figure position after move")
        assertFalse(figure.canCastle(), "after a move, yuo can't do castling guaranteed")

        board.undoMove(figure, from, figureTaken)

        assertEquals(initialLongGameDescription, board.toString(), "game state after move-undo")
        assertEquals(from, figure.position)
        assertEquals(canDoCastlingInitially, figure.canCastle(), "same castling state as before")
    }

    @DataProvider
    fun getTestMoveUndoMoveInvarianceData(): Array<Array<Any>> = arrayOf(
            arrayOf<Any>("e1", "d2", "white 0 King-white-e1-0 Queen-black-d2 King-black-e8-0"),
            arrayOf<Any>("e1", "f1", "white 0 King-white-e1-0 Queen-black-d2 King-black-e8-0")
    )

    @Test
    fun testToString() {
        val board = SimpleArrayBoard()
        assertEquals(board.toString(), initial)
    }
}
