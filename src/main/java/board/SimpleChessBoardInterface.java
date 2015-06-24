package board;

import figures.Figure;
import helper.*;
/**
 * @author stephan
 */
public interface SimpleChessBoardInterface extends BasicChessGameInterface
{
	public void init();
	public void init( int chess960 );
	public void init( String des );
	public void setFigure( Position pos,Figure figure );
	public boolean isCheck( boolean isWhite );
	public CheckStatus getCheckStatus( boolean isWhite );
}
