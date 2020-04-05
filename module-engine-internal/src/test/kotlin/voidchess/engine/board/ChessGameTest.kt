package voidchess.engine.board

import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.ChessGameSupervisorMock
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResult
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.engine.board.move.ExtendedMove
import voidchess.engine.figures.Bishop
import voidchess.engine.figures.King
import voidchess.moves
import kotlin.test.*


internal class ChessGameTest {
    private var game: ChessGame = ChessGame(ChessGameSupervisorDummy)

    @BeforeMethod
    fun setUp() {
        game = ChessGame(ChessGameSupervisorDummy)
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
        val copy = ChessGame(game.toString())
        assertTrue(game.equalsOther(copy))
    }

    @Test
    fun testCopy() {
        val copies = game.copyGame(4)

        for (copy in copies) {
            assertEquals(copy.toString(), game.toString(), "copy and game")
        }
    }

    @Test(dependsOnMethods = ["testIsDrawBecauseOfThreeTimesSamePosition", "testCopy"])
    fun testDeepCopy() {
        val whiteMove = Move.byCode("g1-f3")
        val whiteReturn = Move.byCode("f3-g1")
        val blackMove = Move.byCode("b8-c6")
        val blackReturn = Move.byCode("c6-b8")

        game.move(whiteMove)
        game.move(blackMove)
        game.move(whiteReturn)
        game.move(blackReturn)
        game.move(whiteMove)
        game.move(blackMove)
        game.move(whiteReturn)
        game.move(blackReturn)
        game.move(whiteMove)
        game.move(blackMove)
        game.move(whiteReturn)

        val copy = game.copyGame(3)[1]
        val gameState = copy.move(blackReturn)

        assertEquals(gameState, MoveResult.THREE_TIMES_SAME_POSITION, "game state")
    }

    @Test
    fun testMove() {
        val des = "white 0 King-white-e1-0 Pawn-white-c2-false King-black-e8-0"
        val game = ChessGame(des)
        val move = Move[Position.byCode("c2"), Position.byCode("c4")]
        game.move(move)
        val newDes = "black 1 King-white-e1-0 Pawn-white-c4-true King-black-e8-0"
        assertEquals(newDes, game.toString())
    }

