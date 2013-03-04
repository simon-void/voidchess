package figures;

import image.FigureImage;
import helper.*;
/**
 * @author stephan
 */
abstract public class RochadeFigure extends Figure
{
	protected int stepsTaken;
	final private Position initialPos;

	public RochadeFigure(FigureImage figureImage, boolean isWhite, Position startPosition,byte typeIndex)
	{
		this( figureImage,isWhite,startPosition,0,typeIndex );
	}
	
	public RochadeFigure(FigureImage figureImage, boolean isWhite, Position startPosition,int stepsTaken,byte typeIndex )
	{
		super( figureImage,isWhite,startPosition,typeIndex );
		initialPos      = startPosition;
		this.stepsTaken = stepsTaken;
	}
	
	public Position getInitialPosition()
	{
		return initialPos;
	}

	public boolean canParticipateInRochade()
	{
		return stepsTaken==0;
	}
	
	public void undoMove( Position oldPosition )
	{
		stepsTaken--;
		super.undoMove( oldPosition );
	}

	public void figureMoved( Move move )
	{
		if( position.equalsPosition(move.from) ) stepsTaken++;
		super.figureMoved( move );
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder( 20 );
		buffer.append( super.toString() );
		buffer.append( "-" );
		buffer.append( stepsTaken );
		return buffer.toString();
	}

}
