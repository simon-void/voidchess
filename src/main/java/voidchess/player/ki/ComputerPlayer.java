package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.helper.Position;
import voidchess.helper.RuntimeFacade;
import voidchess.ui.TableInterface;
import voidchess.player.PlayerInterface;
import voidchess.player.ki.concurrent.ConcurrencyStrategy;
import voidchess.player.ki.concurrent.ConcurrencyStrategyFactory;
import voidchess.player.ki.concurrent.EvaluatedMove;

import java.util.Iterator;
import java.util.SortedSet;

/**
 * @author stephan
 */
public class ComputerPlayer
        implements PlayerInterface {
    private ChessGameInterface game;
    private TableInterface table;
    private AbstractComputerPlayerUI ui;
    private DynamicEvaluation dynamicEvaluation;
    private SearchTreePruner standardPruner;
    private StaticEvaluationInterface standardEvaluation;
    private ConcurrencyStrategy concurrencyStrategy;
    private boolean usesStandardEvaluation;

    public ComputerPlayer(TableInterface table, ChessGameInterface game, AbstractComputerPlayerUI ui) {
        this.ui = ui;
        this.game = game;
        this.table = table;

        standardPruner = new SimplePruner(1, 2, 2);
        standardEvaluation = new StaticEvaluation();
        dynamicEvaluation = new DynamicEvaluation(standardPruner, standardEvaluation);
        concurrencyStrategy = ConcurrencyStrategyFactory.getConcurrencyStrategy(ui, 1);
        initEvaluation();
    }

    public void play() {
        //the game starts with a static game evaluation that considers many things
        //but in the endgame, when only the king of one side can move than the only important thing is
        //how many possible moves the king has left. So the Evaluation strategy has to be changed.
        pickStaticSpaceEvaluationIfNeccessary();

//      TODO  real Timer
//		long time = System.currentTimeMillis();

        SortedSet<EvaluatedMove> sortedEvaluatedMoves = concurrencyStrategy.evaluatePossibleMoves(game, dynamicEvaluation);

//		final int calls = StaticEvaluation.getCallCounter();
//		final int totalNumberOfMoves = evaluation.totalNumberOfMoves();
//		long duration = System.currentTimeMillis()-time;
//		double timePerMove = duration/(double)totalNumberOfMoves;
//		double timePerCall = duration/(double)calls;
//		System.out.println( "ComputerPlayer.play()" );
//		System.out.println( "moves:       "+totalNumberOfMoves );
//		System.out.println( "staticEval:  "+calls );
//		System.out.println( "duration:    "+(duration)+"ms" );
//		System.out.println( "timePerMove: "+(timePerMove)+"ms" );
//		System.out.println( "timePerCall: "+(timePerCall)+"ms" );
//		System.out.println();

        EvaluatedMove chosenMove = getMoveToPlay(sortedEvaluatedMoves);

        ui.setValue(chosenMove.getValue(), chosenMove.getMove());
        table.move(chosenMove.getMove());

        RuntimeFacade.collectGarbage();
    }

    /**
     * pick the move to make given an evaluated Set of possible moves.
     * (Taking always the best move would lead to a limited number of openings!)
     *
     * @param sortedEvaluatedMoves (set.first is the best move for the ki, set.last the worst)
     * @return the move the ki will make next
     */
    private EvaluatedMove getMoveToPlay(SortedSet<EvaluatedMove> sortedEvaluatedMoves) {
        Iterator<EvaluatedMove> evaluation = sortedEvaluatedMoves.iterator();
        EvaluatedMove bestMove = evaluation.next();

        //as long as the top moves are almost equally good, pick randomly one (with a higher chance for the better move)
        EvaluatedMove chosenMove = bestMove;
        while (evaluation.hasNext()) {
            if (Math.random() < 0.6) break;
            EvaluatedMove tempMove = evaluation.next();
            if (tempMove.getValue().hasAlmostSameValue( bestMove.getValue())) {
                chosenMove = tempMove;
            } else {
                break;
            }
        }
        return chosenMove;
    }

    /**
     * the game starts with a static game evaluation that considers many things
     * but in the endgame, when only the king of one side can move than the
     * only important thing is how many possible moves the king has left.
     * So the Evaluation strategy has to be changed.
     */
    private void pickStaticSpaceEvaluationIfNeccessary() {
        if (usesStandardEvaluation && StaticSpaceEvaluation.shouldUseStaticSpaceEvaluation(game)) {
            //wird genau einmal pro Spiel ausgef�hrt
            dynamicEvaluation.setEvaluationStrategy(new StaticSpaceEvaluation());
            dynamicEvaluation.setSearchTreePruner(new FullMovePruner(2, 2, 2));
            usesStandardEvaluation = false;
        }
    }

    public String askForPawnChange(Position pawnPosition) {
        return "Queen";
    }

    //am Anfang jedes Spiels wird die EvaluationStrategie zur�ckgesetzt vom potentiel StaticSpaceEvaluation
    public void setIsPlaying(boolean isPlaying) {
        if (isPlaying) initEvaluation();
    }

    //setzt die Default-EvaluationStrategie
    private void initEvaluation() {
        dynamicEvaluation.setEvaluationStrategy(standardEvaluation);
        dynamicEvaluation.setSearchTreePruner(standardPruner);
        usesStandardEvaluation = true;
    }

    //die Funktion wird nur von HumanPlayer ben�tigt (bis jetzt)
    public void setColor(boolean isWhite) {
    }

    public void setSearchTreePruner(SearchTreePruner pruner) {
        standardPruner = pruner;
        dynamicEvaluation.setSearchTreePruner(pruner);
    }

    public void setNumberOfCoresToUse(int numberOfCoresToUse) {
        concurrencyStrategy = ConcurrencyStrategyFactory.getConcurrencyStrategy(ui, numberOfCoresToUse);
    }
}
