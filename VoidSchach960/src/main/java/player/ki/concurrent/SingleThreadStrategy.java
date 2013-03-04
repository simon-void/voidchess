package player.ki.concurrent;

import java.util.SortedSet;
import java.util.TreeSet;

import helper.Move;
import helper.MoveIterator;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;
import board.ChessGameInterface;

class SingleThreadStrategy extends AbstractConcurrencyStrategy
{
	SingleThreadStrategy(AbstractComputerPlayerUI ui)
	{
		super(ui);
	}
	
	@Override
	public SortedSet<EvaluatedMove> evaluatePossibleMoves(ChessGameInterface game, DynamicEvaluation dynamicEvaluation)
	{
		final MoveIterator iter = game.getPossibleMoves( );
		
		final int totalNumberOfMoves = iter.totalNumberOfMoves();
		showProgress(0, totalNumberOfMoves);
		
		int movesDone           = 0;
		SortedSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();
		while( iter.hasMoreMoves() ) {
			Move move = iter.nextMove();
			float value = dynamicEvaluation.evaluateMove( game,move );
			result.add( new EvaluatedMove(move, value) );
			
			showProgress(++movesDone, totalNumberOfMoves);
		}
		
		return result;
	}
}
