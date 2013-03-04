package figures;

import image.FigureImage;
import helper.*;
import board.*;
/**
 * @author stephan
 */
class MockFigure extends Figure
{
	MockFigure( FigureImage figureImage,boolean isWhite,Position position )
	{
		super( figureImage,isWhite,position,(byte)7  );
	}
	
	public boolean isReachable(Position to,BasicChessGameInterface game)
	{
		return false;
	}
	
	public void getReachableMoves( BasicChessGameInterface game,BasicMoveIterator result )
	{
	}
	
	public boolean isSelectable( SimpleChessBoardInterface game )
	{
		return true;
	}
	
	public int countReachableMoves( BasicChessGameInterface game )
	{
		return 0;
	}
	
	protected String getType()
	{
		return "MockFigure";
	}
}
