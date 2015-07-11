package player.ki.concurrent;

import board.ChessGameInterface;
import helper.Move;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;

import java.util.*;
import java.util.concurrent.*;

class MultiThreadStrategy extends AbstractConcurrencyStrategy {
    //  final private DelayedExecutorService delayedExecutorService;
    private final ExecutorService executorService;
    private final int numberOfThreads;

    MultiThreadStrategy(AbstractComputerPlayerUI ui, int numberOfCores) {
        super(ui);

        numberOfThreads = numberOfCores;
        executorService = Executors.newFixedThreadPool(numberOfCores);
//		delayedExecutorService = new DelayedExecutorService(numberOfCores);
    }


    @Override
    public SortedSet<EvaluatedMove> evaluatePossibleMoves(final ChessGameInterface game, final DynamicEvaluation dynamicEvaluation) {
        //as long as the first parameter is 0 and the second one is bigger
        //the progress bar will always show correctly 0% (so '1' as second parameter is fine)
        showProgress(0, 1);

        final LinkedList<Callable<EvaluatedMove>> movesToEvaluate = getEvaluatableMoves(game, dynamicEvaluation);

        SortedSet<EvaluatedMove> result = Collections.emptySortedSet();
        try {
            result = evaluate(movesToEvaluate);//resultFutures.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert !result.isEmpty() : "no evaluation of a possible moves was successfull";

        return result;
    }

    private SortedSet<EvaluatedMove> evaluate(final LinkedList<Callable<EvaluatedMove>> movesToEvaluate)
            throws InterruptedException, ExecutionException {
        SortedSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();

        final int totalNumberOfMoves = movesToEvaluate.size();

        CompletionService<EvaluatedMove> ecs = new ExecutorCompletionService<EvaluatedMove>(executorService);
        submitCallables(movesToEvaluate, ecs, numberOfThreads);

        for (int i = 0; i < totalNumberOfMoves; ++i) {
            //wait for an evaluation to be finished
            EvaluatedMove evaluatedMove = ecs.take().get();
            //show the progress
            showProgress(i, totalNumberOfMoves);
            //add a new move to be evaluated to the queue (if some are left)
            submitCallables(movesToEvaluate, ecs, 1);
            //add this evaluation to result set
            if (evaluatedMove != null) {
                result.add(evaluatedMove);
            }
        }

        return result;
    }

    private LinkedList<Callable<EvaluatedMove>> getEvaluatableMoves(final ChessGameInterface game, final DynamicEvaluation dynamicEvaluation) {
        final List<Move> possibleMoves = getPossibleMoves(game);
        assert !possibleMoves.isEmpty() : "no moves were possible and therefore evaluatable";

        final int totalNumberOfMoves = possibleMoves.size();

        final Iterator<ChessGameInterface> gameInstances = game.copyGame(totalNumberOfMoves).iterator();

        final LinkedList<Callable<EvaluatedMove>> movesToEvaluate = new LinkedList<Callable<EvaluatedMove>>();
        for (Move move : possibleMoves) {
            movesToEvaluate.add(
                    new MoveEvaluationCallable(
                            gameInstances.next(), move, dynamicEvaluation//, delayedExecutorService
                    )
            );
        }

        return movesToEvaluate;
    }

    private void submitCallables(LinkedList<Callable<EvaluatedMove>> movesToEvaluate, CompletionService<EvaluatedMove> completionService, int numberOfMovesToSubmit) {
        for (int i = 0; i < numberOfMovesToSubmit; i++) {
            if (movesToEvaluate.isEmpty()) {
                break;
            }
            completionService.submit(movesToEvaluate.removeFirst());
        }
    }

    private static class MoveEvaluationCallable implements Callable<EvaluatedMove> {
        private final DynamicEvaluation dynamicEvaluation;
        private final ChessGameInterface game;
        private final Move move;

        MoveEvaluationCallable(ChessGameInterface game, Move move, DynamicEvaluation dynamicEvaluation) {
            this.dynamicEvaluation = dynamicEvaluation;
            this.game = game;
            this.move = move;
        }

        public EvaluatedMove call() throws Exception {
            try {
                final float value = dynamicEvaluation.evaluateMove(game, move);
                EvaluatedMove evaluatedMove = new EvaluatedMove(move, value);

                return evaluatedMove;
            } catch (Exception e) {
                //print out the error
                e.printStackTrace();
                return null;
            }
        }
    }

}
