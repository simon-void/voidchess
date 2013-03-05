package player.ki.concurrent;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import helper.Move;
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
		final List<Move> possibleMoves = game.getPossibleMoves( );
		
		final int totalNumberOfMoves = possibleMoves.size();
		showProgress(0, totalNumberOfMoves);
		
		int movesDone           = 0;
		final SortedSet<EvaluatedMove> result = new TreeSet<EvaluatedMove>();
    for(Move move: possibleMoves) {
			float value = dynamicEvaluation.evaluateMove( game,move );
			result.add( new EvaluatedMove(move, value) );
			
			showProgress(++movesDone, totalNumberOfMoves);
		}
		
		return result;
	}
}
