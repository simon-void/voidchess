package player.ki.concurrent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import helper.Move;
import helper.MoveIterator;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;
import board.ChessGameInterface;

class MultiThreadStrategy extends AbstractConcurrencyStrategy
{	
	private final ExecutorService executorService;
	private final List<String> exceptions;
	
	private int numberOfEvaluatedMoves;
	private int totalNumberOfMoves;
	
	MultiThreadStrategy(AbstractComputerPlayerUI ui, int numberOfCores)
	{
		super(ui);
		
		executorService = Executors.newFixedThreadPool(numberOfCores);
		exceptions = new LinkedList<String>();
		numberOfEvaluatedMoves = 0;
	}
	

	@Override
	public SortedSet<EvaluatedMove> evaluatePossibleMoves(final ChessGameInterface game, final DynamicEvaluation dynamicEvaluation)
	{
		assert numberOfEvaluatedMoves == 0 : "method is invoked by several threads at the same time";
		
		final MoveIterator iter = game.getPossibleMoves( );
		
		totalNumberOfMoves = iter.totalNumberOfMoves();
		showProgress(0, totalNumberOfMoves);
		
		List<MoveEvaluationCallable> moveEvaluationCallables = new ArrayList<MoveEvaluationCallable>(totalNumberOfMoves);
		while (iter.hasMoreMoves()) {
			final Move move = iter.nextMove();
			
			moveEvaluationCallables.add(
					new MoveEvaluationCallable(
							game.copyGame(),
							move,
							dynamicEvaluation)
				);
		}
		
		try{
			List<Future<EvaluatedMove>> evaluatedMoves = executorService.invokeAll(moveEvaluationCallables);
			
			assertNoExceptionsWhileEvaluatingGame(game);

			SortedSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();
			for (Future<EvaluatedMove> evaluatedMoveFuture : evaluatedMoves) {				
				//get() is guaranteed not to block anymore by invokeAll(..)
				EvaluatedMove evaluatedMove = evaluatedMoveFuture.get();
				if(evaluatedMove!=null) {
					result.add(evaluatedMove);
				}
			}
			return result;
		}catch(Exception e) {
			//TODO better exception handling here
			throw new RuntimeException("MultiThreadStrategy.lock failed", e);
		}finally{
			numberOfEvaluatedMoves = 0;
			exceptions.clear();
		}
	}
	
	private synchronized void moveEvaluated()
	{
		numberOfEvaluatedMoves++;
		
		showProgress();
	}
	
	private synchronized void addException(Move move, Exception e)
	{		
		//TODO make better message
		exceptions.add(move.toString()+" caused "+e.toString());
		
		moveEvaluated();
	}
	
	private void showProgress()
	{
		showProgress(numberOfEvaluatedMoves, totalNumberOfMoves);
	}
	
	private void assertNoExceptionsWhileEvaluatingGame(ChessGameInterface game)
	{
		//TODO better exception handling (e.g. incorporate game as String)
		if( !exceptions.isEmpty() ) {
			StringBuilder sb = new StringBuilder();
			for(String msg: exceptions) {
				sb.append(msg).append("; \n");
			}
			
			throw new RuntimeException("exception(s) while computing moves: \n"+sb.toString());
		}
	}
	
	private class MoveEvaluationCallable implements Callable<EvaluatedMove>
	{
		private final DynamicEvaluation dynamicEvaluation;
		private final ChessGameInterface game;
		private final Move move; 
		
		MoveEvaluationCallable(final ChessGameInterface game, Move move, DynamicEvaluation dynamicEvaluation)
		{
			this.dynamicEvaluation = dynamicEvaluation;
			this.game = game;
			this.move = move;
		}

		public EvaluatedMove call() throws Exception {
			try {
				final float value = dynamicEvaluation.evaluateMove(game, move);
				EvaluatedMove evaluatedMove = new EvaluatedMove(move, value);
				// increase the progress bar
				moveEvaluated();

				return evaluatedMove;
			} catch (Exception e) {
				// increase the progress bar
				addException(move, e);
				return null;
			}
		}
	}
}
