package figures;

import image.FigureImage;
import helper.*;
import board.*;
/**
 * @author stephan
 */
public class Queen extends Figure
{
	private Bishop bishop;
	private Rock   rock;

	public Queen(FigureImage figureImage, boolean isWhite, Position startPosition)
	{
		super( figureImage,isWhite,startPosition,(byte)5 );
		bishop = new Bishop( null,isWhite,startPosition );
		rock   = new Rock(   null,isWhite,startPosition );
	}

	public void figureMoved( Move move )
	{
		bishop.figureMoved( move );
		rock.figureMoved(   move );
		super.figureMoved(  move );
	}

	public void undoMove( Position oldPosition )
	{
		bishop.undoMove( oldPosition );
		rock.undoMove(   oldPosition );
		super.undoMove(  oldPosition );
	}
	
	public boolean isReachable(Position to,BasicChessGameInterface game)
	{
		return bishop.isReachable( to,game ) || rock.isReachable( to,game );
	}
	
	public void getReachableMoves( BasicChessGameInterface game,BasicMoveIterator result )
	{
		bishop.getReachableMoves(game,result);
		rock.getReachableMoves( game,result );
	}
	
	public boolean isSelectable( SimpleChessBoardInterface game ) 
	{
		return rock.isSelectable( game ) || bishop.isSelectable( game );
	}
	
	public int countReachableMoves( BasicChessGameInterface game )
	{
		return rock.countReachableMoves( game ) + bishop.countReachableMoves( game );
	}
	
	public boolean isQueen()
	{
		return true;
	}
	
	protected String getType()
	{
		return "Queen";
	}
}
