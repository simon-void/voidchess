package voidchess.figures

import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import voidchess.board.ChessGameInterface
import voidchess.board.getFigure
import voidchess.board.move.Move
import voidchess.board.move.Position
import voidchess.initSimpleChessBoard
import java.util.*


class FigureTest {

    @Test
    fun testIsDifferentColor() {
        val figure1 = Queen(true, Position[0, 0])
        val figure2 = Rook(true, Position[0, 0])
        val figure3 = Bishop(false, Position[0, 0])
        assertTrue(figure1.hasDifferentColor(figure3))
        assertFalse(figure2.hasDifferentColor(figure1))
        assertFalse(figure3.hasDifferentColor(figure3))
    }

    @Test
    fun testCanBeHitByEnpasent() {
        assertFalse(
                Knight(
                        true,
                        Position[0, 0]
                ).canBeHitByEnpasent(),
                "should be false for all voidchess.figures but pawns"
        )
    }

    @Test
    fun testToString() {
        val pos = Position.byCode("g4")
        val figure1 = Bishop(true, pos)
        assertEquals("Bishop-white-g4", figure1.toString())
        val figure2 = Bishop(false, pos)
        assertEquals("Bishop-black-g4", figure2.toString())
    }

    @Test
    fun testSubtypes() {
        val pos = Position.byCode("g4")
        Pawn(true, pos)
        Rook(true, pos)
        Knight(true, pos)
        Bishop(true, pos)
        Queen(true, pos)
        King(true, pos)
    }

    @Test
    fun testIsWhite() {
        val pos = Position.byCode("g4")

        val figure1 = Rook(true, pos)
        assertTrue(figure1.isWhite)

        val figure2 = Knight(false, pos)
        assertFalse(figure2.isWhite)
    }

    @Test
    fun testFigureMoved() {
        val from = Position.byCode("c1")
        val to = Position.byCode("g5")
        val thirdpos = Position.byCode("f5")

        val figure1 = Bishop(true, from)
        figure1.figureMoved(Move[from, to])
        assertEquals(figure1.toString(), "Bishop-white-g5")
        figure1.figureMoved(Move[thirdpos, from])
        assertEquals(figure1.toString(), "Bishop-white-g5")
    }

    @Test
    fun testCastling() {
        val from = Position.byCode("c1")
        val to = Position.byCode("g5")
        val move = Move[from, to]

        assertFalse(Pawn(true, from).canCastle())

        val king = King(true, from)
        assertTrue(king.canCastle(), "unmoved king")
        king.figureMoved(move)
        assertFalse(king.canCastle(), "moved king")

        val rook = Rook(false, from)
        assertTrue(rook.canCastle(), "unmoved Rook")
        rook.figureMoved(move)
        assertFalse(rook.canCastle(), "moved Rook")
    }

    @Test(dataProvider = "getIsBoundData")
    fun testIsBound(gameDes: String, figurePos: String, isNotBoundPosCodes: List<String>, isBoundPosCodes: List<String>) {
        val game = initSimpleChessBoard(gameDes)
        val figure = game.getFigure(Position.byCode(figurePos))
        for(isNotBoundPosCode in isNotBoundPosCodes) {
            val isNotBoundPos = Position.byCode(isNotBoundPosCode)
            assertFalse(figure.isBound(isNotBoundPos, game), "$figurePos.isBound($isNotBoundPos, game)")
        }
        for(isBoundPosCode in isBoundPosCodes) {
            val isBoundPos = Position.byCode(isBoundPosCode)
            assertTrue(figure.isBound(isBoundPos, game), "$figurePos.isBound($isBoundPos, game)")
        }
    }

