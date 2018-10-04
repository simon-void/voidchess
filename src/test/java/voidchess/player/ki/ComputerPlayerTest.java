package voidchess.player.ki;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.helper.ChessGameSupervisorDummy;
import voidchess.helper.Move;
import voidchess.helper.RuntimeFacade;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author stephan
 */
public class ComputerPlayerTest {
    @Test
    public void testPlay() {
        String des = "black 0 King-white-g1-2 "
                + "Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false "
                + "Pawn-white-e4-false "
                + "Bishop-black-b7 "
                + "King-black-g8-2";

        ChessGame game = new ChessGame(des);
        game.move(Move.byCode("e3-f2"));
        testTermination(game);

        game = new ChessGame(ChessGameSupervisorDummy.INSTANCE);
        game.move(Move.byCode("c2-c3"));
        testTermination(game);

        des = "black 0 King-white-h1-3 Pawn-white-c7-false "
                + "King-black-b7-3-false";

        game = new ChessGame(des);
        testTermination(game);

        des = "black 0 King-white-h1-3 Pawn-white-c7-false "
                + "Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
                + "Knight-black-a7 King-black-b7-3-false";

        game = new ChessGame(des);
        testTermination(game);

        game = new ChessGame(621);
        game.move(Move.byCode("f2-f3"));
        testTermination(game);

        des = "black 0 Bishop-white-b1 King-white-h1-3 "
                + "Pawn-black-f7-false King-black-e8-0 Rook-black-f8-0";

        game = new ChessGame(des);
        testTermination(game);

        des = "white 0 Rook-black-e1-8 "
                + "Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 "
                + "Knight-white-g4 Queen-black-e5 King-black-g7-3";

        game = new ChessGame(des);
        testTermination(game);

        game = new ChessGame(314);
        testTermination(game);

        game = new ChessGame(621);
        game.move(Move.byCode("g2-g3"));
        game.move(Move.byCode("f7-f6"));
        game.move(Move.byCode("c2-c3"));
        game.move(Move.byCode("g8-b3"));
        game.move(Move.byCode("d1-c2"));
        testTermination(game);

        game = new ChessGame(707);
        game.move(Move.byCode("e1-f3"));
        game.move(Move.byCode("b7-b6"));
        game.move(Move.byCode("f1-e3"));
        game.move(Move.byCode("g7-g6"));
        testTermination(game);
    }

    @Test
    public void testInvarianz() {
        String des = "black 0 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false "
                + "Pawn-black-b7-false King-black-e8-0 Rook-black-h8-3";

        ChessGame game = new ChessGame(des);
        game.move(Move.byCode("b7-b5"));
        testTermination(game);
        String newDes = "white 1 Rook-white-a1-0 King-white-e1-0 Pawn-white-a5-false "
                + "Pawn-black-b5-true King-black-e8-0 Rook-black-h8-3";
        assertEquals(game.toString(), newDes);
    }

    private void testTermination(ChessGame game) {
        testTermination(game, new SimplePruner(1, 2, 2), new StaticEvaluation());
    }

    private void testTermination(ChessGame game, SearchTreePruner pruner, StaticEvaluationInterface staticEvaluation) {
        final NumberFormat numberFormat = NumberFormat.getPercentInstance();
        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation(pruner, staticEvaluation);

        List<Move> possibleMoves = new LinkedList<Move>();
        game.getPossibleMoves(possibleMoves);
        final double numberOfPossibleMoves = possibleMoves.size();
        int moveIndex = 0;
        try {
            for (Move move : possibleMoves) {
                dynamicEvaluation.evaluateMove(game, move);
                System.out.println(numberFormat.format((++moveIndex) / numberOfPossibleMoves));
            }
        } catch (Exception e) {
            String gamestring = game.toString();
            throw new RuntimeException(
                    e.toString() + "-after Moves:"
                            + game.getHistory() + " -leading to position:"
                            + gamestring);
        } catch (AssertionError e) {
            AssertionError extendedE = new AssertionError(e.getMessage() + " History:" + game.getHistory());
            extendedE.setStackTrace(e.getStackTrace());
            throw extendedE;
        }
    }