    @Test
    fun testUndo() {
        var des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0"
        var game = ChessGame(des)
        var move = Move.byCode("e1-h1")
        game.move(move)
        var newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"

        assertEquals(newDes, game.toString())
        game.undo()
        assertEquals(des, game.toString())
        assertTrue(game.isMovable(move.from, move.to, true))


        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0"
        game = ChessGame(des)
        move = Move.byCode("f8-g8")
        game.move(move)
        game.undo()

        assertEquals(des, game.toString())
        assertTrue(game.isMovable(move.from, move.to, false))


        des = "white 0 King-white-e1-0 Pawn-white-b5-false Pawn-black-c5-true King-black-e8-0"
        game = ChessGame(des)
        move = Move.byCode("b5-c6")

        assertTrue(game.isMovable(move.from, move.to, true))
        game.move(move)
        game.undo()
        assertTrue(game.isMovable(move.from, move.to, true))


        des = "white 4 Rook-white-a1-0 King-white-e1-0 Bishop-white-f1 " +
                "Rook-white-h1-0 Pawn-white-b2-false Queen-white-d2 " +
                "Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                "Pawn-white-a3-false Knight-white-c3 Knight-white-f3 " +
                "Pawn-white-c4-false Pawn-black-h4-false Pawn-white-e5-false " +
                "Bishop-black-a6 Pawn-black-b6-false Knight-black-c6 " +
                "Pawn-black-e6-false Pawn-black-h6-false Pawn-black-a7-false " +
                "Pawn-black-c7-false Pawn-black-d7-false Pawn-black-f7-false " +
                "Rook-black-a8-0 Queen-black-d8 King-black-e8-0 " +
                "Rook-black-h8-0"
        game = ChessGame(des)
        move = Move.byCode("e1-a1")

        assertTrue(game.isMovable(move.from, move.to, true))
        game.move(move)
        game.undo()
        assertTrue(game.isMovable(move.from, move.to, true))


        des = "black 1 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false"
        game = ChessGame(des)
        move = Move.byCode("a6-b6")
        game.move(move)
        game.undo()

        assertEquals(game.toString(), des)


        des = "white 1 King-white-h1-4 Pawn-white-a2-false King-black-a6-6"
        game = ChessGame(des)
        move = Move.byCode("a2-a4")
        game.move(move)
        game.undo()

        assertEquals(game.toString(), des)


        des = "white 0 King-white-h1-4 Pawn-white-a2-false Pawn-black-b4-false King-black-a6-6"
        game = ChessGame(des)
        move = Move.byCode("a2-a4")
        game.move(move)
        newDes = "black 1 King-white-h1-4 Pawn-white-a4-true Pawn-black-b4-false King-black-a6-6"

        move = Move.byCode("b4-a3")
        game.move(move)
        move = Move.byCode("h1-h2")
        game.move(move)
        move = Move.byCode("a3-a2")
        game.move(move)
        game.undo()
        game.undo()
        game.undo()

        assertEquals(newDes, game.toString())

        move = Move.byCode("b4-b3")
        game.move(move)
        move = Move.byCode("h1-h2")
        game.move(move)
        move = Move.byCode("b3-b2")
        game.move(move)
        game.undo()
        game.undo()
        game.undo()

        assertEquals(newDes, game.toString())


        des = "white 0 King-white-h1-3 King-black-b7-3 Pawn-white-c7-false"
        game = ChessGame(des)
        move = Move.byCode("c7-c8")
        game.move(move)
        game.undo()

        assertEquals(des, game.toString())


        game = ChessGame(621)
        move = Move.byCode("f2-f3")
        game.move(move)
        move = Move.byCode("b7-b6")
        game.move(move)
        move = Move.byCode("g1-b6")
        game.move(move)
        move = Move.byCode("a7-a6")
        game.move(move)
        des = game.toString()
        move = Move.byCode("e1-f1")
        game.move(move)
        game.undo()
        assertEquals(des, game.toString())
        move = Move.byCode("f1-f2")
        game.move(move)

        game = ChessGame(314)
        des = game.toString()
        game.move(Move.byCode("d1-c1"))
        game.move(Move.byCode("d8-c8"))
        game.undo()
        game.undo()
        assertEquals(des, game.toString())

        game = ChessGame(707)
        game.move(Move.byCode("e1-f3"))
        game.move(Move.byCode("b7-b6"))
        game.move(Move.byCode("f1-e3"))
        game.move(Move.byCode("g7-g6"))
        game.move(Move.byCode("d1-e1"))
        game.move(Move.byCode("a8-f3"))
        des = game.toString()
        game.move(Move.byCode("c1-b1"))
        game.undo()
        assertEquals(des, game.toString())
    }

    @Test
    fun testWithMove() {
        val game = ChessGame("white 0 King-white-e6-6 Rook-white-h1-0 King-black-e8-0")
        val desc = game.toString()

        game.withMove(Move.byCode("h1-h8")) { gameAfterMove ->
            assertNotEquals(desc, gameAfterMove.toString())
        }
        assertEquals(desc, game.toString())

    }

    @Test
    fun testHandleEnpassant() {
        val des = "white 0 Pawn-white-c2-false Pawn-black-b4-false " + "King-white-e1-0 King-black-e8-0"
        val game = ChessGame(des)
        assertFalse(game.getFigure(Position.byCode("c2")).canBeHitEnpassant, "pawn can't be hit enpassant on start row")
        game.move(Move.byCode("c2-c4"))
        assertTrue(game.getFigure(Position.byCode("c4")).canBeHitEnpassant, "pawn can be hit enpassant after enpassant")
        game.move(Move.byCode("e8-f8"))
        assertFalse(game.getFigure(Position.byCode("c4")).canBeHitEnpassant, "pawn can't be hit enpassant after black moved king")
        game.undo()
        assertTrue(game.getFigure(Position.byCode("c4")).canBeHitEnpassant, "pawn can be hit enpassant after king move was undone")
        assertTrue(game.isMovable(Position.byCode("b4"), Position.byCode("c3"), false))
        game.move(Move.byCode("b4-c3"))
        val newDes = "white 0 King-white-e1-0 Pawn-black-c3-false " + "King-black-e8-0"
        assertEquals(newDes, game.toString())
    }

