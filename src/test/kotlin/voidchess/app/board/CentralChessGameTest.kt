package voidchess.app.board

import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.app.inner.board.CentralChessGameImpl
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResultType
import voidchess.common.board.move.Position
import voidchess.common.board.other.Chess960Index
import voidchess.common.figures.Bishop
import voidchess.common.figures.King
import voidchess.initCentralChessGame
import voidchess.toManualConfig
import kotlin.test.*


class CentralChessGameTest {
    private lateinit var game: CentralChessGameImpl

    @BeforeMethod
    fun setUp() {
        game = CentralChessGameImpl()
    }

    @Test
    fun testIsFreeArea() {
        assertTrue(game.isFreeArea(Position.byCode("a3")))
        assertFalse(game.isFreeArea(Position.byCode("c1")))
        assertFalse(game.isFreeArea(Position.byCode("d7")))
    }

    @Test
    fun testGetFigureOrNull() {
        assertNull(game.getFigureOrNull(Position.byCode("e3")))
        val king = game.getFigureOrNull(Position.byCode("e1"))
        assertTrue(king is King)
        assertTrue(king.isWhite)
        val bishop = game.getFigureOrNull(Position.byCode("c8"))
        assertTrue(bishop is Bishop)
        assertFalse(bishop.isWhite)
    }

    @Test
    fun testToString() {
        val code = ("white 0 "
                + "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 "
                + "Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-white-g2-false Pawn-white-h2-false "
                + "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Pawn-black-g7-false Pawn-black-h7-false "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 "
                + "Bishop-black-f8 Knight-black-g8 Rook-black-h8-0")
        assertEquals(game.toString(), code)
    }

    @Test
    fun testEquals() {
        val game1 = CentralChessGameImpl(StartConfig.ClassicConfig)
        val game2 = CentralChessGameImpl(StartConfig.Chess960Config(Chess960Index.classic))
        assertTrue(game1.equalsOther(game2))
    }

    @Test
    fun testMove() {
        val des = "white 0 King-white-e1-0 Pawn-white-c2-false King-black-e8-0"
        val game = initCentralChessGame(des)
        game.move(Move.byCode("c2-c4"))
        val newDes = "black 0 King-white-e1-0 Pawn-white-c4-true King-black-e8-0"
        assertEquals(game.toString(), newDes)
    }

    @Test
    fun testHandleEnpassant() {
        val des = "black 0 Pawn-white-c4-true Pawn-black-b4-false King-white-e1-0 King-black-e8-0"
        val game = initCentralChessGame(des)
        val move = Move[Position.byCode("b4"), Position.byCode("c3")]
        game.move(move)
        val newDes = "white 0 King-white-e1-0 Pawn-black-c3-false King-black-e8-0"
        assertEquals(game.toString(), newDes)
    }

    @Test
    fun testHandleCastling() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0 "
        var game = initCentralChessGame(des)
        var move = Move.byCode("e8-a8")
        game.move(move)
        var newDes = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1"
        assertEquals(game.toString(), newDes)

        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0 "
        game = initCentralChessGame(des)
        move = Move[Position.byCode("f8"), Position.byCode("a8")]
        game.move(move)
        newDes = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1"
        assertEquals(game.toString(), newDes)

        des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0 "
        game = initCentralChessGame(des)
        move = Move[Position.byCode("e1"), Position.byCode("h1")]
        game.move(move)
        newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
        assertEquals(game.toString(), newDes)

        des = "white 0 King-white-g1-0 Rook-white-h1-0 King-black-e8-0 "
        game = initCentralChessGame(des)
        move = Move[Position.byCode("g1"), Position.byCode("h1")]
        game.move(move)
        newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
        assertEquals(game.toString(), newDes)