    private void testTermination(ChessGame game, SearchTreePruner pruner, Move move) {
        final String initDescription = game.toString();
        DynamicEvaluation dynamicEvaluation = new DynamicEvaluation(pruner, new StaticEvaluation());

        List<Move> possibleMoves = new LinkedList<Move>();
        game.getPossibleMoves(possibleMoves);

        if (!possibleMoves.contains(move)) {
            throw new RuntimeException("suggested move:" + move.toString()
                    + "not possible in initial position: " + initDescription);
        }

        try {
            dynamicEvaluation.evaluateMove(game, move);
            // Invariante: evaluateMove darf game nicht ändern
            String msg = "after Move:" + move.toString() + " History:" + game.getHistory();
            assertEquals(game.toString(), initDescription, msg);
        } catch (Exception e) {
            String gameToString = game.toString();
            throw new RuntimeException(e.toString() + "-after Moves:" + game.getHistory() + " -leading to position:"
                    + gameToString);
        } catch (AssertionError e) {
            AssertionError extendedE = new AssertionError(e.getMessage() + " History:" + game.getHistory());
            extendedE.setStackTrace(e.getStackTrace());
            throw extendedE;
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("benchmark")) {
            benchmark();
        } else {
            loadTest();
        }
    }

    private static void benchmark() {
        ChessGame game = new ChessGame();
        game.move(Move.byCode("e2-e4"));
        game.move(Move.byCode("e7-e5"));
        game.move(Move.byCode("g1-f3"));
        game.move(Move.byCode("b8-c6"));
        game.move(Move.byCode("f1-b5"));
        game.move(Move.byCode("f8-c5"));
//    game.move(Move.byCode("d2-d3"));
//    game.move(Move.byCode("d7-d6"));
//    game.move(Move.byCode("b1-c3"));
//    game.move(Move.byCode("c8-g4"));
        SearchTreePruner pruner = new SimplePruner(2, 3, 2);
        StaticEvaluationInterface staticEvaluation = new StaticEvaluation();//new ConstantEvaluation();//
        loadTest(game, pruner, staticEvaluation, "Benchmark");
    }

    private static void loadTest() {
        // Loadtest
        System.out.println("Loadtest: Start");

        // Grundaufstellung ohne Bauern
        String des = "white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0";
        loadTest( des );

        // Grundaufstellung mit Bauern vor König und ohne Läufer
        des = "white 0 Rook-white-a1-0 Knight-white-b1 "
                + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false " + "Rook-black-a8-0 Knight-black-b8 "
                + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0";
        // loadTest( des );

        // Grundaufstellung mit Bauern vor König und ohne Königsläufer
        des = "white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 Knight-white-g1 Rook-white-h1-0 "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 Knight-black-g8 Rook-black-h8-0";
        // loadTest( des );

        // Grundaufstellung mit Bauern vor König
        des = "white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0";
        // loadTest( des );

        // Grundaufstellung mit Bauern vor König und ohne Damen
        des = "white 0 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rook-black-h8-0";
        // loadTest( des );

        // Zeit von
        // Grundaufstellung mit Bauern vor König und ohne Königsläufer
        // ist in etwa so groß wie
        // Grundaufstellung mit Bauern vor König und ohne Dame!!! Warum?
        System.exit(0);
    }

    private static void loadTest(String des) {
        ChessGame game = new ChessGame(des);
        SearchTreePruner pruner = new SimplePruner(2, 3, 2);
        StaticEvaluationInterface staticEvaluation = new StaticEvaluation();
        loadTest(game, pruner, staticEvaluation, "Loadtest");
    }

    private static void loadTest(ChessGame game, SearchTreePruner pruner, StaticEvaluationInterface staticEvaluation, String type) {
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        ComputerPlayerTest computerPlayer = new ComputerPlayerTest();

        try {
            System.out.println(type + ": Berechnung gestartet");
            long time = System.currentTimeMillis();
            computerPlayer.testTermination(game, pruner, staticEvaluation);
            System.out.println("Dauer: " + decimalFormat.format((System.currentTimeMillis() - time) / 1000.0) + "s");
            RuntimeFacade.INSTANCE.printMemoryUsage("Speicherverbrauch used/total");
        } catch (RuntimeException e) {
            System.out.println(type + "fehler:" + e.getMessage());
        } finally {
            System.out.println(type + ": Ende");
        }
    }

}
