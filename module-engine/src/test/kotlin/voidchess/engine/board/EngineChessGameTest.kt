package voidchess.engine.board

import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.common.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.MoveResultType
import voidchess.common.board.move.Position
import voidchess.common.board.other.StartConfig
import voidchess.common.figures.Bishop
import voidchess.common.figures.King
import voidchess.initChessGame
import voidchess.toChess960Config
import voidchess.toManualConfig
import kotlin.test.*


internal class EngineChessGameTest {
    private lateinit var game: EngineChessGameImpl

    @BeforeMethod
    fun setup() {
        game = EngineChessGameImpl()
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
        assertEquals(code, game.toString())
    }

    @Test
    fun testEquals() {
        val copy = EngineChessGameImpl(game.toString().toManualConfig())
        assertTrue(game.equalsOther(copy))
    }

    @Test
    fun testCopy() {
        val game = EngineChessGameImpl(StartConfig.ClassicConfig, listOf(Move.byCode("e2-e4")))
        val gameDes = game.toString()
        val copies = game.copyGame(3)

        for (copy in copies) {
            assertEquals(gameDes, copy.toString(), "copy and game")
        }

        for(i in 0 until copies.size-1) {
            assertNotSame(copies[i], copies[i+1], "all copies should be distinct objects but instance $i and ${i+1} are the same instance")
        }
    }

    @Test
    fun testWithMove() {
        var des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0"
        var game = EngineChessGameImpl(des.toManualConfig())
        assertEquals(des, game.toString())
        var move = Move.byCode("e1-h1")
        game.withMove(move) {
            val newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"

            assertEquals(newDes, game.toString())
        }
        assertEquals(des, game.toString())


        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("f8-g8")
        game.withMove(move) {}
        assertEquals(des, game.toString())


        des = "white 0 King-white-e1-0 Pawn-white-b5-false Pawn-black-c5-true King-black-e8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("b5-c6")
        assertTrue(game.isMovable(move.from, move.to))
        game.withMove(move) {}
        assertTrue(game.isMovable(move.from, move.to))


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
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("e1-a1")
        assertTrue(game.isMovable(move.from, move.to))
        game.withMove(move) {}
        assertTrue(game.isMovable(move.from, move.to))


        des = "black 1 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("a6-b6")
        game.withMove(move) {}
        assertEquals(game.toString(), des)


        des = "white 1 King-white-h1-4 Pawn-white-a2-false King-black-a6-6"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("a2-a4")
        game.withMove(move) {}
        assertEquals(game.toString(), des)


        des = "white 0 King-white-h1-4 Pawn-white-a2-false Pawn-black-b4-false King-black-a6-6"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("a2-a4")
        game.withMove(move) {
            val newDes = "black 1 King-white-h1-4 Pawn-white-a4-true Pawn-black-b4-false King-black-a6-6"

            move = Move.byCode("b4-a3")
            game.withMove(move) {
                move = Move.byCode("h1-h2")
                game.withMove(move) {
                    move = Move.byCode("a3-a2")
                    game.withMove(move) {}
                }
            }

            assertEquals(newDes, game.toString())

            move = Move.byCode("b4-b3")
            game.withMove(move) {
                move = Move.byCode("h1-h2")
                game.withMove(move) {
                    move = Move.byCode("b3-b2")
                    game.withMove(move) {}
                }
            }

            assertEquals(newDes, game.toString())
        }


        des = "white 0 King-white-h1-3 King-black-b7-3 Pawn-white-c7-false"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("c7Qc8")
        game.withMove(move) {}
        assertEquals(des, game.toString())


        game = EngineChessGameImpl(314.toChess960Config())
        des = game.toString()
        game.withMove(Move.byCode("d1-c1")) {
            game.withMove(Move.byCode("d8-c8")) {}
        }
        assertEquals(des, game.toString())


        game = initChessGame(707,"e1-f3","b7-b6","f1-e3","g7-g6","d1-e1","a8-f3") as EngineChessGameImpl
        des = game.toString()
        game.withMove(Move.byCode("c1-b1")) {}
        assertEquals(des, game.toString())


        des = "white 0 King-white-e1-0 Pawn-white-c2-false King-black-e8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move[Position.byCode("c2"), Position.byCode("c4")]
        game.withMove(move) {
            val newDes = "black 1 King-white-e1-0 Pawn-white-c4-true King-black-e8-0"
            assertEquals(newDes, game.toString())
        }

        des = "white 0 Rook-white-b1-1 King-white-h1-4 Knight-white-d4 King-black-a6-6"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move.byCode("d4-c6")
        game.withMove(move) { moveResult ->
            assertEquals(MoveResultType.STALEMATE, moveResult)
        }
    }

