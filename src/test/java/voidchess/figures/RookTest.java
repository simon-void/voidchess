package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.move.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.board.move.Move;
import voidchess.board.move.Position;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;


public class RookTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Pawn-white-c2-false Rook-white-c4-2 "
                + "Knight-black-g4 King-white-e1-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);

        Position from = Position.byCode("c4");
        Position to1 = Position.byCode("c3");
        Position to2 = Position.byCode("g4");
        Position to3 = Position.byCode("c2");
        Position to4 = Position.byCode("h4");
        Position to5 = Position.byCode("d5");

        Rook rook = new Rook(true, from);
        assertTrue(rook.isReachable(to1, game));
        assertTrue(rook.isReachable(to2, game));
        assertFalse(rook.isReachable(to3, game));
        assertFalse(rook.isReachable(to4, game));
        assertFalse(rook.isReachable(to5, game));
        assertFalse(rook.isReachable(from, game));
    }

    @Test
    public void testUndoMove() {
        FigureFactory figureFactory = new FigureFactory();
        Rook rook = (Rook) figureFactory.getRook(Position.byCode("a1"), false);

        assertTrue(rook.canCastle());
        rook.figureMoved(Move.byCode("a1-b1"));
        assertFalse(rook.canCastle());
        rook.undoMove(Position.byCode("a1"));
        assertTrue(rook.canCastle());
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 King-white-e2-0 Pawn-black-b4-false "
                + "Rook-white-e4-2 King-black-e8-0";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure Rook = game.getFigureOrNull(Position.byCode("e4"));
        List<Move> moveIter = new LinkedList<>();
        Rook.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 11);
    }
}
