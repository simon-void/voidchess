

package player.ki;

import java.util.ArrayList;
import java.util.List;

import board.*;
import helper.*;

/**
 * @author stephan
 */
public class DynamicEvaluation
{
  final private int possibleMovesBufferSize = 40;
  
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
		  final List<Move> minPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);
			result = getMin( game,forWhite,depth,thisMove_isChess,thisMove_hasHitFigure,minPossibleMovesBuffer );
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
	                      final boolean lastMove_hasHitFigure,
                        final List<Move> minPossibleMovesBuffer )
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
		
		minPossibleMovesBuffer.clear();
		game.getPossibleMoves(minPossibleMovesBuffer);
		final List<Move> maxPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);
		
		for(Move move: minPossibleMovesBuffer) {
			float tempValue;

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
									thisMove_hasHitFigure,
									maxPossibleMovesBuffer);
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
	                     	final boolean lastMove_hasHitFigure,
	                      final List<Move> maxPossibleMovesBuffer)
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
		
		maxPossibleMovesBuffer.clear();
    game.getPossibleMoves(maxPossibleMovesBuffer);
    final List<Move> minPossibleMovesBuffer = new ArrayList<Move>(possibleMovesBufferSize);
    
    for(Move move: maxPossibleMovesBuffer) {
			float tempValue;

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
									thisMove_hasHitFigure,
									minPossibleMovesBuffer);
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