    @DataProvider
    fun getIsBoundData(): Array<Array<Any>> = arrayOf(
            arrayOf("white 0 King-white-e1-0 Rook-white-e3-4 Queen-black-e5 King-black-e8-0", "e3", listOf("e2", "e4", "e5"), listOf("f3", "d3", "c3")),
            arrayOf("white 0 King-white-e1-0 Rook-white-e3-4 Queen-black-e5 King-black-e8-0", "e1", listOf("d1", "d2", "e2", "f2", "f1"), listOf<String>()),
            arrayOf("white 0 King-white-e1-0 Rook-white-e3-4 Bishop-black-c3 King-black-e8-0", "e3", listOf("c3"), listOf("d3", "f3", "e2", "e4", "e5")),
            arrayOf("white 0 King-white-e1-0 Rook-white-d3-4 Bishop-black-b4 King-black-e8-0", "d3", listOf("c3", "d2"), listOf("e3", "d4", "b3", "d1")),
            arrayOf("white 0 King-white-e1-0 Knight-white-a2 Bishop-black-b4 King-black-e8-0", "a2", listOf("c3", "b4"), listOf("c1")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-e3-4 Bishop-black-c3 Queen-black-e5 King-black-e8-0", "e3", listOf<String>(), listOf("c3", "d3", "f3", "e2", "e4", "e5")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-e3-4 Bishop-black-c3 Queen-black-e5 King-black-e8-0", "e1", listOf("d1", "e2", "f2", "f1"), listOf("a1", "d2")),
            arrayOf("white 0 Rook-white-a1-0 King-white-e1-0 Bishop-black-e2 King-black-e8-0", "e1", listOf("d2", "e2", "f2"), listOf("a1", "d1", "f1"))
    )

    @Test(dependsOnMethods = ["testIsBound"])
    fun testIsMovable() {
        val des = "white 0 King-white-e1-0 Rook-white-h2-1 Queen-black-h4 King-black-e8-0"
        val game = initSimpleChessBoard(des)

        val from = Position.byCode("h2")
        val to1 = Position.byCode("f2")
        val to2 = Position.byCode("h4")
        val to3 = Position.byCode("g3")
        val to4 = Position.byCode("e2")

        val rook = Rook(true, from)

        assertTrue(rook.isMovable(to1, game))
        assertTrue(rook.isMovable(to2, game))
        assertFalse(rook.isMovable(to3, game))
        assertFalse(rook.isMovable(to4, game))
    }

    @Test
    fun testGetTypeInfo() {
        val from1 = Position.byCode("e3")
        val from2 = Position.byCode("e1")

        val pawn1 = Pawn(true, from1)
        val pawn2 = Pawn(true, from1)
        val pawn3 = Pawn(false, from1)
        val pawn4 = Pawn(true, from2)

        assertEquals(pawn1.typeInfo, pawn2.typeInfo)    //gleiche Objekte sollten die gleiche TypeInfo haben
        assertNotEquals(pawn1.typeInfo, pawn3.typeInfo) //unterschiedliche TypeInfo bei unterschiedliche Farbe
        assertEquals(pawn1.typeInfo, pawn4.typeInfo)    //Position geht nicht mit ein

        val rook = Rook(true, from1)
        val knight = Knight(true, from1)
        val bishop = Bishop(true, from1)
        val queen = Queen(true, from1)
        val king = King(true, from2)

        val pawnByte = pawn1.typeInfo
        val rookByte = rook.typeInfo
        val knightByte = knight.typeInfo
        val bishopByte = bishop.typeInfo
        val queenByte = queen.typeInfo
        val kingByte = king.typeInfo

        val figureByteList = LinkedList<Int>()
        //Die byte-Werte müssen paarweise disjunkt sein
        figureByteList.add(pawnByte)
        assertFalse(
                figureByteList.contains(rookByte),
                "Bytewert sollte noch nicht in der Liste sein"
        )
        figureByteList.add(rookByte)
        assertFalse(
                figureByteList.contains(knightByte),
                "Bytewert sollte noch nicht in der Liste sein"
        )
        figureByteList.add(knightByte)
        assertFalse(
                figureByteList.contains(bishopByte),
                "Bytewert sollte noch nicht in der Liste sein"
        )
        figureByteList.add(bishopByte)
        assertFalse(
                figureByteList.contains(queenByte),
                "Bytewert sollte noch nicht in der Liste sein"
        )
        figureByteList.add(queenByte)
        assertFalse(
                figureByteList.contains(kingByte),
                "Bytewert sollte noch nicht in der Liste sein"
        )
    }
}
