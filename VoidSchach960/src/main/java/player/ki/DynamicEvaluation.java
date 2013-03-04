

package player.ki;

import board.*;
import helper.*;

/**
 * @author stephan
 */
public class DynamicEvaluation
{
	private StaticEvaluationInterface strategy;
	private SearchTreePruner   pruner;
	private ChessValue valuewrapper;

	DynamicEvaluation( SearchTreePruner pruner,StaticEvaluationInterface strategy )
	{
		valuewrapper = ChessValue.getInstance();
		setEvaluationStrategy( strategy );
		setSearchTreePruner( pruner );
	}
	
	DynamicEvaluation()
	{
		this( new SimplePruner(),new StaticEvaluation() );
	}

	public float evaluateMove( final ChessGameInterface game,final Move move )
	{
		int depth = 0;
		final boolean forWhite = game.isWhiteTurn();
		boolean thisMove_hasHitFigure = game.hasHitFigure();
		boolean thisMove_isChess = game.isCheck( !forWhite );

		game.useDummySupervisor();
		int endoption = game.move( move );

		float result;
		if( endoption==ChessGameInterface.NO_END ){
			result = getMin( game,forWhite,depth,thisMove_isChess,thisMove_hasHitFigure );
		}else if( endoption==ChessGameInterface.MATT ){
			result = valuewrapper.getThisComputerPlayerIsMatt( depth+1 );
		}else{
			result = valuewrapper.getDrawValue();
		}

		game.useNormalSupervisor();
		game.undo();
		return result;
	}

	private float getMin( final ChessGameInterface game,
	                      final boolean forWhite,
	                      final int depth,
	                      final boolean lastMove_isChess,
	                      final boolean lastMove_hasHitFigure )
	{
		boolean thisMove_hasHitFigure = game.hasHitFigure();
		boolean thisMove_isChess = game.isCheck( !forWhite );

		if( pruner.stopMinDynamicEvaluation(	depth,
												thisMove_isChess,
												thisMove_hasHitFigure,
												lastMove_isChess,
												lastMove_hasHitFigure ) ) {
			return valuewrapper.getFloatValue( strategy.evaluate( game,forWhite ) );
		}

		float minValue = valuewrapper.INITAL;
		MoveIterator iter = game.getPossibleMoves();
		while( iter.hasMoreMoves() ) {
			float tempValue;
			Move move = iter.nextMove();

			assert (game.isFreeArea( move.to ) || !game.getFigure( move.to ).isKing() )
				:	"getMin:"+
					game.getFigure( move.from ).getClass().getName()+
					" hits King white Move "+
					move.toString();

			int endoption = game.move( move );

			if( endoption==ChessGameInterface.NO_END ){
				tempValue = getMax( game,
									forWhite,
									depth,
									thisMove_isChess,
									thisMove_hasHitFigure );
			}else if( endoption==ChessGameInterface.MATT ) {
				game.undo();
				return valuewrapper.getOtherPlayerIsMatt( depth+1 );
			}else{
				tempValue = valuewrapper.getDrawValue();
			}

			game.undo();

			if( minValue==valuewrapper.INITAL || minValue>tempValue ) {
				minValue = tempValue;
			}
		}

		assert minValue!=valuewrapper.INITAL
			:"no minimum found";
		
		return minValue;
	}

	private float getMax(	final ChessGameInterface game,
	                     	final boolean forWhite,
							int depth,
							final boolean lastMove_isChess,
							final boolean lastMove_hasHitFigure )
	{
		depth++;

		boolean thisMove_hasHitFigure = game.hasHitFigure();
		boolean thisMove_isChess = game.isCheck( forWhite );
		if( pruner.stopMaxDynamicEvaluation(	depth,
												thisMove_isChess,
												thisMove_hasHitFigure,
												lastMove_isChess,
												lastMove_hasHitFigure ) ) {
			return valuewrapper.getFloatValue( strategy.evaluate( game,forWhite ) );
		}

		float maxValue = valuewrapper.INITAL;
		MoveIterator iter = game.getPossibleMoves();

		while( iter.hasMoreMoves() ){
			float tempValue;
			Move move = iter.nextMove();

			assert (game.isFreeArea( move.to ) || !game.getFigure( move.to ).isKing() )
				:	"getMax:"+
					game.getFigure( move.from ).getClass().getName()+
					" hits King white Move "+
					move.toString();

			int endoption = game.move( move );

			if( endoption==ChessGameInterface.NO_END ){
				tempValue = getMin(	game,
									forWhite,
									depth,
									thisMove_isChess,
									thisMove_hasHitFigure );
			}else if( endoption==ChessGameInterface.MATT ) {
				game.undo();
				return valuewrapper.getThisComputerPlayerIsMatt( depth+1 );
			}else{
				tempValue = valuewrapper.getDrawValue();
			}

			game.undo();

			if( maxValue==valuewrapper.INITAL || tempValue>maxValue ) {
				maxValue = tempValue;
			}
		}

		assert maxValue!=valuewrapper.INITAL
			:"no maximum found";
		
		return maxValue;
	}

	public void setEvaluationStrategy( StaticEvaluationInterface strategy )
	{
		this.strategy = strategy;
	}
	
	public void setSearchTreePruner( SearchTreePruner pruner )
	{
		this.pruner = pruner;
	}
}
