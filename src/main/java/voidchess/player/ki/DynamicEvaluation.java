package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.helper.ChessGameSupervisor;
import voidchess.helper.Move;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedAsDraw;
import voidchess.player.ki.evaluation.EvaluatedAsMatt;
import voidchess.player.ki.evaluation.EvaluatedMove;

import java.util.*;

/**
 * @author stephan
 */
public class DynamicEvaluation {
    final private int possibleMovesBufferSize = 40;

    private StaticEvaluationInterface strategy;
    private SearchTreePruner pruner;

    public DynamicEvaluation(SearchTreePruner pruner, StaticEvaluationInterface strategy) {
        setEvaluationStrategy(strategy);
        setSearchTreePruner(pruner);
    }

    DynamicEvaluation() {
        this(new SimplePruner(), new StaticEvaluation());
    }

    public Evaluated evaluateMove(final ChessGameInterface game, final Move move) {
        int depth = 0;
        final boolean forWhite = game.isWhiteTurn();
        boolean thisMove_hasHitFigure = game.hasHitFigure();
        boolean thisMove_isChess = game.isCheck(!forWhite);

        final ChessGameSupervisor interactiveSupervisor = game.suspendInteractiveSupervisor();
        int endoption = game.move(move);

        Evaluated result;
        if (endoption == ChessGameInterface.Companion.getNO_END()) {
            final List<Move> minPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);
            result = getMin(game, forWhite, depth, thisMove_isChess, thisMove_hasHitFigure, minPossibleMovesBuffer);
        } else if (endoption == ChessGameInterface.Companion.getMATT()) {
            result = new EvaluatedAsMatt(depth + 1, true);
        } else {
            result = EvaluatedAsDraw.INSTANCE;
        }

        game.useSupervisor(interactiveSupervisor);
        game.undo();
        return result;
    }

    private Evaluated getMin(final ChessGameInterface game,
                             final boolean forWhite,
                             final int depth,
                             final boolean lastMove_isChess,
                             final boolean lastMove_hasHitFigure,
                             final List<Move> minPossibleMovesBuffer) {
        boolean thisMove_hasHitFigure = game.hasHitFigure();
        boolean thisMove_isChess = game.isCheck(!forWhite);

        if (pruner.stopMinDynamicEvaluation(depth,
                thisMove_isChess,
                thisMove_hasHitFigure,
                lastMove_isChess,
                lastMove_hasHitFigure)) {
            return strategy.getPrimaryEvaluation(game, forWhite);
        }

        minPossibleMovesBuffer.clear();
        game.getPossibleMoves(minPossibleMovesBuffer);
        final NavigableSet<EvaluatedMove> primaryEvaluations = new TreeSet<EvaluatedMove>();
        final List<Move> maxPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);

        for (Move move : minPossibleMovesBuffer) {
            Evaluated primaryEval;

            assert (game.isFreeArea(move.getTo()) || !game.getFigure(move.getTo()).isKing())
                    : "getMin:" +
                    game.getFigure(move.getFrom()).getClass().getName() +
                    " hits King white Move " +
                    move.toString();

            int endoption = game.move(move);

            if (endoption == ChessGameInterface.Companion.getNO_END()) {
                primaryEval = getMax(game,
                        forWhite,
                        depth,
                        thisMove_isChess,
                        thisMove_hasHitFigure,
                        maxPossibleMovesBuffer);
            } else if (endoption == ChessGameInterface.Companion.getMATT()) {
                game.undo();
                return new EvaluatedAsMatt(depth + 1, false);
            } else {
                primaryEval = EvaluatedAsDraw.INSTANCE;
            }

            primaryEvaluations.add(new EvaluatedMove(move, primaryEval));

            game.undo();
        }

        //now byCode a secondaryEvaluation for the Moves with the smallest primary Evaluation
        SortedSet<Evaluated> combinedEvaluations = new TreeSet<>();
        final Evaluated minPrimary = primaryEvaluations.first().getValue();

        for (EvaluatedMove evaluatedMove : primaryEvaluations) {
            final Evaluated primaryEval = evaluatedMove.getValue();
            if (primaryEval.isCloseToByPrimary(minPrimary)) {
                game.move(evaluatedMove.getMove());
                //add secondary Evaluation
                strategy.addSecondaryEvaluation(game, forWhite, primaryEval);
                combinedEvaluations.add(primaryEval);
                game.undo();
            } else {
                //the set is sorted so once an element is to big, all the rest are
                break;
            }
        }

        //minValue
        return combinedEvaluations.first();
    }

    private Evaluated getMax(final ChessGameInterface game,
                             final boolean forWhite,
                             int depth,
                             final boolean lastMove_isChess,
                             final boolean lastMove_hasHitFigure,
                             final List<Move> maxPossibleMovesBuffer) {
        depth++;

        boolean thisMove_hasHitFigure = game.hasHitFigure();
        boolean thisMove_isChess = game.isCheck(forWhite);
        if (pruner.stopMaxDynamicEvaluation(depth,
                thisMove_isChess,
                thisMove_hasHitFigure,
                lastMove_isChess,
                lastMove_hasHitFigure)) {
            return strategy.getPrimaryEvaluation(game, forWhite);
        }

        SortedSet<EvaluatedMove> movesWithPrimaryEvaluation = new TreeSet<>();

        maxPossibleMovesBuffer.clear();
        game.getPossibleMoves(maxPossibleMovesBuffer);
        final NavigableSet<EvaluatedMove> primaryEvaluations = new TreeSet<EvaluatedMove>();
        final List<Move> minPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);

        for (Move move : maxPossibleMovesBuffer) {
            Evaluated primaryEval;

            assert (game.isFreeArea(move.getTo()) || !game.getFigure(move.getTo()).isKing())
                    : "getMax:" +
                    game.getFigure(move.getFrom()).getClass().getName() +
                    " hits King white Move " +
                    move.toString();

            int endoption = game.move(move);

            if (endoption == ChessGameInterface.Companion.getNO_END()) {
                primaryEval = getMin(game,
                        forWhite,
                        depth,
                        thisMove_isChess,
                        thisMove_hasHitFigure,
                        minPossibleMovesBuffer);
            } else if (endoption == ChessGameInterface.Companion.getMATT()) {
                game.undo();
                return new EvaluatedAsMatt(depth + 1, true);
            } else {
                primaryEval = EvaluatedAsDraw.INSTANCE;
            }

            primaryEvaluations.add(new EvaluatedMove(move, primaryEval));

            game.undo();
        }

        //now byCode a secondaryEvaluation for the Moves with the smallest primary Evaluation
        SortedSet<Evaluated> combinedEvaluations = new TreeSet<>();
        final Evaluated maxPrimary = primaryEvaluations.last().getValue();

        for (EvaluatedMove evaluatedMove : primaryEvaluations.descendingSet()) {
            final Evaluated primaryEval = evaluatedMove.getValue();
            if (primaryEval.isCloseToByPrimary(maxPrimary)) {
                game.move(evaluatedMove.getMove());
                //add secondary Evaluation
                strategy.addSecondaryEvaluation(game, forWhite, primaryEval);
                combinedEvaluations.add(primaryEval);
                game.undo();
            } else {
                //the set is sorted in reverse so once an element is to small, all the rest are
                break;
            }
        }

        //maxValue
        return combinedEvaluations.last();
    }

    public void setEvaluationStrategy(StaticEvaluationInterface strategy) {
        this.strategy = strategy;
    }

    public void setSearchTreePruner(SearchTreePruner pruner) {
        this.pruner = pruner;
    }
}