package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.board.ChessGameInterface;
import voidchess.board.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.helper.Move;
import voidchess.helper.Position;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class FigureTest {

    @Test
    public void testIsDifferentColor() {
        Figure figure1 = new Queen(true, Position.get(0, 0));
        Figure figure2 = new Rook(true, Position.get(0, 0));
        Figure figure3 = new Bishop(false, Position.get(0, 0));
        assertTrue(figure1.hasDifferentColor(figure3));
        assertFalse(figure2.hasDifferentColor(figure1));
        assertFalse(figure3.hasDifferentColor(figure3));
    }

    @Test
    public void testCanBeHitByEnpasent() {
        assertFalse(
                new Knight(
                        true,
                        Position.get(0, 0)
                ).canBeHitByEnpasent(),
                "should be false for all voidchess.figures but pawns"
        );
    }

    @Test
    public void testToString() {
        Position pos = Position.byCode("g4");
        Figure figure1 = new Bishop(true, pos);
        assertEquals("Bishop-white-g4", figure1.toString());
        Figure figure2 = new Bishop(false, pos);
        assertEquals("Bishop-black-g4", figure2.toString());
    }

    @Test
    public void testSubtypes() {
        Position pos = Position.byCode("g4");
        new Pawn(true, pos);
        new Rook(true, pos);
        new Knight(true, pos);
        new Bishop(true, pos);
        new Queen(true, pos);
        new King(true, pos);
    }

    @Test
    public void testIsWhite() {
        Position pos = Position.byCode("g4");

        Figure figure1 = new Rook(true, pos);
        assertTrue(figure1.isWhite());

        Figure figure2 = new Knight(false, pos);
        assertFalse(figure2.isWhite());
    }

    @Test
    public void testFigureMoved() {
        Position from = Position.byCode("c1");
        Position to = Position.byCode("g5");
        Position thirdpos = Position.byCode("f5");

        Figure figure1 = new Bishop(true, from);
        figure1.figureMoved(Move.get(from, to));
        assertEquals(figure1.toString(), "Bishop-white-g5");
        figure1.figureMoved(Move.get(thirdpos, from));
        assertEquals(figure1.toString(), "Bishop-white-g5");
    }

    @Test
    public void testCastling() {
        Position from = Position.byCode("c1");
        Position to = Position.byCode("g5");
        Move move = Move.get(from, to);

        assertFalse(new Pawn(true, from).canCastle());

        Figure king = new King(true, from);
        assertTrue(king.canCastle(), "unmoved king");
        king.figureMoved(move);
        assertFalse(king.canCastle(), "moved king");

        Figure Rook = new Rook(false, from);
        assertTrue(Rook.canCastle(), "unmoved Rook");
        Rook.figureMoved(move);
        assertFalse(Rook.canCastle(), "moved Rook");
    }

    @Test
    public void testIsPassivetBound() {
        String des = "white 0 King-white-e1-0 Rook-white-e3-4 Queen-black-e5";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from = Position.byCode("e3");
        Position to1 = Position.byCode("e2");
        Position to2 = Position.byCode("e4");
        Position to3 = Position.byCode("e5");
        Position to4 = Position.byCode("e6");
        Position to5 = Position.byCode("d3");

        Rook rook = new Rook(true, from);

        assertFalse(rook.isPassiveBound(to1, game));
        assertFalse(rook.isPassiveBound(to2, game));
        assertFalse(rook.isPassiveBound(to3, game));
        assertTrue(rook.isPassiveBound(to4, game));
        assertTrue(rook.isPassiveBound(to5, game));
    }

    @Test
    public void testIsBound() {
        String des = "white 0 King-white-e1-0 Rook-white-e3-4 Queen-black-e5";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from1 = Position.byCode("e3");
        Position from2 = Position.byCode("e1");
        Position to1 = Position.byCode("e2");
        Position to2 = Position.byCode("e4");
        Position to3 = Position.byCode("e5");
        Position to4 = Position.byCode("e6");
        Position to5 = Position.byCode("d3");
        Position to6 = Position.byCode("d2");
        Position to7 = Position.byCode("a1");

        Rook rook = new Rook(true, from1);
        King king = new King(true, from2);

        assertFalse(rook.isBound(to1, game));
        assertFalse(rook.isBound(to2, game));
        assertFalse(rook.isBound(to3, game));
        assertTrue(rook.isBound(to4, game));
        assertTrue(rook.isBound(to5, game));
        assertFalse(king.isBound(to1, game));
        assertFalse(king.isBound(to6, game));

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-e3-4 Bishop-black-c3 Queen-black-e5";
        game.init(des);

        assertTrue(rook.isBound(to1, game));
        assertTrue(rook.isBound(to2, game));
        assertTrue(rook.isBound(to3, game));
        assertTrue(rook.isBound(to4, game));
        assertTrue(rook.isBound(to5, game));
        assertFalse(king.isBound(to1, game));
        assertTrue(king.isBound(to6, game));
        assertTrue(king.isBound(to7, game));

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Bishop-black-e2";
        game.init(des);

        assertFalse(king.isBound(to1, game));
        assertFalse(king.isBound(to6, game));
        assertTrue(king.isBound(to7, game));
    }

    @Test
    public void testIsMoveable() {
        String des = "white 0 King-white-e1-0 Rook-white-h2-1 Queen-black-h4";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from = Position.byCode("h2");
        Position to1 = Position.byCode("f2");
        Position to2 = Position.byCode("h4");
        Position to3 = Position.byCode("g3");
        Position to4 = Position.byCode("e2");

        Rook rook = new Rook(true, from);

        assertTrue(rook.isMovable(to1, game));
        assertTrue(rook.isMovable(to2, game));
        assertFalse(rook.isMovable(to3, game));
        assertFalse(rook.isMovable(to4, game));
    }

    @Test
    public void testGetTypeInfo() {
        Position from1 = Position.byCode("e3");
        Position from2 = Position.byCode("e1");

        Pawn pawn1 = new Pawn(true, from1);
        Pawn pawn2 = new Pawn(true, from1);
        Pawn pawn3 = new Pawn(false, from1);
        Pawn pawn4 = new Pawn(true, from2);

        assertEquals(pawn1.getTypeInfo(), pawn2.getTypeInfo());    //gleiche Objekte sollten die gleiche TypeInfo haben
        assertNotEquals(pawn1.getTypeInfo(), pawn3.getTypeInfo()); //unterschiedliche TypeInfo bei unterschiedliche Farbe
        assertEquals(pawn1.getTypeInfo(), pawn4.getTypeInfo());    //Position geht nicht mit ein

        Rook rook = new Rook(true, from1);
        Knight knight = new Knight(true, from1);
        Bishop bishop = new Bishop(true, from1);
        Queen queen = new Queen(true, from1);
        King king = new King(true, from2);

        Integer pawnByte = pawn1.getTypeInfo();
        Integer RookByte = rook.getTypeInfo();
        Integer knightByte = knight.getTypeInfo();
        Integer bishopByte = bishop.getTypeInfo();
        Integer queenByte = queen.getTypeInfo();
        Integer kingByte = king.getTypeInfo();

        List<Integer> figureByteList = new LinkedList<>();
        //Die byte-Werte müssen paarweise disjunkt sein
        figureByteList.add(pawnByte);
        assertFalse(
                figureByteList.contains(RookByte),
                "Bytewert sollte noch nicht in der Liste sein"
        );
        figureByteList.add(RookByte);
        assertFalse(
                figureByteList.contains(knightByte),
                "Bytewert sollte noch nicht in der Liste sein"
        );
        figureByteList.add(knightByte);
        assertFalse(
                figureByteList.contains(bishopByte),
                "Bytewert sollte noch nicht in der Liste sein"
        );
        figureByteList.add(bishopByte);
        assertFalse(
                figureByteList.contains(queenByte),
                "Bytewert sollte noch nicht in der Liste sein"
        );
        figureByteList.add(queenByte);
        assertFalse(
                figureByteList.contains(kingByte),
                "Bytewert sollte noch nicht in der Liste sein"
        );
    }

    static List<Move> getPossibleMovesFrom(String posCode, ChessGameInterface game) {
        Position from = Position.byCode(posCode);
        List<Move> moveIter = new LinkedList<>();
        List<Move> allMoves = new LinkedList<>();
        game.getPossibleMoves(allMoves);
        for (Move move : allMoves) {
            if (move.from.equalsPosition(from)) {
                moveIter.add(move);
            }
        }
        return moveIter;
    }
}
