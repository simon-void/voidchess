package voidchess.common.board

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.assertFiguresKnowTherePosition
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.common.figures.King
import voidchess.common.figures.Queen
import voidchess.common.figures.Rook
import voidchess.common.helper.toChess960Config
import voidchess.common.initChessBoard
import voidchess.common.toManualConfig
import kotlin.test.*


class ArrayChessBoardTest {
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
        val board = ArrayChessBoard()
        var figure = board.getFigure(Position.byCode("a1"))
        assertTrue(figure is Rook)
        assertTrue(figure.isWhite)

        figure = board.getFigure(Position.byCode("e8"))
        assertTrue(figure is King)
        assertFalse(figure.isWhite)

        val nullFigure = board.getFigureOrNull(Position.byCode("e3"))
        assertNull(nullFigure)
    }

    @Test
    fun testInit960() {
        val board = ArrayChessBoard()
        board.init(518.toChess960Config())
        assertEquals(initial, board.toString())
    }

    @Test
    fun testInitByDescriptionWithToManySpaces() {
        val board = ArrayChessBoard()
        val des = "  white   0   Queen-white-c1     King-white-e1-0    King-black-e8-0     "
        board.init(des.toManualConfig())
        assertTrue(board.getFigure(Position.byCode("c1")) is Queen)
    }

    @Test
    fun testIsFreeArea() {
        val board = ArrayChessBoard()
        assertTrue(board.isFreeArea(Position.byCode("a3")))
        assertFalse(board.isFreeArea(Position.byCode("a2")))
    }

    @Test
    fun testGetCachedAttackLines() {
        val board = ArrayChessBoard()
        var attackLines = board.getCachedAttackLines()
        assertFalse(attackLines.isCheck)

        var des = "white 0 King-white-e1-0 Rook-white-h2-1 Queen-black-b4 King-black-e8-0"
        board.init(des.toManualConfig())
        attackLines = board.getCachedAttackLines()
        assertTrue(attackLines.isCheck)
        assertFalse(attackLines.isDoubleCheck)
        assertEquals(3, attackLines.checkLines[0].posProgression.size)

        des = "white 0 King-white-e2-3 Knight-black-g1 Knight-white-f3 Rook-black-h2-12 King-black-g7-3"
        board.init(des.toManualConfig())
        attackLines = board.getCachedAttackLines()
        assertTrue(attackLines.isCheck)
        assertTrue(attackLines.isDoubleCheck)
    }

    @Test(dataProvider = "getTestMoveUndoMoveInvarianceData")
    fun testSimulateSimplifiedMove(fromCode: String, toCode: String, gameDes: String) {
        val board = ArrayChessBoard(gameDes.toManualConfig())
        board.assertFiguresKnowTherePosition()
        val initialContent = board.toString()

        val figure = board.getFigure(Position.byCode(fromCode))
        board.simulateSimplifiedMove(figure, Position.byCode(toCode)) {boardAfterMove->
            boardAfterMove.assertFiguresKnowTherePosition()
            assertNotEquals(initialContent, boardAfterMove.toString(), "game state within simulated")
            true
        }
        board.assertFiguresKnowTherePosition()
        assertEquals(initialContent, board.toString(), "game state after simulated")
    }

    @DataProvider
    fun getTestMoveUndoMoveInvarianceData(): Array<Array<Any>> = arrayOf(
            arrayOf<Any>("e1", "d2", "white 0 King-white-e1-0 Queen-black-d2 King-black-e8-0"),
            arrayOf<Any>("e1", "f1", "white 0 King-white-e1-0 Queen-black-d2 King-black-e8-0")
    )

    @Test
    fun testToString() {
        val board = ArrayChessBoard()
        assertEquals(board.toString(), initial)
    }

    @Test
    fun testMovesPlayed() {
        val twoMoves = mutableListOf(Move.byCode("e2-e4"), Move.byCode("e7-e5"))
        val thirdMove = Move.byCode("d2-d4")
        val threeMoves = mutableListOf(Move.byCode("e2-e4"), Move.byCode("e7-e5"), thirdMove)
        val board = ArrayChessBoard()
        twoMoves.forEach {
            board.move(it)
        }
        assertEquals(twoMoves, board.movesPlayed())
        board.move(thirdMove)
        assertEquals(threeMoves, board.movesPlayed())
        board.undo()
        assertEquals(twoMoves, board.movesPlayed())
    }

    @Test(expectedExceptions = [IllegalArgumentException::class])
    fun testHandleMissingPromotionTypeWhenPawnPromotion() {
        val des = "black 0 King-white-e1-0 Pawn-black-g2-false King-black-e8-0"
        initChessBoard(des, "g2-g1")
    }

    @Test(dataProvider = "getHandlePawnPromotionData")
    fun testHandlePawnPromotion(promotionType: Char, figureDes: String) {
        val des = "black 0 King-white-e1-0 Pawn-black-g2-false King-black-e8-0"
        val game = initChessBoard(des, "g2${promotionType}g1")
        assertEquals("King-white-e1-0 $figureDes King-black-e8-0", game.toString())
    }

    @DataProvider
    fun getHandlePawnPromotionData(): Array<Array<Any>> = arrayOf(
        arrayOf('Q', "Queen-black-g1"),
        arrayOf('K', "Knight-black-g1"),
        arrayOf('B', "Bishop-black-g1"),
        arrayOf('R', "Rook-black-g1-0")
    )
}
