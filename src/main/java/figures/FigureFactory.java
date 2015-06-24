package figures;

import java.util.StringTokenizer;
import image.FigureImageFactory;
import helper.*;
/**
 * @author stephan
 */
final public class FigureFactory
{
	private FigureImageFactory figureImageFactory;
  
	public FigureFactory( FigureImageFactory figureImageFactory )
	{
		this.figureImageFactory = figureImageFactory;
	}
  
	public Figure getKing(Position pos, boolean isWhite)
	{
		return new King( figureImageFactory.getKing( isWhite ),isWhite,pos );
	}
	
	private Figure getKing(Position pos, boolean isWhite,int stepsTaken,boolean didRochade)
	{
		return new King( figureImageFactory.getKing( isWhite ),isWhite,pos,stepsTaken,didRochade );
	}

	public Figure getQueen(Position pos, boolean isWhite)
	{
		return new Queen( figureImageFactory.getQueen( isWhite ),isWhite,pos );
	}

	public Figure getBishop(Position pos, boolean isWhite)
	{
		return new Bishop( figureImageFactory.getBishop( isWhite ),isWhite,pos );
	}

	public Figure getKnight(Position pos, boolean isWhite)
	{
		return new Knight( figureImageFactory.getKnight( isWhite ),isWhite,pos );
	}

	public Figure getRock(Position pos, boolean isWhite)
	{
		return new Rock( figureImageFactory.getRock( isWhite ),isWhite,pos );
	}
	
	private Figure getRock(Position pos, boolean isWhite,int stepsTaken)
	{
		return new Rock( figureImageFactory.getKing( isWhite ),isWhite,pos,stepsTaken );
	}

	public Figure getPawn(Position pos, boolean isWhite)
	{
		return new Pawn( figureImageFactory.getPawn( isWhite ),isWhite,pos );
	}
	
	private Figure getPawn(Position pos, boolean isWhite,boolean canBeHitByEnpasent)
	{
		return new Pawn( figureImageFactory.getKing( isWhite ),isWhite,pos,canBeHitByEnpasent );
	}
	
	public void setFigureImageFactory( FigureImageFactory figureImageFactory )
	{
		this.figureImageFactory = figureImageFactory;
	}
	
	public Figure getFigureByString( String description )
	{
		StringTokenizer st = new StringTokenizer( description,"-",false );
		String type     = st.nextToken();
		boolean isWhite = st.nextToken().equals( "white" );
		Position pos    = Position.get( st.nextToken() );
		
		if( type.equals("Knight") ) return getKnight( pos,isWhite );
		if( type.equals("Bishop") ) return getBishop( pos,isWhite );
		if( type.equals("Queen") )  return getQueen(  pos,isWhite );
		
		if( type.equals("Pawn") ) {
			boolean readyForRochadeOrEnpasent = st.nextToken().equals("true");
			return getPawn(pos,isWhite,readyForRochadeOrEnpasent);
		}
		
		int stepsTaken = Integer.parseInt( st.nextToken() ); 
		if( type.equals( "Rock") ) return getRock(pos,isWhite,stepsTaken );
		
		boolean didRochade =st.hasMoreTokens() && st.nextToken().equals( "true" );
		if( type.equals( "King") ) return getKing(pos,isWhite,stepsTaken,didRochade );
		
		throw new IllegalArgumentException( "figure description misformated" );
	}
}
