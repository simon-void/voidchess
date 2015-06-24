package figures;

import java.util.ArrayList;
import java.util.List;

import image.FigureImage;
import helper.*;
import board.*;
/**
 * @author stephan
 */
public class Rock extends RochadeFigure
{
  private List<Position> positions = new ArrayList<Position>(8);
	
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
	
	private void getNorthIterator( BasicChessGameInterface game,List<Position> result )
	{
		int row              = position.row;
		
		while( true ) {
			row++;
			if( row==8 ) break;
			Position checkPosition = Position.get( row,position.column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.add( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.add( checkPosition );
				}
				break;
			}
		}
	}
	
	private void getSouthIterator( BasicChessGameInterface game,List<Position> result )
	{
		int row              = position.row;
		
		while( true ) {
			row--;
			if( row<0 ) break;
			Position checkPosition = Position.get( row,position.column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.add( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.add( checkPosition );
				}
				break;
			}
		}
	}
	
	private void getEastIterator( BasicChessGameInterface game,List<Position> result )
	{
		int column            = position.column;
		
		while( true ) {
			column++;
			if( column==8 ) break;
			Position checkPosition = Position.get( position.row,column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.add( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.add( checkPosition );
				}
				break;
			}
		}
	}
	
	private void getWestIterator( BasicChessGameInterface game,List<Position> result )
	{
		int column            = position.column;
		
		while( true ) {
			column--;
			if( column<0 ) break;
			Position checkPosition = Position.get( position.row,column );
			Figure figure = game.getFigure( checkPosition );
			if( figure==null ) {
				result.add( checkPosition );
			}else{
				if( isWhite!=figure.isWhite ) {
					result.add( checkPosition );
				}
				break;
			}
		}
	}
	
	public void getReachableMoves( BasicChessGameInterface game,List<Move> result )
	{
		positions.clear(); 
		getNorthIterator( game,positions );
		getSouthIterator( game,positions );
		getEastIterator(  game,positions );
		getWestIterator(  game,positions );
		
		for(Position pos: positions) {
			result.add( Move.get( position,pos ) );
		}
	}
	
	public boolean isSelectable( SimpleChessBoardInterface game )
	{
		positions.clear(); 
		getNorthIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();
		
		getSouthIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();
		
		getEastIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();
				
		getWestIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();
		
		return false;
	}
	
	public int countReachableMoves( BasicChessGameInterface game )
	{
		positions.clear(); 
		getNorthIterator( game,positions );
		getSouthIterator( game,positions );
		getEastIterator(  game,positions );
		getWestIterator(  game,positions );
		return positions.size();
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
