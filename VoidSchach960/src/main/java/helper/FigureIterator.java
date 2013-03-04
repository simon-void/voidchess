package helper;

import figures.*;

/**
 * @author stephan
 */
public class FigureIterator
{
	private ArrayQueue figures;
	
	public FigureIterator()
	{
		figures = new ArrayQueue( 32 );
	}
	
	public void addFigure( Figure figure )
	{
		figures.putLast( figure ); 
	}
	
	public Figure next()
	{
		return (Figure)figures.popFirst();
	}
	
	public boolean hasNext()
	{
		return figures.size()!=0;
	}
	
	public int countFigures()
	{
		return figures.size();
	}
}