    @Test
    fun testHandleEnpassant() {
        val des = "white 0 Pawn-white-c2-false Pawn-black-b4-false King-white-e1-0 King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        assertFalse(game.getFigure(Position.byCode("c2")).canBeHitEnpassant, "pawn can't be hit enpassant on start row")
        game.withMove(Move.byCode("c2-c4")) {
            assertTrue(
                game.getFigure(Position.byCode("c4")).canBeHitEnpassant,
                "pawn can be hit enpassant after enpassant"
            )
            game.withMove(Move.byCode("e8-f8")) {
                assertFalse(
                    game.getFigure(Position.byCode("c4")).canBeHitEnpassant,
                    "pawn can't be hit enpassant after black moved king"
                )
            }
            assertTrue(
                game.getFigure(Position.byCode("c4")).canBeHitEnpassant,
                "pawn can be hit enpassant after king move was undone"
            )
            assertTrue(game.isMovable(Position.byCode("b4"), Position.byCode("c3")))
            game.withMove(Move.byCode("b4-c3")) {
                val newDes = "white 0 King-white-e1-0 Pawn-black-c3-false King-black-e8-0"
                assertEquals(newDes, game.toString())
            }
        }
    }

    @Test
    fun testHandleCastling() {
        var des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0"
        var game = EngineChessGameImpl(des.toManualConfig())
        var move = Move[Position.byCode("e8"), Position.byCode("a8")]
        game.withMove(move) {
            val newDes = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1"
            assertEquals(newDes, game.toString())
        }

        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move[Position.byCode("f8"), Position.byCode("a8")]
        game.withMove(move) {
            val newDes = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1"
            assertEquals(newDes, game.toString())
        }

        des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move[Position.byCode("e1"), Position.byCode("h1")]
        game.withMove(move) {
            val newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
            assertEquals(newDes, game.toString())
        }

        des = "white 0 King-white-g1-0 Rook-white-h1-0 King-black-e8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move[Position.byCode("g1"), Position.byCode("h1")]
        game.withMove(move) {
            val newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
            assertEquals(newDes, game.toString())
        }

        des = "white 0 King-white-f1-0 Rook-white-g1-0 King-black-e8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        move = Move[Position.byCode("f1"), Position.byCode("g1")]
        game.withMove(move) {
            val newDes = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0"
            assertEquals(newDes, game.toString())
        }

        game = EngineChessGameImpl(314.toChess960Config())
        game.withMove(Move.byCode("d1-c1")) {
            val expectedNewDesStart =
                "black 1 Knight-white-a1 Queen-white-b1 King-white-c1-1-true Rook-white-d1-1 Bishop-white-e1 Bishop-white-f1 Rook-white-g1-0 Knight-white-h1"
            assertEquals(expectedNewDesStart, game.toString().substring(0, expectedNewDesStart.length))
        }
    }

    @Test
    fun testIsMatt() {
        val des = "black 0 King-white-e1-0 Queen-black-h2 Pawn-black-f3-false King-black-e8-0"
        val game = initChessGame(des)
        game.withMove(Move.byCode("h2-e2")) {moveResult->
            assertEquals(MoveResultType.CHECKMATE, moveResult)
        }
    }

