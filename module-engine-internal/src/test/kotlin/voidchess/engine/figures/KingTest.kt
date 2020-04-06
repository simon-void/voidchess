package voidchess.engine.figures

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.engine.board.ChessGame
import voidchess.engine.board.ChessBoard
import voidchess.engine.board.getFigure
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import voidchess.getPossibleMovesFrom
import voidchess.initChessBoard
import voidchess.initSimpleChessBoard
import voidchess.toTargetPosAsStringSet
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class KingTest {
    @Test(dataProvider = "getIsSelectableData")
    fun testIsSelectable(game: ChessBoard, kingPosCode: String, expectedIsSelectable: Boolean) {
        val king = game.getFigure(Position.byCode(kingPosCode)) as King
        assertEquals(expectedIsSelectable, king.isSelectable(game), "isSelectable")
    }

    @DataProvider
    fun getIsSelectableData(): Array<Array<Any>> = arrayOf(
            arrayOf(initSimpleChessBoard(518), "e1", false),
            arrayOf(initSimpleChessBoard(518, "f2-f3", "f7-f6"), "e1", true),
            arrayOf(initSimpleChessBoard(613), "f1", true),
            arrayOf(initSimpleChessBoard(380), "d1", true)
    )

    @Test
    fun testIsReachable() {
        var des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0"
        var game = ChessGame(des)

        var from = Position.byCode("e1")
        var to1 = Position.byCode("f1")
        var to2 = Position.byCode("g1")
        var to3 = Position.byCode("d1")
        var to4 = Position.byCode("c1")
        var to5 = Position.byCode("d2")
        var to6 = Position.byCode("e3")
        var to7 = Position.byCode("a1")
        val to8 = Position.byCode("h1")

        var king = King(true, from)
        assertTrue(king.isReachable(to1, game))
        assertFalse(king.isReachable(to2, game))
        assertTrue(king.isReachable(to3, game))
        assertFalse(king.isReachable(to4, game))
        assertTrue(king.isReachable(to5, game))
        assertFalse(king.isReachable(to6, game))
        assertFalse(king.isReachable(from, game))
        assertTrue(king.isReachable(to7, game))
        assertTrue(king.isReachable(to8, game))


        des = "black 0 Rook-black-a8-0 Knight-black-b8 King-black-c8-0 Rook-black-h8-0 King-white-e1-0"
        game = ChessGame(des)

        from = Position.byCode("c8")
        to1 = Position.byCode("a8")
        to2 = Position.byCode("d8")
        to3 = Position.byCode("d7")
        to4 = Position.byCode("b8")
        to5 = Position.byCode("g8")
        to6 = Position.byCode("e6")
        to7 = Position.byCode("h8")

        king = King(false, from)
        assertFalse(king.isReachable(to1, game))
        assertTrue(king.isReachable(to2, game))
        assertTrue(king.isReachable(to3, game))
        assertFalse(king.isReachable(to4, game))
        assertFalse(king.isReachable(to5, game))
        assertFalse(king.isReachable(to6, game))
        assertTrue(king.isReachable(to7, game))
        assertFalse(king.isReachable(from, game))


        game = ChessGame(621)
        from = Position.byCode("e1")
        to1 = Position.byCode("f1")
        king = King(true, from)
        assertFalse(king.isReachable(to1, game))
    }

    @Test(dataProvider = "getReachableByCastlingData")
    fun testIsReachableByCastling(gameDes: String, kingPosCode: String, rookPosCode: String, expectedCanCastle: Boolean) {
        val game = ChessGame(gameDes)
        val kingPos = Position.byCode(kingPosCode)
        val rookPos = Position.byCode(rookPosCode)
        val king = game.getFigure(kingPos) as King
        val rook = game.getFigure(rookPos) as Rook
        assertTrue(king.canCastle(), "king can castle")
        assertTrue(rook.canCastle(), "rook can castle")
        assertEquals(king.isReachable(rookPos, game), expectedCanCastle, "can castle")
    }

    @DataProvider
    fun getReachableByCastlingData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 Rook-white-a1-0 King-white-g1-0 King-black-e8-0", "g1", "a1", true),
            arrayOf("white 0 Rook-white-c1-0 King-white-g1-0 King-black-e8-0", "g1", "c1", true),
            arrayOf("white 0 Rook-white-f1-0 King-white-g1-0 King-black-e8-0", "g1", "f1", true),
            arrayOf("white 0 Rook-white-a1-0 Bishop-white-b1 King-white-g1-0 King-black-e8-0", "g1", "a1", false),
            arrayOf("white 0 Bishop-white-b1 Rook-white-c1-0 King-white-g1-0 King-black-e8-0", "g1", "c1", true),
            arrayOf("white 0 Bishop-white-b1 Rook-white-f1-0 King-white-g1-0 King-black-e8-0", "g1", "f1", true),
            arrayOf("white 0 Rook-white-a1-0 Bishop-white-e1 King-white-g1-0 King-black-e8-0", "g1", "a1", false),
            arrayOf("white 0 Rook-white-c1-0 Bishop-white-e1 King-white-g1-0 King-black-e8-0", "g1", "c1", false),
            arrayOf("white 0 Bishop-white-e1 Rook-white-f1-0 King-white-g1-0 King-black-e8-0", "g1", "f1", false),
            arrayOf("white 0 Rook-white-a1-0 Bishop-black-e1 King-white-g1-0 King-black-e8-0", "g1", "a1", false),
            arrayOf("white 0 Rook-white-c1-0 Bishop-black-e1 King-white-g1-0 King-black-e8-0", "g1", "c1", false),
            arrayOf("white 0 Bishop-black-e1 Rook-white-f1-0 King-white-g1-0 King-black-e8-0", "g1", "f1", false),
            arrayOf("white 0 Rook-white-a1-0 King-white-b1-0 Bishop-white-e1 King-black-e8-0", "b1", "a1", true),
            arrayOf("white 0 Rook-white-a1-0 King-white-b1-0 Bishop-white-d1 King-black-e8-0", "b1", "a1", false),
            arrayOf("white 0 Rook-white-a1-0 King-white-b1-0 Bishop-white-c1 King-black-e8-0", "b1", "a1", false),
            arrayOf("white 0 Bishop-white-a1 Rook-white-b1-0 King-white-c1-0 King-black-e8-0", "c1", "b1", true),
            arrayOf("white 0 Bishop-white-b1 Rook-white-c1-0 King-white-d1-0 Bishop-white-e1 King-black-e8-0", "d1", "c1", true),

            arrayOf("black 0 Rook-black-h8-0 King-black-b8-0 King-white-e1-0", "b8", "h8", true),
            arrayOf("black 0 Rook-black-g8-0 King-black-b8-0 King-white-e1-0", "b8", "g8", true),
            arrayOf("black 0 Rook-black-c8-0 King-black-b8-0 King-white-e1-0", "b8", "c8", true),
            arrayOf("black 0 Rook-black-h8-0 Bishop-black-g8 King-black-b8-0 King-white-e1-0", "b8", "h8", false),
            arrayOf("black 0 Bishop-black-h8 Rook-black-g8-0 King-black-b8-0 King-white-e1-0", "b8", "g8", true),
            arrayOf("black 0 Bishop-black-h8 Rook-black-c8-0 King-black-b8-0 King-white-e1-0", "b8", "c8", true),
            arrayOf("black 0 Rook-black-h8-0 Bishop-black-e8 King-black-b8-0 King-white-e1-0", "b8", "h8", false),
            arrayOf("black 0 Rook-black-f8-0 Bishop-black-e8 King-black-b8-0 King-white-e1-0", "b8", "f8", false),
            arrayOf("black 0 Bishop-black-e8 Rook-black-c8-0 King-black-b8-0 King-white-e1-0", "b8", "c8", false),
            arrayOf("black 0 Rook-black-h8-0 Bishop-white-e8 King-black-b8-0 King-white-e1-0", "b8", "h8", false),
            arrayOf("black 0 Rook-black-f8-0 Bishop-white-e8 King-black-b8-0 King-white-e1-0", "b8", "f8", false),
            arrayOf("black 0 Bishop-white-e8 Rook-black-c8-0 King-black-b8-0 King-white-e1-0", "b8", "c8", false),
            arrayOf("black 0 Rook-black-h8-0 King-black-g8-0 Bishop-black-e8 King-white-e1-0", "g8", "h8", true),
            arrayOf("black 0 Rook-black-h8-0 King-black-g8-0 Bishop-black-f8 King-white-e1-0", "g8", "h8", false),
            arrayOf("black 0 Bishop-black-h8 Rook-black-g8-0 King-black-f8-0 King-white-e1-0", "f8", "g8", true),
            arrayOf("black 0 Bishop-black-h8 Rook-black-g8-0 King-black-f8-0 Bishop-black-e8 King-white-e1-0", "f8", "g8", true)
    )

    @Test(dataProvider = "getGetPossibleMovesData")
    fun testGetPossibleMoves(gameDes: String, posCode: String, expectedPossibleMoveCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)
        val possibleMoves = game.getPossibleMovesFrom(posCode)
        val actualPossibleMoveCodes = possibleMoves.asSequence().map { move-> move.to.toString() }.toSet()
        assertEquals(expectedPossibleMoveCodes, actualPossibleMoveCodes, "expected# ${expectedPossibleMoveCodes.size}, actual# ${actualPossibleMoveCodes.size} - king can move to")
    }

    @DataProvider
    fun getGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0", "e1", setOf("a1", "d1", "d2", "e2", "f2", "f1", "h1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Bishop-black-e4 King-black-e8-0", "e1", setOf("a1", "d1", "d2", "e2", "f2", "f1", "h1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Bishop-black-e3 King-black-e8-0", "e1", setOf("d1", "e2", "f1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Bishop-black-e2 King-black-e8-0", "e1", setOf("d2", "e2", "f2")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Knight-black-e3 King-black-e8-0", "e1", setOf("d2", "e2", "f2")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Knight-black-e2 King-black-e8-0", "e1", setOf("d1", "d2", "e2", "f2", "f1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Rook-black-f2-2 Pawn-black-e3-false King-black-e8-0", "e1", setOf("d1", "a1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Rook-black-d2-2 Pawn-black-e3-false King-black-e8-0", "e1", setOf("f1", "h1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-b3-8 Pawn-black-c5-false King-black-d5-8 Knight-black-e1", "b3", setOf("c3", "b2", "a2", "a3", "a4")),
            arrayOf("white 0 King-black-e8-0 Queen-black-d8 King-white-d3-8 Bishop-black-c4", "d3", setOf("c4", "c3", "c2", "e3", "e4")),
            arrayOf("white 0 King-black-e8-0 Queen-black-d8 King-white-d3-8 Bishop-black-b5", "d3", setOf("c3", "c2", "e3", "e4")),
            arrayOf("white 0 King-black-e8-0 Queen-black-d8 King-white-d3-8 Bishop-black-c4 Pawn-black-b5-false", "d3", setOf("c3", "c2", "e3", "e4")),
            arrayOf("white 0 King-black-e8-0 Queen-black-d8 King-white-d3-8 Bishop-black-c4 Pawn-black-b5-false Knight-black-d1", "d3", setOf("c2", "e4")),
            arrayOf("white 0 King-black-e8-0 Queen-black-d8 King-white-d3-8 Bishop-black-c4 Pawn-black-b5-false Knight-black-d1 Knight-black-e3", "d3", setOf("e4")),
            arrayOf("white 0 King-black-e8-0 Queen-black-d8 King-white-d3-8 Bishop-black-c4 Pawn-black-b5-false Knight-black-d1 Pawn-white-c2-false", "d3", setOf("e4"))
    )

    @Test(dataProvider = "getPossibleMovesAfterInitialMovesData")
    fun testGetPossibleMovesAfterInitialMoves(chess960: Int, moveCodes: List<String>, posCode: String, expectedMoveToCodes: Set<String>) {
        val game1 = initChessBoard(chess960)
        for(moveCode in moveCodes) game1.move(Move.byCode(moveCode))
        val moveFrom1 = game1.getPossibleMovesFrom(posCode)
        val actualMoveToCodes = moveFrom1.toTargetPosAsStringSet()
        assertEquals(expectedMoveToCodes, actualMoveToCodes, "king can move to")
    }

    @DataProvider
    fun getPossibleMovesAfterInitialMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf(621, listOf("f2-f3", "f7-f6", "g1-f2", "g8-f7"), "e1", setOf("f1")),
            arrayOf(621, listOf("f2-f3", "f7-f6", "g1-f2", "g8-f7", "e1-f1"), "e8", setOf("f8")),
            arrayOf(621, listOf("c2-c3", "f7-f6", "d1-c2", "g8-c4", "c2-h7"), "e8", setOf("f7")),
            arrayOf(621, listOf<String>(), "e1", setOf<String>())
    )

    @Test(dataProvider = "getGetCriticalMovesData")
    fun testGetPossibleIrreversibleMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)
        val king = game.getFigure(Position.byCode(posCode))
        val results = TreeSet<Move>()
        king.getCriticalMoves(game, results)
        val actualMoveToCodes = results.toTargetPosAsStringSet()
        assertEquals(expectedMoveToCodes, actualMoveToCodes, "king can move to irreversibly")
    }

    @DataProvider
    fun getGetCriticalMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Rook-white-a1-0 Rook-white-h1-0", "e1", setOf("a1", "h1")),
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Rook-white-a1-0 Rook-white-h1-0 Knight-black-f2", "e1", setOf("f2", "h1"))
    )

    @Test(dataProvider = "getGetPossibleTakingMovesData")
    fun testGetPossibleTakingMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)
        val king = game.getFigure(Position.byCode(posCode))
        val results = LinkedList<Move>()
        king.getPossibleTakingMoves(game, results)
        val actualMoveToCodes = results.toTargetPosAsStringSet()
        assertEquals(expectedMoveToCodes, actualMoveToCodes, "king can take on")
    }

    @DataProvider
    fun getGetPossibleTakingMovesData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-black-e8-0 King-white-e1-0 Rook-white-a1-0 Rook-white-h1-0 Knight-black-f2", "e1", setOf("f2"))
    )

    @Test
    fun testDidCastling() {
        val des = "white 0 Rook-white-a1-0 King-white-e1-0 " + "King-black-e8-0"
        val game = ChessGame(des)

        val king = game.getFigureOrNull(Position.byCode("e1")) as King
        assertFalse(king.didCastling)
        game.move(Move.byCode("e1-a1"))
        assertTrue(king.didCastling)
    }

    @Test
    fun testDoesShortCastling() {
        val game = ChessGame(518)
        with(game) {
            move(Move.byCode("g2-g3"))
            move(Move.byCode("g7-g6"))
            move(Move.byCode("f1-g2"))
            move(Move.byCode("f8-g7"))
            move(Move.byCode("g1-f3"))
            move(Move.byCode("g8-f6"))
        }

        assertTrue(game.isMovable(Position.byCode("e1"), Position.byCode("h1"), true), "isMovable: king can move to h1 (short castling)")
        val whiteKingMoves = game.getPossibleMovesFrom("e1")
        assertEquals(whiteKingMoves.size,2, "king can go to f1 and h1 (short castling), therefore getPossibleMoves#")
        assertTrue(whiteKingMoves.contains(Move.byCode("e1-h1")), "possible move e1-h1")
        game.move(Move.byCode("e1-h1"))
        assertTrue(game.isMovable(Position.byCode("e8"), Position.byCode("h8"), false), "isMovable: king can move to h8 (short castling)")
        val blackKingMoves = game.getPossibleMovesFrom("e8")
        assertEquals(blackKingMoves.size, 2, "king can go to f8 and h8 (short castling), therefore getPossibleMoves#")
        assertTrue(blackKingMoves.contains(Move.byCode("e8-h8")), "possible move e8-h8")
    }

    @Test
    fun testDoesLongCastling() {
        val game = ChessGame(518)
        game.apply {
            move(Move.byCode("e2-e4"))
            move(Move.byCode("e7-e5"))
            move(Move.byCode("d1-e2"))
            move(Move.byCode("d8-e7"))
            move(Move.byCode("b1-c3"))
            move(Move.byCode("b8-c6"))
            move(Move.byCode("b2-b3"))
            move(Move.byCode("b7-b6"))
            move(Move.byCode("c1-b2"))
            move(Move.byCode("c8-b7"))
        }

        assertTrue(game.isMovable(Position.byCode("e1"), Position.byCode("a1"), true), "isMovable: king can move to a1 (long castling)")
        val whiteKingMoves = game.getPossibleMovesFrom("e1")
        assertEquals(2, whiteKingMoves.size, "king can go to d1 and a1 (long castling), therefore getPossibleMoves#")
        assertTrue(whiteKingMoves.contains(Move.byCode("e1-a1")), "possible move e1-a1")
        game.move(Move.byCode("e1-a1"))
        assertTrue(game.isMovable(Position.byCode("e8"), Position.byCode("a8"), false), "isMovable: king can move to a8 (long castling)")
        val blackKingMoves = game.getPossibleMovesFrom("e8")
        assertEquals(2, blackKingMoves.size, "king can go to d8 and a8 (long castling), therefore getPossibleMoves#")
        assertTrue(blackKingMoves.contains(Move.byCode("e8-a8")), "possible move e8-a8")
    }

    @Test
    fun testImmediatelyCastlingInChess960Positions() {
        //check out https://de.wikipedia.org/wiki/Chess960#Castlingregeln

        //Rook on a1, king on b1 so b1-a1 should be possible as a first move
        var game = ChessGame(314)
        val c1 = Position.byCode("c1")
        val d1 = Position.byCode("d1")
        var isCastlingPossible = game.isMovable(d1, c1, true)
        assertTrue(isCastlingPossible, "castling should be possible")

        //Rook on a1, king on b1 so b1-a1 should be be possible as a first move
        game = ChessGame(759)
        val a1 = Position.byCode("a1")
        val b1 = Position.byCode("b1")
        isCastlingPossible = game.isMovable(b1, a1, true)
        assertFalse(isCastlingPossible, "castling should be impossible possible")
    }
}