        des = "white 0 King-white-f1-0 Rook-white-g1-0 King-black-e8-0 "
        game = initCentralChessGame(des)
        move = Move[Position.byCode("f1"), Position.byCode("g1")]
        game.move(move)
        newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
        assertEquals(game.toString(), newDes)
    }

    @Test
    fun testIsMatt() {
        val des = "black 0 King-white-e1-0 Queen-black-h2 Pawn-black-f3-false King-black-e8-0"
        val game = initCentralChessGame(des)
        val endOption = game.move(Move.byCode("h2-e2"))
        assertEquals(MoveResultType.CHECKMATE, endOption)
    }

    @Test
    fun testIsDrawBecauseOfNoMoves() {
        val des = "black 0 King-white-e1-0 Queen-black-h2 Pawn-black-c2-false Pawn-white-e7-false King-black-e8-0"
        val game = initCentralChessGame(des)
        val endOption = game.move(Move.byCode("h2-g2"))
        assertEquals(MoveResultType.STALEMATE, endOption)
    }

    @Test
    fun testIsDrawBecauseOfLowMaterial() {
        val des = "white 0 King-white-e1-0 Bishop-black-g2 Knight-white-c2 Knight-white-e7 King-black-e8-0"
        val game = initCentralChessGame(des)
        val endOption = game.move(Move.byCode("e1-f2"))
        assertEquals(MoveResultType.DRAW, endOption)
    }

    @Test(dataProvider = "get3TimesSamePositionData")
    fun testIsDrawBecauseOfThreeTimesSamePosition(startConfig: StartConfig, listOfMoves: List<String>) {
        val game = CentralChessGameImpl(startConfig)

        listOfMoves.map { Move.byCode(it) }.forEachIndexed { index, move ->
            val expectedMoveResultType =
                if (index < listOfMoves.lastIndex) MoveResultType.NO_END
                else MoveResultType.THREE_TIMES_SAME_POSITION

            assertEquals(game.move(move), expectedMoveResultType, "unexpected MoveResultType at index $index")
        }
    }

    @DataProvider
    fun get3TimesSamePositionData(): Array<Array<Any>> {
        val whiteMove = "c2-a1"
        val whiteReturn = "a1-c2"
        val blackMove = "g2-h3"
        val blackReturn = "h3-g2"
        return arrayOf(
            arrayOf(
                "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 Knight-white-c2 Knight-white-e7 King-black-e8-0".toManualConfig(),
                listOf(
                    whiteMove, blackMove, whiteReturn, blackReturn, whiteMove, blackMove, whiteReturn, blackReturn,
                ),
            ),
//            arrayOf(
//                "white 0 King-white-a2-4 King-black-a7-4 Queen-white-a1 Queen-black-a8 Knight-white-b8".toManualConfig(),
//                listOf(
//                    "a1-b1", "a8-b8", "b1-c1", "b8-c8",
//                    "c1-b1", "c8-b8", "b1-a1", "b8-a8",
//                    "a1-b1", "a8-b8", "b1-c1", "b8-c8",
//                    "c1-a1", "c8-a8", "a1-b1", "a8-b8",
//                ),
//            ),
        )
    }

    @Test
    fun testIsDrawBecauseOf50MovesWithoutPawnOrCatchingMove() {
        var des = "white 98 King-white-e1-0 Bishop-white-b2-false Pawn-black-b4-false King-black-e8-0"
        var game = initCentralChessGame(des)
        var endOption: MoveResultType
        endOption = game.move(Move.byCode("b2-a3"))
        assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, endOption)
        endOption = game.move(Move.byCode("b4-a3"))
        assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, endOption)

        des = "white 98 King-white-e1-0 Bishop-white-b2-false Pawn-black-b4-false King-black-e8-0"
        game = initCentralChessGame(des)
        endOption = game.move(Move.byCode("b2-a3"))
        assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, endOption)
        endOption = game.move(Move.byCode("b4-b3"))
        assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, endOption)

        des = "white 98 King-white-e1-0 Bishop-white-b2-false Pawn-black-b4-false King-black-e8-0"
        game = initCentralChessGame(des)
        endOption = game.move(Move.byCode("b2-a3"))
        assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, endOption)
        endOption = game.move(Move.byCode("e8-d8"))
        assertEquals(MoveResultType.FIFTY_MOVES_NO_HIT, endOption)
    }

    @Test(expectedExceptions = [IllegalArgumentException::class])
    fun testHandleMissingPromotionTypeWhenPawnPromotion() {
        val des = "black 0 King-white-e1-0 Pawn-black-g2-false King-black-e8-0"
        initCentralChessGame(des, "g2-g1")
    }

    @Test(dataProvider = "getHandlePawnPromotionData")
    fun testHandlePawnPromotion(promotionType: Char, figureDes: String) {
        val des = "black 0 King-white-e1-0 Pawn-black-g2-false King-black-e8-0"
        val game = initCentralChessGame(des, "g2${promotionType}g1")
        assertEquals("white 0 King-white-e1-0 $figureDes King-black-e8-0", game.toString())
    }

    @DataProvider
    fun getHandlePawnPromotionData(): Array<Array<Any>> = arrayOf(
        arrayOf('Q', "Queen-black-g1"),
        arrayOf('K', "Knight-black-g1"),
        arrayOf('B', "Bishop-black-g1"),
        arrayOf('R', "Rook-black-g1-0")
    )

    @Test
    fun testIsSelectable() {
        var des = "white 0 King-white-e1-0 Queen-black-g2 Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0"
        var game = initCentralChessGame(des)
        assertFalse(game.isSelectable(Position.byCode("g2")))
        assertFalse(game.isSelectable(Position.byCode("e1")))
        assertTrue(game.isSelectable(Position.byCode("e6")))

        des = "black 0 King-white-e1-0 Queen-white-e2 Bishop-black-c8 King-black-e8-0"
        game = initCentralChessGame(des)
        assertTrue(game.isSelectable(Position.byCode("c8")))
        assertTrue(game.isSelectable(Position.byCode("e8")))
        assertFalse(game.isSelectable(Position.byCode("h8")))

        des = "black 0 King-white-e1-0 Queen-white-e2 Rook-black-a6-1 King-black-e8-0"
        game = initCentralChessGame(des)
        assertTrue(game.isSelectable(Position.byCode("a6")))
    }

    @Test
    fun testIsMovable() {
        var des = "white 0 King-white-e1-0 Queen-black-g2 Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0"
        var game = initCentralChessGame(des)


        assertTrue(game.isMovable(Position.byCode("e6"), Position.byCode("e7")))

        des = "black 0 King-white-e1-0 Pawn-black-a5-false King-black-g6-2 Rook-white-h6-1"
        game = initCentralChessGame(des)
        assertFalse(game.isMovable(Position.byCode("a5"), Position.byCode("a4")))

        des = "black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0"
        game = initCentralChessGame(des)
        assertFalse(game.isMovable(Position.byCode("e8"), Position.byCode("g8")))

        game = initCentralChessGame(621)
        game.move(Move.byCode("f2-f3"))
        game.move(Move.byCode("a7-a6"))
        val from = Position.byCode("f1")
        val to = Position.byCode("f2")
        assertTrue(game.isMovable(from, to))
        assertFalse(game.isFreeArea(from))
    }

    @Test
    fun testColorChangedBetweenMoves() {
        val des = "white 0 King-white-e1-0 Pawn-black-g3-false King-black-e8-0"
        val game = initCentralChessGame(des)
        game.move(Move.byCode("e1-d1"))
        try {
            game.move(Move.byCode("d1-c1"))
            fail()
        } catch (e: AssertionError) {
        }

    }

    @Test
    fun testChecksForMoveMovesFigureNotNull() {
        val des = "white 0 King-white-e1-0 Pawn-black-g3-false King-black-e8-0"
        val game = initCentralChessGame(des)
        try {
            game.move(Move.byCode("a1-b1"))
            fail()
        } catch (e: IllegalStateException) {
        }

    }

    @Test
    fun testHasHitBiggerFigure() {
        val des = ("white 0 King-white-h2-3 Queen-black-h3 "
                + "Pawn-white-g2-false Bishop-white-e7 King-black-e8-0 "
                + "Knight-black-g5 Pawn-white-a6-false")
        val game = initCentralChessGame(des)
        assertTrue(game.hasHitFigure)        //because numberWithoutHit=0 and figureCount!=32
        game.move(Move.byCode("g2-h3"))
        assertTrue(game.hasHitFigure)
        game.move(Move.byCode("g5-h3"))
        assertTrue(game.hasHitFigure)
        game.move(Move.byCode("h2-h3"))
        assertTrue(game.hasHitFigure)
        game.move(Move.byCode("e8-e7"))
        assertTrue(game.hasHitFigure)
        game.move(Move.byCode("a6-a7"))
        assertFalse(game.hasHitFigure)
        game.move(Move.byCode("e7-d7"))
        assertFalse(game.hasHitFigure)
    }

    @Test
    fun testIsCheck() {
        val des = "white 0 King-white-g1-2 Bishop-black-f2 King-black-e8-0"
        val game = initCentralChessGame(des)
        assertTrue(game.isCheck)
    }

    @Test(dataProvider = "getCompleteHistoryData")
    fun testGetCompleteHistory(initialPos: Int, expectedCompleteHistory: String) {
        val game = initCentralChessGame(initialPos)
        val moves = expectedCompleteHistory.split(',').map { Move.byCode(it) }
        for(move in moves) {
            assertTrue(game.isMovable(move.from, move.to), "move $move isn't allowed in sequence $expectedCompleteHistory")
            game.move(move)
        }
        assertEquals(expectedCompleteHistory, game.getCompleteHistory())
    }

    @DataProvider
    fun getCompleteHistoryData(): Array<Array<Any>> = arrayOf(
        arrayOf(621, "g2-g3,f7-f6,c2-c3,g8-f7,d1-c2,a7-a6,c2-h7"),
        arrayOf(518, "g2-g3,g7-g6,g1-f3,g8-f6,f1-g2,f8-g7,e1-h1,e8-h8"),
        arrayOf(652, "c2-c3,c7-c6,c1-b3,c8-b6,b1-c2,b8-c7,d1-a1,d8-a8")
    )
}
