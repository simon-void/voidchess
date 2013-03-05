package figures;

import java.util.List;

import image.FigureImage;
import helper.*;
import board.*;
/**
 * @author stephan
 */
public class Rock extends RochadeFigure
{
	private BasicPositionIterator posIter = new BasicPositionIterator(); 
	
	public Rock(FigureImage figureImage, boolean isWhite, Position startPosition)
	{
		super( figureImage,isWhite,startPosition,(byte)2 );
	}
	
	public Rock(FigureImage figureImage, boolean isWhite, Position startPosition,int stepsTaken)
	{
		super( figureImage,isWhite,startPosition,stepsTaken,(byte)2 );
	}
	
	public boolean isReachable(Position to,BasicChessGameInterface game)
	{
		return isHorizontalReachable(to,game) || isVerticalReachable(to,game);
	}
	
	private boolean isHorizontalReachable( Position to,BasicChessGameInterface game )
	{
		if( to.row    != position.row)    return false;
		if( to.column == position.column) return false;
		
		int minColumn = Math.min( to.column,position.column );
		int maxColumn = Math.max( to.column,position.column );
		
		for( int column=minColumn+1;column<maxColumn;column++ ) {
			Position middlePosition = Position.get( to.row,column );
			if( !game.isFreeArea( middlePosition ) ) return false;
		}
		
		return game.isFreeArea(to) || hasDifferentColor( game.getFigure(to) );
	}
	
	private boolean isVerticalReachable( Position to,BasicChessGameInterface game )
	{
		if( to.column != position.column) return false;
		if( to.row    == position.row)    return false;

		int minRow = Math.min( to.row,position.row );
		int maxRow = Math.max( to.row,position.row );
		
		for( int row=minRow+1;row<maxRow;row++ ) {
			Position middlePosition = Position.get( row,to.column );
			if( !game.isFreeArea( middlePosition ) ) return false;
		}
		
		return game.isFreeArea(to) || hasDifferentColor( game.getFigure(to) );
	}
	
	private void getNorthIterator( BasicChessGameInterface game,BasicPositionIterator result )
	{
		int row              = position.row;
		
		while( true ) {
			row++;
			if( row==8 ) break;
			Position checkPosition = Position.get( row,position.column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.addPosition( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.addPosition( checkPosition );
				}
				break;
			}
		}
	}
	
	private void getSouthIterator( BasicChessGameInterface game,BasicPositionIterator result )
	{
		int row              = position.row;
		
		while( true ) {
			row--;
			if( row<0 ) break;
			Position checkPosition = Position.get( row,position.column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.addPosition( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.addPosition( checkPosition );
				}
				break;
			}
		}
	}
	
	private void getEastIterator( BasicChessGameInterface game,BasicPositionIterator result )
	{
		int column            = position.column;
		
		while( true ) {
			column++;
			if( column==8 ) break;
			Position checkPosition = Position.get( position.row,column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.addPosition( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.addPosition( checkPosition );
				}
				break;
			}
		}
	}
	
	private void getWestIterator( BasicChessGameInterface game,BasicPositionIterator result )
	{
		int column            = position.column;
		
		while( true ) {
			column--;
			if( column<0 ) break;
			Position checkPosition = Position.get( position.row,column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.addPosition( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.addPosition( checkPosition );
				}
				break;
			}
		}
	}
	
	public void getReachableMoves( BasicChessGameInterface game,List<Move> result )
	{
		posIter.clear(); 
		getNorthIterator( game,posIter );
		getSouthIterator( game,posIter );
		getEastIterator(  game,posIter );
		getWestIterator(  game,posIter );
		
		while( posIter.hasNext() ) {
			result.add( Move.get( position,posIter.next() ) );
		}
	}
	
	public boolean isSelectable( SimpleChessBoardInterface game )
	{
		posIter.clear(); 
		getNorthIterator( game,posIter );
		while( posIter.hasNext() ) {
			if( !isBound( posIter.next(),game ) ) { 
				return true;
			}
		}
		posIter.clear();
		
		getSouthIterator( game,posIter );
		while( posIter.hasNext() ) {
			if( !isBound( posIter.next(),game ) ) { 
				return true;
			}
		}
		posIter.clear();
		
		getEastIterator( game,posIter );
		while( posIter.hasNext() ) {
			if( !isBound( posIter.next(),game ) ) { 
				return true;
			}
		}
		posIter.clear();
				
		getWestIterator( game,posIter );
		while( posIter.hasNext() ) {
			if( !isBound( posIter.next(),game ) ) { 
				return true;
			}
		}
		posIter.clear();
		
		return false;
	}
	
	public int countReachableMoves( BasicChessGameInterface game )
	{
		posIter.clear(); 
		getNorthIterator( game,posIter );
		getSouthIterator( game,posIter );
		getEastIterator(  game,posIter );
		getWestIterator(  game,posIter );
		return posIter.countPositions();
	}
	
	public boolean isRock()
	{
		return true;
	}
	
	protected String getType()
	{
		return "Rock";
	}
}
