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
    public void testConstructor() {
        new MockFigure(true, Position.Companion.get(0, 0));
    }

    @Test
    public void testIsDifferentColor() {
        Figure figure1 = new MockFigure(true, Position.Companion.get(0, 0));
        Figure figure2 = new MockFigure(true, Position.Companion.get(0, 0));
        Figure figure3 = new MockFigure(false, Position.Companion.get(0, 0));
        assertTrue(figure1.hasDifferentColor(figure3));
        assertFalse(figure2.hasDifferentColor(figure1));
        assertFalse(figure3.hasDifferentColor(figure3));
    }

    @Test
    public void testCanBeHitByEnpasent() {
        assertFalse(
                new MockFigure(
                        true,
                        Position.Companion.get(0, 0)
                ).canBeHitByEnpasent(),
                "should be false for all voidchess.figures but pawns"
        );
    }

    @Test
    public void testToString() {
        Position pos = Position.Companion.byCode("g4");
        Figure figure1 = new MockFigure(true, pos);
        assertEquals("MockFigure-white-g4", figure1.toString());
        Figure figure2 = new Bishop(false, pos);
        assertEquals("Bishop-black-g4", figure2.toString());
    }

    @Test
    public void testSubtypes() {
        Position pos = Position.Companion.byCode("g4");
        new Pawn(true, pos);
        new Rock(true, pos);
        new Knight(true, pos);
        new Bishop(true, pos);
        new Queen(true, pos);
        new King(true, pos);
    }

    @Test
    public void testIsWhite() {
        Position pos = Position.Companion.byCode("g4");

        Figure figure1 = new Rock(true, pos);
        assertTrue(figure1.isWhite());

        Figure figure2 = new Knight(false, pos);
        assertFalse(figure2.isWhite());
    }

    @Test
    public void testFigureMoved() {
        Position from = Position.Companion.byCode("c1");
        Position to = Position.Companion.byCode("g5");
        Position thirdpos = Position.Companion.byCode("f5");

        Figure figure1 = new Bishop(true, from);
        figure1.figureMoved(Move.get(from, to));
        assertEquals(figure1.toString(), "Bishop-white-g5");
        figure1.figureMoved(Move.get(thirdpos, from));
        assertEquals(figure1.toString(), "Bishop-white-g5");
    }

    @Test
    public void testRochade() {
        Position from = Position.Companion.byCode("c1");
        Position to = Position.Companion.byCode("g5");
        Move move = Move.get(from, to);

        assertFalse(new Pawn(true, from).canParticipateInRochade());

        Figure king = new King(true, from);
        assertTrue(king.canParticipateInRochade(), "unmoved king");
        king.figureMoved(move);
        assertFalse(king.canParticipateInRochade(), "moved king");

        Figure rock = new Rock(false, from);
        assertTrue(rock.canParticipateInRochade(), "unmoved rock");
        rock.figureMoved(move);
        assertFalse(rock.canParticipateInRochade(), "moved rock");
    }

    @Test
    public void testIsPassivetBound() {
        String des = "white 0 King-white-e1-0 Rock-white-e3-4 Queen-black-e5";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from = Position.Companion.byCode("e3");
        Position to1 = Position.Companion.byCode("e2");
        Position to2 = Position.Companion.byCode("e4");
        Position to3 = Position.Companion.byCode("e5");
        Position to4 = Position.Companion.byCode("e6");
        Position to5 = Position.Companion.byCode("d3");

        Rock rock = new Rock(true, from);

        assertFalse(rock.isPassiveBound(to1, game));
        assertFalse(rock.isPassiveBound(to2, game));
        assertFalse(rock.isPassiveBound(to3, game));
        assertTrue(rock.isPassiveBound(to4, game));
        assertTrue(rock.isPassiveBound(to5, game));
    }

    @Test
    public void testIsBound() throws Exception {
        String des = "white 0 King-white-e1-0 Rock-white-e3-4 Queen-black-e5";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from1 = Position.Companion.byCode("e3");
        Position from2 = Position.Companion.byCode("e1");
        Position to1 = Position.Companion.byCode("e2");
        Position to2 = Position.Companion.byCode("e4");
        Position to3 = Position.Companion.byCode("e5");
        Position to4 = Position.Companion.byCode("e6");
        Position to5 = Position.Companion.byCode("d3");
        Position to6 = Position.Companion.byCode("d2");
        Position to7 = Position.Companion.byCode("a1");

        Rock rock = new Rock(true, from1);
        King king = new King(true, from2);

        assertFalse(rock.isBound(to1, game));
        assertFalse(rock.isBound(to2, game));
        assertFalse(rock.isBound(to3, game));
        assertTrue(rock.isBound(to4, game));
        assertTrue(rock.isBound(to5, game));
        assertFalse(king.isBound(to1, game));
        assertFalse(king.isBound(to6, game));

        des = "white 0 Rock-white-a1-0 King-white-e1-0 Rock-white-e3-4 Bishop-black-c3 Queen-black-e5";
        game.init(des);

        assertTrue(rock.isBound(to1, game));
        assertTrue(rock.isBound(to2, game));
        assertTrue(rock.isBound(to3, game));
        assertTrue(rock.isBound(to4, game));
        assertTrue(rock.isBound(to5, game));
        assertFalse(king.isBound(to1, game));
        assertTrue(king.isBound(to6, game));
        assertTrue(king.isBound(to7, game));

        des = "white 0 Rock-white-a1-0 King-white-e1-0 Bishop-black-e2";
        game.init(des);

        assertFalse(king.isBound(to1, game));
        assertFalse(king.isBound(to6, game));
        assertTrue(king.isBound(to7, game));
    }

    @Test
    public void testIsMoveable() {
        String des = "white 0 King-white-e1-0 Rock-white-h2-1 Queen-black-h4";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Position from = Position.Companion.byCode("h2");
        Position to1 = Position.Companion.byCode("f2");
        Position to2 = Position.Companion.byCode("h4");
        Position to3 = Position.Companion.byCode("g3");
        Position to4 = Position.Companion.byCode("e2");

        Rock rock = new Rock(true, from);

        assertTrue(rock.isMoveable(to1, game));
        assertTrue(rock.isMoveable(to2, game));
        assertFalse(rock.isMoveable(to3, game));
        assertFalse(rock.isMoveable(to4, game));
    }

    @Test
    public void testGetTypeInfo() {
        Position from1 = Position.Companion.byCode("e3");
        Position from2 = Position.Companion.byCode("e1");

        Pawn pawn1 = new Pawn(true, from1);
        Pawn pawn2 = new Pawn(true, from1);
        Pawn pawn3 = new Pawn(false, from1);
        Pawn pawn4 = new Pawn(true, from2);

        assertTrue(pawn1.getTypeInfo() == pawn2.getTypeInfo());    //gleiche Objekte sollten die gleiche TypeInfo haben
        assertTrue(pawn1.getTypeInfo() != pawn3.getTypeInfo()); //unterschiedliche TypeInfo bei unterschiedliche Farbe
        assertTrue(pawn1.getTypeInfo() == pawn4.getTypeInfo()); //Position geht nicht mit ein

        Rock rock = new Rock(true, from1);
        Knight knight = new Knight(true, from1);
        Bishop bishop = new Bishop(true, from1);
        Queen queen = new Queen(true, from1);
        King king = new King(true, from2);

        Byte pawnByte = new Byte(pawn1.getTypeInfo());
        Byte rockByte = new Byte(rock.getTypeInfo());
        Byte knightByte = new Byte(knight.getTypeInfo());
        Byte bishopByte = new Byte(bishop.getTypeInfo());
        Byte queenByte = new Byte(queen.getTypeInfo());
        Byte kingByte = new Byte(king.getTypeInfo());

        List<Byte> figureByteList = new LinkedList<>();
        //Die byte-Werte müssen paarweise disjunkt sein
        figureByteList.add(pawnByte);
        assertFalse(
                figureByteList.contains(rockByte),
                "Bytewert sollte noch nicht in der Liste sein"
        );
        figureByteList.add(rockByte);
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

    public static List<Move> getPossibleMovesFrom(ChessGameInterface game, Position from) {
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
