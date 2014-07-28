package player.ki.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import helper.Move;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;
import board.ChessGameInterface;

class MultiThreadStrategy extends AbstractConcurrencyStrategy
{	
	private final ExecutorService executorService;
	
	MultiThreadStrategy(AbstractComputerPlayerUI ui, int numberOfCores)
	{
		super(ui);
		
		executorService = Executors.newFixedThreadPool(numberOfCores);
	}
	

	@Override
	public SortedSet<EvaluatedMove> evaluatePossibleMoves(final ChessGameInterface game, final DynamicEvaluation dynamicEvaluation)
	{
	  //as long as the first parameter is 0 and the second one is bigger
	  //the progress bar will always show correctly 0% (so '1' as second parameter is fine)
	  showProgress(0, 1);
	  
		final List<Move> possibleMoves = getPossibleMoves(game);
		assert !possibleMoves.isEmpty() : "no moves were possible and therefore evaluatable";
		
		final int totalNumberOfMoves = possibleMoves.size();
		final AtomicInteger numberOfFinnishedEvaluations = new AtomicInteger(0);
		
		final List<Future<EvaluatedMove>> evaluationFutures = new ArrayList<Future<EvaluatedMove>>(totalNumberOfMoves);
		final Iterator<ChessGameInterface> gameInstances = game.copyGame(totalNumberOfMoves).iterator();
		
		long time = System.currentTimeMillis();
		
		for(Move move: possibleMoves) {
		  evaluationFutures.add(
		    executorService.submit(
		      new MoveEvaluationCallable(
		          gameInstances.next(),	move, dynamicEvaluation, numberOfFinnishedEvaluations
		      )
		    )
		  );
		}
		
		blockAndShowProgressUntillEvaluationIsDone(numberOfFinnishedEvaluations, totalNumberOfMoves);
		
    final SortedSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();
    for (Future<EvaluatedMove> evaluatedMoveFuture : evaluationFutures) {
      try {
        //get() shouldn't need to block but it shouldn't be a problem is if it does
        final EvaluatedMove evaluatedMove = evaluatedMoveFuture.get();
        if(evaluatedMove==null) {
          //only possible in case of exception
          continue;
        }
        result.add(evaluatedMove);
      } catch (Exception e) {/*shouldn't happen*/}
    }
    
    long duration = time - System.currentTimeMillis();
    System.out.println("time:"+duration);
    
    assert !result.isEmpty() : "no evaluation of a possible moves was successfull";
    
    return result;
	}
	
	private void blockAndShowProgressUntillEvaluationIsDone(final AtomicInteger numberOfFinnishedEvaluations, final int totalNumberOfMoves)
	{
	  int lastNumberOfFinnishedEvaluations = 0;
	  boolean notAllEvaluationsDone = true;
	  
	  while(notAllEvaluationsDone) {
	    //sleep for a second
	    sleep(1000);
	    //than check the number of finnished Evaluations 
	    
	    final int nowNumberOfFinnishedEvaluations = numberOfFinnishedEvaluations.get();
	    if(nowNumberOfFinnishedEvaluations>lastNumberOfFinnishedEvaluations) {
	      //more evaluations have been finnished
	      //so update the lastNumberOfFinnishedEvaluations for the next iteration 
	      lastNumberOfFinnishedEvaluations = nowNumberOfFinnishedEvaluations;
	      //and update the progress bar
	      showProgress(nowNumberOfFinnishedEvaluations, totalNumberOfMoves);
	    }
	    //check if all evaluations are done
	    notAllEvaluationsDone = nowNumberOfFinnishedEvaluations<totalNumberOfMoves; 
	  }
	}
	
	private void sleep(final int numberOfMilliseconds)
	{
	  try{
	    Thread.sleep(numberOfMilliseconds);
	  } catch(InterruptedException e) {}
	}
	
//	private synchronized void moveEvaluated()
//	{
//		numberOfEvaluatedMoves++;
//		
//		showProgress();
//	}
//	
//	private synchronized void addException(Move move, Exception e)
//	{		
//		//TODO make better message
//		exceptions.add(move.toString()+" caused "+e.toString());
//		
//		moveEvaluated();
//	}
//	
//	private void showProgress()
//	{
//		showProgress(numberOfEvaluatedMoves, totalNumberOfMoves);
//	}
//	
//	private void assertNoExceptionsWhileEvaluatingGame(ChessGameInterface game)
//	{
//		//TODO better exception handling (e.g. incorporate game as String)
//		if( !exceptions.isEmpty() ) {
//			StringBuilder sb = new StringBuilder();
//			for(String msg: exceptions) {
//				sb.append(msg).append("; \n");
//			}
//			
//			throw new RuntimeException("exception(s) while computing moves: \n"+sb.toString());
//		}
//	}
	
	private static class MoveEvaluationCallable implements Callable<EvaluatedMove>
	{
	  final AtomicInteger numberOfFinnishedEvaluations;
		private final DynamicEvaluation dynamicEvaluation;
		private final ChessGameInterface game;
		private final Move move; 
		
		MoveEvaluationCallable(ChessGameInterface game, Move move, DynamicEvaluation dynamicEvaluation, final AtomicInteger numberOfFinnishedEvaluations)
		{
		  this.numberOfFinnishedEvaluations = numberOfFinnishedEvaluations;
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
			  e.printStackTrace(System.err);
			  return null;
			}finally{
			  //this Callable is finished so increment this counter
			  //so that the progress can be examined
				numberOfFinnishedEvaluations.incrementAndGet();
			}
		}
	}
}
