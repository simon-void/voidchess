package figures;


import java.util.ArrayList;
import java.util.List;

import image.FigureImage;
import helper.*;
import board.*;
/**
 * @author stephan
 */
public class Bishop extends Figure
{
	private List<Position> positions = new ArrayList<Position>(8); 
	
	public Bishop(FigureImage figureImage, boolean isWhite, Position startPosition)
	{
		super( figureImage,isWhite,startPosition,(byte)4 );	
	}
	
	public boolean isReachable(Position to,BasicChessGameInterface game)
	{
		int horizontal_difference = Math.abs( position.row-to.row );
		int vertical_difference   = Math.abs( position.column-to.column );
		
		if( horizontal_difference!=vertical_difference || horizontal_difference==0 ) {
			return false;
		}
		
		boolean first_bigger  = position.row>to.row;
		boolean second_bigger = position.column>to.column;
		
		if( first_bigger==second_bigger ) {
			return isNorthEastReachable( to,game );
		}else{
			return isSouthEastReachable( to,game );
		}
	}
	
	private boolean isNorthEastReachable( Position to,BasicChessGameInterface game )
	{
		int min_column,min_row,max_row;
		if(to.row<position.row) {
			min_column = to.column+1;
			min_row    = to.row+1;
			max_row    = position.row;
		}else{
			min_column = position.column+1;
			min_row    = position.row+1;
			max_row    = to.row;
		}
		
		while( min_row<max_row ){
			Position middlePosition = Position.get( min_row,min_column );
			if( !game.isFreeArea(middlePosition) ) {
				return false;
			}
			min_row++;
			min_column++;
		}
		return game.isFreeArea(to) || hasDifferentColor( game.getFigure(to) );
	}
	
	private boolean isSouthEastReachable( Position to,BasicChessGameInterface game )
	{
		int max_column,min_row,max_row;
		if(to.row<position.row) {
			max_column = to.column-1;
			min_row    = to.row+1;
			max_row    = position.row;
		}else{
			max_column = position.column-1;
			min_row    = position.row+1;
			max_row    = to.row;
		}
		
		while( min_row<max_row ){
			Position middlePosition = Position.get( min_row,max_column );
			if( !game.isFreeArea(middlePosition) ) {
				return false;
			}
			min_row++;
			max_column--;
		}
		return game.isFreeArea(to) || hasDifferentColor( game.getFigure(to) );
	}
	
	private void getNorthEastIterator( BasicChessGameInterface game,List<Position> result )
	{
		int row,column;
		
		row = position.row;
		column = position.column;
		while(true) {
			row++;
			column++;
			if( row==8 || column==8 ) break;
			Position checkPosition = Position.get( row,column );
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
	
	private void getSouthEastIterator( BasicChessGameInterface game,List<Position> result )
	{
		int row,column;
		
		row = position.row;
		column = position.column;
		while(true) {
			row--;
			column++;
			if( row<0 || column==8 ) break;
			Position checkPosition = Position.get( row,column );
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
	
	private void getNorthWestIterator( BasicChessGameInterface game,List<Position> result )
	{
		int row,column;
		
		row = position.row;
		column = position.column;
		while(true) {
			row++;
			column--;
			if( row==8 || column<0 ) break;
			Position checkPosition = Position.get( row,column );
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
	
	private void getSouthWestIterator( BasicChessGameInterface game,List<Position> result )
	{
		int row,column;
		
		row = position.row;
		column = position.column;
		while(true) {
			row--;
			column--;
			if( row<0 || column<0 ) break;
			Position checkPosition = Position.get( row,column );
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
		getNorthEastIterator( game,positions );
		getSouthEastIterator( game,positions );
		getNorthWestIterator( game,positions );
		getSouthWestIterator( game,positions );
		
		for(Position pos: positions) {
			result.add( Move.get( position,pos ) );
		}
	}
	
	public boolean isSelectable( SimpleChessBoardInterface game )
	{	
		positions.clear();
		getNorthEastIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();

		getSouthEastIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();
		
		getNorthWestIterator( game,positions );
		for(Position pos: positions) {
			if( !isBound( pos,game ) ) { 
				return true;
			}
		}
		positions.clear();
		
		getSouthWestIterator( game,positions );
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
		getNorthEastIterator( game,positions );
		getSouthEastIterator( game,positions );
		getNorthWestIterator( game,positions );
		getSouthWestIterator( game,positions );

		return positions.size();
	}
	
	public boolean isBishop()
	{
		return true;
	}

	protected String getType()
	{
		return "Bishop";
	}
}