    @Test
    fun testHandleCastling() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0 "
        var game = ChessGame(des)
        var move = Move[Position.byCode("e8"), Position.byCode("a8")]
        game.move(move)
        var newDes = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1"
        assertEquals(newDes, game.toString())

        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0 "
        game = ChessGame(des)
        move = Move[Position.byCode("f8"), Position.byCode("a8")]
        game.move(move)
        newDes = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1"
        assertEquals(newDes, game.toString())

        des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0 "
        game = ChessGame(des)
        move = Move[Position.byCode("e1"), Position.byCode("h1")]
        game.move(move)
        newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
        assertEquals(newDes, game.toString())

        des = "white 0 King-white-g1-0 Rook-white-h1-0 King-black-e8-0 "
        game = ChessGame(des)
        move = Move[Position.byCode("g1"), Position.byCode("h1")]
        game.move(move)
        newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
        assertEquals(newDes, game.toString())

        des = "white 0 King-white-f1-0 Rook-white-g1-0 King-black-e8-0 "
        game = ChessGame(des)
        move = Move[Position.byCode("f1"), Position.byCode("g1")]
        game.move(move)
        newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
        assertEquals(newDes, game.toString())

        game = ChessGame(314)
        game.move(Move.byCode("d1-c1"))
        val expectedNewDesStart = "black 1 Knight-white-a1 Queen-white-b1 King-white-c1-1-true Rook-white-d1-1 Bishop-white-e1 Bishop-white-f1 Rook-white-g1-0 Knight-white-h1"
        assertEquals(expectedNewDesStart, game.toString().substring(0, expectedNewDesStart.length))
    }

    @Test
    fun testIsMatt() {
        val des = "black 0 King-white-e1-0 Queen-black-h2 " + "Pawn-black-f3-false King-black-e8-0"
        val game = ChessGame(des)
        val endOption = game.move(Move.byCode("h2-e2"))
        assertEquals(MoveResult.CHECKMATE, endOption)
    }

    @Test
    fun testIsDrawBecauseOfNoMoves() {
        val des = "black 0 King-white-e1-0 Queen-black-h2 " + "Pawn-black-c2-false Pawn-white-e7-false King-black-e8-0"
        val game = ChessGame(des)
        val endOption = game.move(Move.byCode("h2-g2"))
        assertEquals(MoveResult.STALEMATE, endOption)
    }

    @Test
    fun testIsDrawBecauseOfLowMaterial() {
        val des = "white 0 King-white-e1-0 Bishop-black-g2 " + "Knight-white-c2 Knight-white-e7 King-black-e8-0"
        val game = ChessGame(des)
        val endOption = game.move(Move.byCode("e1-f2"))
        assertEquals(MoveResult.DRAW, endOption)
    }

    @Test
    fun testIsDrawBecauseOfThreeTimesSamePosition() {
        val des = "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 Knight-white-c2 Knight-white-e7 King-black-e8-0"
        val game = ChessGame(des)
        val whiteMove = Move.byCode("c2-a1")
        val whiteReturn = Move.byCode("a1-c2")
        val blackMove = Move.byCode("g2-h3")
        val blackReturn = Move.byCode("h3-g2")

        assertEquals(game.move(whiteMove), MoveResult.NO_END)
        assertEquals(game.move(blackMove), MoveResult.NO_END)
        assertEquals(game.move(whiteReturn), MoveResult.NO_END)
        assertEquals(game.move(blackReturn), MoveResult.NO_END)

        assertEquals(game.move(whiteMove), MoveResult.NO_END)
        assertEquals(game.move(blackMove), MoveResult.NO_END)
        assertEquals(game.move(whiteReturn), MoveResult.NO_END)
        //        assertEquals(game.move(blackReturn), MoveResult.NO_END);
        assertEquals(game.move(blackReturn), MoveResult.THREE_TIMES_SAME_POSITION)

        //        assertEquals(game.move(whiteMove), MoveResult.THREE_TIMES_SAME_POSITION);
    }

    @Test
    fun testIsDrawBecauseOf50HitlessMoves() {
        var des = "white 98 King-white-e1-0 Pawn-white-a2-false " + "Pawn-black-b4-false King-black-e8-0"
        var game = ChessGame(des)
        var endOption: MoveResult
        endOption = game.move(Move.byCode("a2-a4"))
        assertNotEquals(MoveResult.FIFTY_MOVES_NO_HIT, endOption)
        endOption = game.move(Move.byCode("b4-a3"))
        assertNotEquals(MoveResult.FIFTY_MOVES_NO_HIT, endOption)

        des = "white 98 King-white-e1-0 Pawn-white-a2-false " + "Pawn-black-b4-false King-black-e8-0"
        game = ChessGame(des)
        endOption = game.move(Move.byCode("a2-a4"))
        assertNotEquals(MoveResult.FIFTY_MOVES_NO_HIT, endOption)
        endOption = game.move(Move.byCode("b4-b3"))
        assertEquals(MoveResult.FIFTY_MOVES_NO_HIT, endOption)
    }

    @Test
    fun testHandleTransformPawn() {
        val mock = ChessGameSupervisorMock(PawnPromotion.KNIGHT)
        val des = "black 0 King-white-e1-0 Pawn-black-g2-false " + "King-black-e8-0"
        val game = ChessGame(des)
        game.useSupervisor(mock)
        game.move(Move.byCode("g2-g1"))
        val newDes = "white 1 King-white-e1-0 Knight-black-g1 " + "King-black-e8-0"
        assertEquals(newDes, game.toString())
    }

    @Test
    fun testIsSelectable() {
        var des = "white 0 King-white-e1-0 Queen-black-g2 " + "Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0"
        var game = ChessGame(des)
        assertFalse(game.isSelectable(Position.byCode("e1"), true))
        assertTrue(game.isSelectable(Position.byCode("e6"), true))
        assertFalse(game.isSelectable(Position.byCode("e6"), false))

        des = "black 0 King-white-e1-0 Queen-white-e2 " + "Bishop-black-c8 King-black-e8-0"
        game = ChessGame(des)
        assertTrue(game.isSelectable(Position.byCode("c8"), false))

        des = "black 0 King-white-e1-0 Queen-white-e2 " + "Rook-black-a6-1 King-black-e8-0"
        game = ChessGame(des)
        assertTrue(game.isSelectable(Position.byCode("a6"), false))
    }

    @Test
    fun testIsMovable() {
        var des = "white 0 King-white-e1-0 Queen-black-g2 " + "Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0"
        var game = ChessGame(des)


        assertTrue(game.isMovable(Position.byCode("e6"), Position.byCode("e7"), true))

        des = "black 0 King-white-e1-0 Pawn-black-a5-false " + "King-black-g6-2 Rook-white-h6-1"
        game = ChessGame(des)
        assertFalse(game.isMovable(Position.byCode("a5"), Position.byCode("a4"), false))

        des = "black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0"
        game = ChessGame(des)
        assertFalse(game.isMovable(Position.byCode("e8"), Position.byCode("g8"), false))

        game = ChessGame(621)
        game.move(Move.byCode("f2-f3"))
        game.move(Move.byCode("a7-a6"))
        val from = Position.byCode("f1")
        val to = Position.byCode("f2")
        assertTrue(game.isMovable(from, to, true))
        assertFalse(game.isFreeArea(from))
    }

    @Test
    fun testColorChangedBetweenMoves() {
        val des = "white 0 King-white-e1-0 Pawn-black-g3-false " + "King-black-e8-0"
        val game = ChessGame(des)
        game.move(Move.byCode("e1-d1"))
        try {
            game.move(Move.byCode("d1-c1"))
            fail()
        } catch (e: AssertionError) {
        }

    }

    @Test
    fun testChecksForMoveMovesFigureNotNull() {
        val des = "white 0 King-white-e1-0 Pawn-black-g3-false " + "King-black-e8-0"
        val game = ChessGame(des)
        try {
            game.move(Move.byCode("a1-b1"))
            fail()
        } catch (e: AssertionError) {
        }
    }

    @Test
    fun testHasHitBiggerFigure() {
        val des = ("white 0 King-white-h2-3 Queen-black-h3 "
                + "Pawn-white-g2-false Bishop-white-e7 King-black-e8-0 "
                + "Knight-black-g5 Pawn-white-a6-false")
        val game = ChessGame(des)
        assertTrue(game.hasHitFigure)            //da numberWithoutHit=0 ist in 'des'
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
    fun testCountFigures() {
        val des = "white 0 King-white-e1-0 Pawn-black-a5-true " + "Pawn-white-b5-false Pawn-white-e7-false King-black-e8-0"
        val game = ChessGame(des)
        assertEquals(5, game.countFigures())
        game.move(Move.byCode("b5-a6"))
        assertEquals(4, game.countFigures())
        game.move(Move.byCode("e8-e7"))
        assertEquals(3, game.countFigures())
        game.undo()
        assertEquals(4, game.countFigures())
        game.undo()
        assertEquals(5, game.countFigures())
    }

    @Test(dataProvider = "getTestGetPossibleMovesData")
    fun testGetPossibleMoves(game: ChessGame, moveCodes: List<String>, expectedPossibleMovesCount: Int) {
        val moves = moveCodes.map { Move.byCode(it) }
        for (move in moves) {
            val isWhiteTurn = game.isWhiteTurn
            val isMovable = game.isMovable(move.from, move.to, isWhiteTurn)
            assertTrue(isMovable, "$move should be valid")
            game.move(move)
        }
        val possibleMoves = game.getAllMoves()
        assertEquals(expectedPossibleMovesCount, possibleMoves.size, "possible move count")
    }


    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf(ChessGame(518), listOf("g1-f3", "b8-c6", "f3-g1", "c6-b4", "g1-f3", "b4-c2"), 1),
            arrayOf(ChessGame("black 0 King-white-g1-2 Bishop-black-b6 King-black-e8-0"), listOf("b6-c5"), 4),
            arrayOf(ChessGame("black 0 Rook-white-a1-0 Rook-white-f1-1 King-white-g1-1-true "
                    + "Pawn-white-a2-false Pawn-white-b2-false Bishop-white-d2 Bishop-white-e2 "
                    + "Pawn-white-f2-false Pawn-white-h2-false Queen-white-b3 Pawn-white-g3-false "
                    + "Pawn-white-e4-false Pawn-black-b5-false Pawn-black-a6-false Bishop-black-b6 "
                    + "Pawn-black-h6-false Bishop-black-b7 Pawn-black-f7-false Pawn-black-g7-false "
                    + "Rook-black-c8-1 Queen-black-d8 Rook-black-f8-1 King-black-g8-1"), listOf("b6-f2"), 4),
            arrayOf(ChessGame("black 0 Pawn-white-b2-false King-white-d3-2 Rook-black-h4-1 Rook-black-a8-0 King-black-e8-0"), listOf("a8-a3"), 5),
            arrayOf(ChessGame("black 0 King-white-d3-2 Knight-black-e5 Bishop-black-g8 King-black-e8-0"), listOf("g8-h7"), 5),
            arrayOf(ChessGame("white 2 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 King-white-e1-0 " +
                    "Queen-white-d1 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 Pawn-white-a2-false " +
                    "Pawn-white-b2-false Pawn-white-d2-false Pawn-white-e2-false " +
                    "Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                    "Pawn-white-c3-false Pawn-black-d6-false Pawn-black-a7-false " +
                    "Pawn-black-b7-false Pawn-black-c7-false Pawn-black-e7-false Pawn-black-f7-false " +
                    "Pawn-black-g7-false Pawn-black-h7-false Rook-black-a8-0 Knight-black-b8 " +
                    "Bishop-black-c8 Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0"), listOf("d1-a4"), 6),
            arrayOf(ChessGame("black 0 King-white-e1-0 Rook-white-d2-2 Queen-black-e2 " + "Bishop-black-b4 King-black-e8-0"), listOf("b4-c3"), 1),
            arrayOf(ChessGame("black 0 King-white-g1-2 Pawn-black-c4-false Pawn-white-d4-true " + "Bishop-black-b6 King-black-e8-0"), listOf("c4-d3"), 4),
            arrayOf(ChessGame("black 0 King-white-h1-3 Pawn-white-c7-false "
                    + "Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
                    + "Knight-black-a7 King-black-b7-3-false"), listOf("b7-c6", "c7-c8"), 1),
            arrayOf(ChessGame("black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0"), listOf<String>(), 12),
            arrayOf(ChessGame("white 0 King-white-g6-6 Pawn-white-g7-false King-black-e8-0 Knight-black-h8"), listOf<String>(), 7),
            arrayOf(ChessGame("white 0 Rook-white-b1-0 King-white-d1-0 Rook-white-e1-0 Rook-black-h1-1 Rook-black-a2-1 Knight-black-d3 King-black-d8-0"), listOf<String>(), 12),
            arrayOf(ChessGame(518), listOf("e2-e4", "d7-d5", "f1-b5", "c7-c6", "b5-c6", "b8-d7", "c6-b5"), 19),
            arrayOf(ChessGame(621), listOf("g2-g3", "f7-f6", "c2-c3", "g8-f7", "d1-c2", "e8-f8", "c2-h7"), 1),
            arrayOf(ChessGame("white 0 Rook-black-e1-8 Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 Knight-white-g4 Queen-black-e8 King-black-g7-3"),
                listOf("f2-e1", "e2-f1"), 2),
            arrayOf(ChessGame("white 0 Rook-white-b1-0 King-white-d1-0 Rook-white-e1-0 Bishop-black-d3 King-black-d8-0"), listOf<String>(), 22)
    )

    @Test
    fun testGetPossibleMovesAfterIndirectChessAfterEnpassent() {
        game.moves(listOf("e2-e4", "d7-d5", "e4-e5", "e8-d7", "d1-g4", "f7-f5", "e5-f6")) //en-passant creates indirect chess path
        assertTrue(game.getLastMove()!! is ExtendedMove.Enpassant)
        val possibleMoves = game.getAllMoves()
        val actualMoveCodes = possibleMoves.asSequence().map { move -> move.toString() }.toSet()
        val expectedMoveCodes = setOf("d7-e8", "d7-c6", "d7-d6", "e7-e6")
        assertEquals(expectedMoveCodes, actualMoveCodes, "expected#: ${expectedMoveCodes.size}, actual#: ${possibleMoves.size}")
    }

    @Test
    fun testIsCheck() {
        val des = "white 0 King-white-g1-2 Bishop-black-f2 King-black-e8-0"
        val game = ChessGame(des)
        assertTrue(game.isCheck(true))
    }

    @Test
    fun testGetHistory() {
        val game = ChessGame(621)
        game.move(Move.byCode("g2-g3"))
        game.move(Move.byCode("f7-f6"))
        assertEquals(game.history, "g2-g3,f7-f6")
        game.move(Move.byCode("c2-c3"))
        game.move(Move.byCode("g8-f7"))
        assertEquals(game.history, "g2-g3,f7-f6,c2-c3,g8-f7")
        game.move(Move.byCode("d1-c2"))
        game.move(Move.byCode("a7-a6"))
        game.move(Move.byCode("c2-h7"))
        assertEquals(game.history, "g8-f7,d1-c2,a7-a6,c2-h7")
    }

    @Test(dataProvider = "getCompleteHistoryData")
    fun testGetCompleteHistory(initialPos: Int, expectedCompleteHistory: String) {
        val game = ChessGame(initialPos)
        val moves = expectedCompleteHistory.split(',').map { Move.byCheckedCode(it) }
        for(move in moves) {
            assertTrue(game.isMovable(move.from, move.to, game.isWhiteTurn), "move $move isn't allowed in sequence $expectedCompleteHistory")
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
