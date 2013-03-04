package board;

import helper.FigureIterator;
import helper.Position;
import figures.Figure;

/**
 * @author stephan
 */
public interface BasicChessGameInterface
{
	public boolean isFreeArea( Position pos );
	public Figure getFigure( Position pos );
	public Position getKingPosition( boolean whiteKing );
	public FigureIterator getFigures();
}
