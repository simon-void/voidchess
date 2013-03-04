package player.ki.concurrent;

import java.util.SortedSet;

import board.ChessGameInterface;
import player.ki.AbstractComputerPlayerUI;
import player.ki.DynamicEvaluation;

abstract class AbstractConcurrencyStrategy implements ConcurrencyStrategy
{
	final private AbstractComputerPlayerUI ui;
	
	public AbstractConcurrencyStrategy(AbstractComputerPlayerUI ui)
	{
		this.ui = ui;
	}
	
	/* (non-Javadoc)
	 * @see player.ki.concurrent.ConcurrencyStrategy#evaluatePossibleMoves(board.ChessGameInterface, player.ki.DynamicEvaluation)
	 */
	public abstract SortedSet<EvaluatedMove> evaluatePossibleMoves(final ChessGameInterface game, final DynamicEvaluation  dynamicEvaluation);
	
	protected void showProgress(int movesDone, final int totalNumberOfMoves)
	{
		ui.setProgress(movesDone,totalNumberOfMoves );
	}
}
