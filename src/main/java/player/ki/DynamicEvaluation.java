package player.ki;

import board.ChessGameInterface;
import helper.ChessGameSupervisor;
import helper.Move;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stephan
 */
public class DynamicEvaluation {
    final private int possibleMovesBufferSize = 40;

    private StaticEvaluationInterface strategy;
    private SearchTreePruner pruner;

    DynamicEvaluation(SearchTreePruner pruner, StaticEvaluationInterface strategy) {
        setEvaluationStrategy(strategy);
        setSearchTreePruner(pruner);
    }

    DynamicEvaluation() {
        this(new SimplePruner(), new StaticEvaluation());
    }

    public Evaluaded evaluateMove(final ChessGameInterface game, final Move move) {
        int depth = 0;
        final boolean forWhite = game.isWhiteTurn();
        boolean thisMove_hasHitFigure = game.hasHitFigure();
        boolean thisMove_isChess = game.isCheck(!forWhite);

        final ChessGameSupervisor interactiveSupervisor = game.suspendInteractiveSupervisor();
        int endoption = game.move(move);

        Evaluaded result;
        if (endoption == ChessGameInterface.NO_END) {
            final List<Move> minPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);
            result = getMin(game, forWhite, depth, thisMove_isChess, thisMove_hasHitFigure, minPossibleMovesBuffer);
        } else if (endoption == ChessGameInterface.MATT) {
            result = Evaluaded.getThisComputerPlayerIsMatt(depth + 1);
        } else {
            result = Evaluaded.DRAW;
        }

        game.useSupervisor(interactiveSupervisor);
        game.undo();
        return result;
    }

    private Evaluaded getMin(final ChessGameInterface game,
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
            return strategy.evaluate(game, forWhite);
        }

        Evaluaded minValue = Evaluaded.INITAL;

        minPossibleMovesBuffer.clear();
        game.getPossibleMoves(minPossibleMovesBuffer);
        final List<Move> maxPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);

        for (Move move : minPossibleMovesBuffer) {
            Evaluaded tempValue;

            assert (game.isFreeArea(move.to) || !game.getFigure(move.to).isKing())
                    : "getMin:" +
                    game.getFigure(move.from).getClass().getName() +
                    " hits King white Move " +
                    move.toString();

            int endoption = game.move(move);

            if (endoption == ChessGameInterface.NO_END) {
                tempValue = getMax(game,
                        forWhite,
                        depth,
                        thisMove_isChess,
                        thisMove_hasHitFigure,
                        maxPossibleMovesBuffer);
            } else if (endoption == ChessGameInterface.MATT) {
                game.undo();
                return Evaluaded.getOtherPlayerIsMatt(depth + 1);
            } else {
                tempValue = Evaluaded.DRAW;
            }

            game.undo();

            if (minValue == Evaluaded.INITAL || minValue.compareTo(tempValue)>0) {
                minValue = tempValue;
            }
        }

        assert minValue != Evaluaded.INITAL
                : "no minimum found";

        return minValue;
    }

    private Evaluaded getMax(final ChessGameInterface game,
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
            return strategy.evaluate(game, forWhite);
        }

        Evaluaded maxValue = Evaluaded.INITAL;

        maxPossibleMovesBuffer.clear();
        game.getPossibleMoves(maxPossibleMovesBuffer);
        final List<Move> minPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);

        for (Move move : maxPossibleMovesBuffer) {
            Evaluaded tempValue;

            assert (game.isFreeArea(move.to) || !game.getFigure(move.to).isKing())
                    : "getMax:" +
                    game.getFigure(move.from).getClass().getName() +
                    " hits King white Move " +
                    move.toString();

            int endoption = game.move(move);

            if (endoption == ChessGameInterface.NO_END) {
                tempValue = getMin(game,
                        forWhite,
                        depth,
                        thisMove_isChess,
                        thisMove_hasHitFigure,
                        minPossibleMovesBuffer);
            } else if (endoption == ChessGameInterface.MATT) {
                game.undo();
                return Evaluaded.getThisComputerPlayerIsMatt(depth + 1);
            } else {
                tempValue = Evaluaded.DRAW;
            }

            game.undo();

            if (maxValue == Evaluaded.INITAL || tempValue.compareTo(maxValue)>0) {
                maxValue = tempValue;
            }
        }

        assert maxValue != Evaluaded.INITAL
                : "no maximum found";

        return maxValue;
    }

    public void setEvaluationStrategy(StaticEvaluationInterface strategy) {
        this.strategy = strategy;
    }

    public void setSearchTreePruner(SearchTreePruner pruner) {
        this.pruner = pruner;
    }
}
