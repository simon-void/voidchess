/*
 * Created on 24.09.2006
 */

package board;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import image.*;
import helper.*;
import figures.*;

public class SimpleArrayBoard
implements SimpleChessBoardInterface
{
	final private Figure[][] game;
	final private LastMoveProvider lastMoveProvider;
	final private FigureFactory figureFactory;
	
	private Position whiteKingPosition;
	private Position blackKingPosition;
	
	private boolean calculatedWhiteCheck;
	private boolean calculatedBlackCheck;
	private boolean isWhiteCheck;
	private boolean isBlackCheck;
	private CheckStatus whiteCheckStatus;
	private CheckStatus blackCheckStatus;

	SimpleArrayBoard(LastMoveProvider lastMoveProvider)
	{
		figureFactory = new FigureFactory();
		game          = new Figure[8][8];
		this.lastMoveProvider = lastMoveProvider;
		init();
	}
	
	//für Testzwecke
	public SimpleArrayBoard( String des, LastMoveProvider lastMoveProvider )
	{
		this(lastMoveProvider );
		init( des );
	}
	
	private void clearCheckComputation()
	{
		calculatedWhiteCheck = false;
		calculatedBlackCheck = false;
		whiteCheckStatus     = null;
		blackCheckStatus     = null;
	}
	
	public boolean isCheck( boolean isWhite )
	{
		if( isWhite ) {
			if( !calculatedWhiteCheck ) {
				isWhiteCheck = CheckSearch.isCheck( this,getKingPosition( isWhite ) );
				calculatedWhiteCheck = true;
			}
			return isWhiteCheck;
		}else {
			if( !calculatedBlackCheck ) {
				isBlackCheck = CheckSearch.isCheck( this,getKingPosition( isWhite ) );
				calculatedBlackCheck = true;
			}
			return isBlackCheck;
		}
	}
	
	public CheckStatus getCheckStatus( boolean isWhite )
	{
	  final ExtendedMove lastMove = lastMoveProvider.getLastMove();
		if( isWhite ) {
			if( whiteCheckStatus==null ) {
				whiteCheckStatus = CheckSearch.analyseCheck( this,isWhite,lastMove );
			}
			return whiteCheckStatus;
		}else {
			if( blackCheckStatus==null ) {
				blackCheckStatus = CheckSearch.analyseCheck( this,isWhite,lastMove );
			}
			return blackCheckStatus;
		}
	}
	
	private Position getPositionOfCodedFigure( String figure_description )
	{
		StringTokenizer st = new StringTokenizer( figure_description,"-",false );
		st.nextToken();
		st.nextToken();
		return Position.get( st.nextToken() );
	}
	
	public void init()
	{
		clear();
		Position pos;
		
		for( int i=0;i<8;i++ ) {
			pos = Position.get( 1,i );
			setFigure( pos,figureFactory.getPawn( pos,true ) );
			pos = Position.get( 6,i );
			setFigure( pos,figureFactory.getPawn( pos,false ) );
		}
		pos = Position.get( "a1" );
		setFigure( pos,figureFactory.getRock(   pos,true ) );
		pos = Position.get( "h1" );
		setFigure( pos,figureFactory.getRock(   pos,true ) );
		pos = Position.get( "b1" );
		setFigure( pos,figureFactory.getKnight( pos,true ) );
		pos = Position.get( "g1" );
		setFigure( pos,figureFactory.getKnight( pos,true ) );
		pos = Position.get( "c1" );
		setFigure( pos,figureFactory.getBishop( pos,true ) );
		pos = Position.get( "f1" );
		setFigure( pos,figureFactory.getBishop( pos,true ) );
		pos = Position.get( "d1" );
		setFigure( pos,figureFactory.getQueen(  pos,true ) );
		pos = Position.get( "e1" );
		setFigure( pos,figureFactory.getKing(   pos,true ) );
		whiteKingPosition = pos;
		
		pos = Position.get( "a8" );
		setFigure( pos,figureFactory.getRock(   pos,false ) );
		pos = Position.get( "h8" );
		setFigure( pos,figureFactory.getRock(   pos,false ) );
		pos = Position.get( "b8" );
		setFigure( pos,figureFactory.getKnight( pos,false ) );
		pos = Position.get( "g8" );
		setFigure( pos,figureFactory.getKnight( pos,false ) );
		pos = Position.get( "c8" );
		setFigure( pos,figureFactory.getBishop( pos,false ) );
		pos = Position.get( "f8" );
		setFigure( pos,figureFactory.getBishop( pos,false ) );
		pos = Position.get( "d8" );
		setFigure( pos,figureFactory.getQueen(  pos,false ) );
		pos = Position.get( "e8" );
		setFigure( pos,figureFactory.getKing(   pos,false ) );
		blackKingPosition = pos;
	}
	
	public void init( String des )
	{
		clear();
		
		StringTokenizer st = new StringTokenizer( des," ",false );
		st.nextToken();
		st.nextToken();
		
		while( st.hasMoreTokens() ) {
			String figure_description = st.nextToken();
			Position pos  = getPositionOfCodedFigure( figure_description );
			Figure figure = figureFactory.getFigureByString( figure_description );
			setFigure( pos,figure );
		}
	}
	
	public void init( int chess960 )
	{
		assert ( chess960>=0 && chess960<960 ) : "chess960 out of bounds";
			
		clear();
		Position pos;
		
		//die Bauernpositionen verändern sich nicht
		for( int i=0;i<8;i++ ) {
			pos = Position.get( 1,i );
			setFigure( pos,figureFactory.getPawn( pos,true ) );
			pos = Position.get( 6,i );
			setFigure( pos,figureFactory.getPawn( pos,false ) );
		}
		
		/*erster Läufer*/
		int rest = chess960%4;
		int row  = rest*2+1;
		chess960 = chess960/4;
		
		pos = Position.get( 0,row );
		setFigure( pos,figureFactory.getBishop( pos,true ) );
		pos = Position.get( 7,row );
		setFigure( pos,figureFactory.getBishop( pos,false ) );
		
		/*zweiter Läufer*/
		rest = chess960%4;
		row  = rest*2;
		chess960 = chess960/4;
		
		pos = Position.get( 0,row );
		setFigure( pos,figureFactory.getBishop( pos,true ) );
		pos = Position.get( 7,row );
		setFigure( pos,figureFactory.getBishop( pos,false ) );
		
		/*zweiter Dame*/
		rest = chess960%6;
		row  = getFreeRow( rest );
		chess960 = chess960/6;
		
		pos = Position.get( 0,row );
		setFigure( pos,figureFactory.getQueen( pos,true ) );
		pos = Position.get( 7,row );
		setFigure( pos,figureFactory.getQueen( pos,false ) );
		
		String[] otherFigures = getFigureArray( chess960 );
		for( int i=0;i<5;i++ ) {
			//immer die erste noch freie Spalte
			row = getFreeRow( 0 );
			pos = Position.get( 0,row );
			Figure figure = createFigure( otherFigures[i],true,pos );
			setFigure( pos,figure );
			if( figure.isKing() ) {
				whiteKingPosition = pos;
			}
			pos = Position.get( 7,row );
			figure = createFigure( otherFigures[i],false,pos );
			setFigure( pos,figure );
			if( figure.isKing() ) {
				blackKingPosition = pos;
			}
		}
	}
	
	private String[] getFigureArray( int index )
	{
		assert index>=0 && index<10;
		
		switch( index ){
			case 0: String[] out0 = { "Springer","Springer","Turm","König","Turm" }; return out0;
			case 1: String[] out1 = { "Springer","Turm","Springer","König","Turm" }; return out1;
			case 2: String[] out2 = { "Springer","Turm","König","Springer","Turm" }; return out2;
			case 3: String[] out3 = { "Springer","Turm","König","Turm","Springer" }; return out3;
			case 4: String[] out4 = { "Turm","Springer","Springer","König","Turm" }; return out4;
			case 5: String[] out5 = { "Turm","Springer","König","Springer","Turm" }; return out5;
			case 6: String[] out6 = { "Turm","Springer","König","Turm","Springer" }; return out6;
			case 7: String[] out7 = { "Turm","König","Springer","Springer","Turm" }; return out7;
			case 8: String[] out8 = { "Turm","König","Springer","Turm","Springer" }; return out8;
			case 9: String[] out9 = { "Turm","König","Turm","Springer","Springer" }; return out9;
		}
		return null;
	}
	
	private Figure createFigure( String name,boolean isWhite,Position pos )
	{
		if( name.equals( "Turm" ) )		return figureFactory.getRock(   pos,isWhite );
		if( name.equals( "Springer" ) )	return figureFactory.getKnight( pos,isWhite );
		if( name.equals( "König" ) )	return figureFactory.getKing(   pos,isWhite );
		
		throw new IllegalStateException("unbekannte Figure:"+name );
	}
	
	private int getFreeRow( int index )
	{
		assert index>=0 && index<8;
		
		int counter = 0;
		for( int row=0;row<8;row++ ) {
			if( isFreeArea( Position.get( 0,row ) ) ) {
				if( index==counter ) return row;
				else                 counter++;
			}
		}
		throw new RuntimeException( "No free Position with index "+index+" found" );
	}

	private void clear()
	{
		clearCheckComputation();
		for( int row=0;row<8;row++ ) {
			for( int column=0;column<8;column++ ) {
				game[row][column] = null;
			}
		}
		whiteKingPosition = null;
		blackKingPosition = null;
	}

	public void setFigure(Position pos, Figure figure)
	{
		clearCheckComputation();

		game[pos.row][pos.column] = figure;
		if( figure!=null && figure.isKing() ){
			if( figure.isWhite() )	whiteKingPosition = pos;
			else					blackKingPosition = pos;
		}
		
	}

	public Figure getFigure(Position pos)
	{
		return game[pos.row][pos.column];
	}
	
	public boolean isFreeArea( Position pos )
	{
		return game[pos.row][pos.column]==null;
	}
	
	public List<Figure> getFigures()
	{
	  List<Figure> figureIter = new ArrayList<Figure>(16);
		
		for( int row=0;row<8;row++ ) {
			for( int column=0;column<8;column++ ) {
				if( game[row][column] != null ) {
					figureIter.add( game[row][column] );
				}
			}
		}
		return figureIter;
	}
	
	public Position getKingPosition( boolean whiteKing )
	{
		if( whiteKing ) return whiteKingPosition;
		else            return blackKingPosition;
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder( 512 );
		for( int row=0;row<8;row++ ) {
			for( int column=0;column<8;column++ ) {
				Position pos = Position.get( row,column );
				if( !isFreeArea(pos) ) {
					buffer.append( getFigure(pos).toString() );
					buffer.append( " " );
				}
			}
		}
		//löscht das letzte space
		if( buffer.length()!=0 ) buffer.deleteCharAt( buffer.length()-1 );
		
		return buffer.toString();
	}
}