    @Test
    fun testIsDrawBecauseOfNoMoves() {
        val des = "black 0 King-white-e1-0 Queen-black-h2 Pawn-black-c2-false Pawn-white-e7-false King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        game.withMove(Move.byCode("h2-g2")) {moveResult->
            assertEquals(MoveResultType.STALEMATE, moveResult)
        }
    }

    @Test
    fun testIsDrawBecauseOfLowMaterial() {
        val des = "white 0 King-white-e1-0 Bishop-black-g2 Knight-white-c2 Knight-white-e7 King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        game.withMove(Move.byCode("e1-f2")) {moveResult->
            assertEquals(MoveResultType.DRAW, moveResult)
        }
    }

    @Test
    fun testIsDrawBecauseOfThreeTimesSamePosition() {
        val whiteMove = "c2-a1"
        val whiteReturn = "a1-c2"
        val blackMove = "g2-h3"
        val blackReturn = "h3-g2"
        val des = "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 Knight-white-c2 Knight-white-e7 King-black-e8-0"
        val game = initChessGame(des, whiteMove, blackMove, whiteReturn, blackReturn, whiteMove, blackMove)

        game.withMove(Move.byCode(whiteReturn)) { moveResult1 ->
            assertEquals(moveResult1, MoveResultType.NO_END)
            game.withMove(Move.byCode(blackReturn)) { moveResult2 ->
                assertEquals(moveResult2, MoveResultType.THREE_TIMES_SAME_POSITION)
            }
        }
    }

    @Test
    fun testIsDrawBecauseOf50HitlessMoves() {
        var des = "white 98 King-white-e1-0 Pawn-white-a2-false Pawn-black-b4-false King-black-e8-0"
        var game = EngineChessGameImpl(des.toManualConfig())
        game.withMove(Move.byCode("a2-a4")) {moveResult1->
            assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, moveResult1)
            game.withMove(Move.byCode("b4-a3")) {moveResult2->
                assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, moveResult2)
            }
        }

        des = "white 98 King-white-e1-0 Pawn-white-a2-false Pawn-black-b4-false King-black-e8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        game.withMove(Move.byCode("a2-a4")) {moveResult1->
            assertNotEquals(MoveResultType.FIFTY_MOVES_NO_HIT, moveResult1)
            game.withMove(Move.byCode("b4-b3")) {moveResult2->
                assertEquals(MoveResultType.FIFTY_MOVES_NO_HIT, moveResult2)
            }
        }
    }

    @Test
    fun testHandleTransformPawn() {
        val des = "black 0 King-white-e1-0 Pawn-black-g2-false King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        game.withMove(Move.byCode("g2Bg1")) {
            val newDes = "white 1 King-white-e1-0 Bishop-black-g1 King-black-e8-0"
            assertEquals(newDes, game.toString())
        }
    }

    @Test
    fun testIsMovable() {
        var des = "white 0 King-white-e1-0 Queen-black-g2 Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0"
        var game = EngineChessGameImpl(des.toManualConfig())
        assertTrue(game.isMovable(Position.byCode("e6"), Position.byCode("e7")))

        des = "black 0 King-white-e1-0 Pawn-black-a5-false King-black-g6-2 Rook-white-h6-1"
        game = EngineChessGameImpl(des.toManualConfig())
        assertFalse(game.isMovable(Position.byCode("a5"), Position.byCode("a4")))

        des = "black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0"
        game = EngineChessGameImpl(des.toManualConfig())
        assertFalse(game.isMovable(Position.byCode("e8"), Position.byCode("g8")))

        game = EngineChessGameImpl(621.toChess960Config())
        game.withMove(Move.byCode("f2-f3")) {
            game.withMove(Move.byCode("a7-a6")) {
                val from = Position.byCode("f1")
                val to = Position.byCode("f2")
                assertTrue(game.isMovable(from, to))
                assertFalse(game.isFreeArea(from))
            }
        }
    }

    @Test
    fun testColorChangedBetweenMoves() {
        val des = "white 0 King-white-e1-0 Pawn-black-g3-false King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        assertTrue(game.isWhiteTurn)
        game.withMove(Move.byCode("e1-d1")) {
            assertFalse(game.isWhiteTurn)
            game.withMove(Move.byCode("d1-c1")) {
                assertTrue(game.isWhiteTurn)
            }
        }
    }

    @Test
    fun testChecksForMoveMovesFigureNotNull() {
        val des = "white 0 King-white-e1-0 Pawn-black-g3-false King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        try {
            game.withMove(Move.byCode("a1-b1")) {}
            fail()
        } catch (e: IllegalStateException) {
        }
    }

    @Test
    fun testHasHitFigure() {
        val des = ("white 0 King-white-h2-3 Queen-black-h3 "
                + "Pawn-white-g2-false Bishop-white-e7 King-black-e8-0 "
                + "Knight-black-g5 Pawn-white-a6-false")
        val game = EngineChessGameImpl(des.toManualConfig())
        game.withMove(Move.byCode("g2-h3")) {
            assertTrue(game.latestExtendedMove.hasHitFigure)
            game.withMove(Move.byCode("g5-h3")) {
                assertTrue(game.latestExtendedMove.hasHitFigure)
                game.withMove(Move.byCode("h2-h3")) {
                    assertTrue(game.latestExtendedMove.hasHitFigure)
                    game.withMove(Move.byCode("e8-e7")) {
                        assertTrue(game.latestExtendedMove.hasHitFigure)
                        game.withMove(Move.byCode("a6-a7")) {
                            assertFalse(game.latestExtendedMove.hasHitFigure)
                            game.withMove(Move.byCode("e7-d7")) {
                                assertFalse(game.latestExtendedMove.hasHitFigure)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test(dataProvider = "getTestGetPossibleMovesData")
    fun testGetPossibleMoves(startConfig: StartConfig, moveCodes: Array<String>, expectedPossibleMovesCount: Int) {
        val game = initChessGame(startConfig, *moveCodes)
        val possibleMoves = game.getAllMoves()
        assertEquals(expectedPossibleMovesCount, possibleMoves.size, "possible move count")
    }


    @DataProvider
    fun getTestGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf(518.toChess960Config(), arrayOf("g1-f3", "b8-c6", "f3-g1", "c6-b4", "g1-f3", "b4-c2"), 1),
        arrayOf(
            "black 0 King-white-g1-2 Bishop-black-b6 King-black-e8-0".toManualConfig(),
            arrayOf("b6-c5"),
            4
        ),
        arrayOf(
            ("black 0 Rook-white-a1-0 Rook-white-f1-1 King-white-g1-1-true "
                    + "Pawn-white-a2-false Pawn-white-b2-false Bishop-white-d2 Bishop-white-e2 "
                    + "Pawn-white-f2-false Pawn-white-h2-false Queen-white-b3 Pawn-white-g3-false "
                    + "Pawn-white-e4-false Pawn-black-b5-false Pawn-black-a6-false Bishop-black-b6 "
                    + "Pawn-black-h6-false Bishop-black-b7 Pawn-black-f7-false Pawn-black-g7-false "
                    + "Rook-black-c8-1 Queen-black-d8 Rook-black-f8-1 King-black-g8-1"
                    ).toManualConfig(),
            arrayOf("b6-f2"),
            4
        ),
        arrayOf(
            "black 0 Pawn-white-b2-false King-white-d3-2 Rook-black-h4-1 Rook-black-a8-0 King-black-e8-0".toManualConfig(),
            arrayOf("a8-a3"),
            5
        ),
        arrayOf(
            "black 0 King-white-d3-2 Knight-black-e5 Bishop-black-g8 King-black-e8-0".toManualConfig(),
            arrayOf("g8-h7"),
            5
        ),
        arrayOf(
            ("white 2 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 King-white-e1-0 " +
                    "Queen-white-d1 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 Pawn-white-a2-false " +
                    "Pawn-white-b2-false Pawn-white-d2-false Pawn-white-e2-false " +
                    "Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                    "Pawn-white-c3-false Pawn-black-d6-false Pawn-black-a7-false " +
                    "Pawn-black-b7-false Pawn-black-c7-false Pawn-black-e7-false Pawn-black-f7-false " +
                    "Pawn-black-g7-false Pawn-black-h7-false Rook-black-a8-0 Knight-black-b8 " +
                    "Bishop-black-c8 Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0"
                    ).toManualConfig()
            , arrayOf("d1-a4"),
            6
        ),
        arrayOf(
            "black 0 King-white-e1-0 Rook-white-d2-2 Queen-black-e2 Bishop-black-b4 King-black-e8-0".toManualConfig(),
            arrayOf("b4-c3"),
            1
        ),
        arrayOf(
            "black 0 King-white-g1-2 Pawn-black-c4-false Pawn-white-d4-true Bishop-black-b6 King-black-e8-0".toManualConfig(),
            arrayOf("c4-d3"),
            4
        ),
        arrayOf(
                ("black 0 King-white-h1-3 Pawn-white-c7-false "
                        + "Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
                        + "Knight-black-a7 King-black-b7-3-false").toManualConfig(),
            arrayOf("b7-c6", "c7Qc8"),
            1
        ),
        arrayOf(
            "black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0".toManualConfig(),
            emptyArray<String>(),
            12
        ),
        arrayOf(
            "white 0 King-white-g6-6 Pawn-white-g7-false King-black-e8-0 Knight-black-h8".toManualConfig(),
            emptyArray<String>(),
            7
        ),
        arrayOf(
            "white 0 Rook-white-b1-0 King-white-d1-0 Rook-white-e1-0 Rook-black-h1-1 Rook-black-a2-1 Knight-black-d3 King-black-d8-0".toManualConfig(),
            emptyArray<String>(),
            12
        ),
        arrayOf(
            518.toChess960Config(),
            arrayOf("e2-e4", "d7-d5", "f1-b5", "c7-c6", "b5-c6", "b8-d7", "c6-b5"),
            19
        ),
        arrayOf(
            621.toChess960Config(),
            arrayOf("g2-g3", "f7-f6", "c2-c3", "g8-f7", "d1-c2", "e8-f8", "c2-h7"),
            1
        ),
        arrayOf(
            "white 0 Rook-black-e1-8 Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 Knight-white-g4 Queen-black-e8 King-black-g7-3".toManualConfig(),
            arrayOf("f2-e1", "e2Qf1"),
            2
        ),
        arrayOf(
            "white 0 Rook-white-b1-0 King-white-d1-0 Rook-white-e1-0 Bishop-black-d3 King-black-d8-0".toManualConfig(),
            emptyArray<String>(),
            22
        )
    )

    @Test
    fun testGetPossibleMovesAfterIndirectChessAfterEnpassant() {
        val game = initChessGame(
            518, "e2-e4", "d7-d5", "e4-e5", "e8-d7", "d1-g4", "f7-f5", "e5-f6"
        )
        //en-passant creates indirect chess path
        val possibleMoves = game.getAllMoves()
        val actualMoveCodes = possibleMoves.asSequence().map { move -> move.toString() }.toSet()
        val expectedMoveCodes = setOf("d7-e8", "d7-c6", "d7-d6", "e7-e6")
        assertEquals(
            expectedMoveCodes,
            actualMoveCodes,
            "expected#: ${expectedMoveCodes.size}, actual#: ${possibleMoves.size}"
        )
    }

    @Test
    fun testIsCheck() {
        val des = "white 0 King-white-g1-2 Bishop-black-f2 King-black-e8-0"
        val game = EngineChessGameImpl(des.toManualConfig())
        assertTrue(game.isCheck)
    }

    @Test(dataProvider = "getShortTermHistoryData")
    fun testShortTermHistory(chess960: Int, moves: Array<String>, expectedShortTermHistory: String) {
        val game = initChessGame(chess960, *moves)
        assertEquals(expectedShortTermHistory, game.shortTermHistory)
    }

    @DataProvider
    fun getShortTermHistoryData(): Array<Array<Any>> {
        fun String.splitToArray() = this.split(",").toTypedArray()
        return arrayOf(
            arrayOf<Any>(621, "g2-g3,f7-f6".splitToArray(), "g2-g3,f7-f6"),
            arrayOf<Any>(621, "g2-g3,f7-f6,c2-c3,g8-f7".splitToArray(), "g2-g3,f7-f6,c2-c3,g8-f7"),
            arrayOf<Any>(621, "g2-g3,f7-f6,c2-c3,g8-f7,d1-c2,a7-a6,c2-h7".splitToArray(), "g8-f7,d1-c2,a7-a6,c2-h7")
        )
    }

    @Test(dataProvider = "getCompleteHistoryData")
    fun testGetCompleteHistory(initialPos: Int, expectedCompleteHistory: String) {
        val moveArray = expectedCompleteHistory.split(",").toTypedArray()
        val game = initChessGame(initialPos, *moveArray)
        assertEquals(expectedCompleteHistory, game.completeHistory)
    }

    @DataProvider
    fun getCompleteHistoryData(): Array<Array<Any>> = arrayOf(
        arrayOf(621, "g2-g3,f7-f6,c2-c3,g8-f7,d1-c2,a7-a6,c2-h7"),
        arrayOf(518, "g2-g3,g7-g6,g1-f3,g8-f6,f1-g2,f8-g7,e1-h1,e8-h8"),
        arrayOf(652, "c2-c3,c7-c6,c1-b3,c8-b6,b1-c2,b8-c7,d1-a1,d8-a8")
    )
}
