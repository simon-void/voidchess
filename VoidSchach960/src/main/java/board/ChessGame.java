package board;

import java.util.*;

import image.*;
import figures.*;
import helper.*;

/**
 * @author stephan
 */
public class ChessGame implements ChessGameInterface
{
	private SimpleChessBoardInterface game;
	private FigureFactory figureFactory;
	private boolean whiteTurn;
	private MementoStack mementoStack;
	private ExtendedMoveStack extendedMoveStack;
	private NumberStack numberStack;
	private int numberOfMovesWithoutHit;
	private int figureCount;
	private boolean hasHitFigure;
	private ChessGameSupervisor supervisor;
	private ChessGameSupervisor normalSupervisor;
	final private ChessGameSupervisor dummySupervisor;
	
  /** der normale Konstruktor, der von außerhalb verwendet werden sollte
   * @param supervisor
   * @param figureImageFactory
   */
	public ChessGame( ChessGameSupervisor supervisor, FigureImageFactory figureImageFactory )
	{
		hasHitFigure     = false; 
		normalSupervisor = supervisor;
		dummySupervisor  = new DummyChessGameSupervisor();
		this.supervisor  = normalSupervisor;
		figureFactory    = new FigureFactory( figureImageFactory );
		mementoStack     = new MementoStack();
		extendedMoveStack= new ExtendedMoveStack();
		numberStack      = new NumberStack();
		game             = new SimpleArrayBoard( figureImageFactory );

		initGame();
	}
	
	/**für JUnit-TestKlassen
	 * @param game_description
	 */
	public ChessGame( String game_description )
	{
		this( new DummyChessGameSupervisor(),game_description,new FigureImageFactoryMock("",true) );
	}
	
	/**für JUnit-TestKlassen
	 * @param game_description
	 */
	public ChessGame( int initialPosition )
	{
		this( new DummyChessGameSupervisor(),initialPosition,new FigureImageFactoryMock("",true) );
	}
	
	/**wird nur implizit für JUnit-tests verwendet
	 * @param supervisor
	 * @param game_description
	 * @param figureImageFactory
	 */
	private ChessGame( ChessGameSupervisor supervisor,
	           String game_description,
	           FigureImageFactory figureImageFactory )
	{
		normalSupervisor = supervisor;
		dummySupervisor  = new DummyChessGameSupervisor();
		this.supervisor  = normalSupervisor;
		figureFactory    = new FigureFactory( figureImageFactory );
		mementoStack     = new MementoStack();
		extendedMoveStack= new ExtendedMoveStack();
		numberStack      = new NumberStack();
		
		StringTokenizer st = new StringTokenizer( game_description," ",false );
		whiteTurn = st.nextToken().equals( "white" );
		numberOfMovesWithoutHit = Integer.parseInt( st.nextToken() );
		for( int i=0;i<numberOfMovesWithoutHit;i++ ) numberStack.noFigureHit();
				
		figureCount = 0;
		while( st.hasMoreTokens() ) {
			figureCount++;
			st.nextToken();
		}
		
		game = new SimpleArrayBoard( figureImageFactory );
		game.init( game_description );
		
		memorizeGame();
		hasHitFigure = numberOfMovesWithoutHit==0;
	}
	
	/**wird nur implizit für JUnit-tests verwendet
	 * @param supervisor
	 * @param initialPosition
	 * @param figureImageFactory
	 */
	private ChessGame(	ChessGameSupervisor supervisor,
	                   	int initialPosition,
	                   	FigureImageFactory figureImageFactory )
	{
		normalSupervisor = supervisor;
		dummySupervisor  = new DummyChessGameSupervisor();
		this.supervisor  = normalSupervisor;
		figureFactory    = new FigureFactory( figureImageFactory );
		mementoStack     = new MementoStack();
		extendedMoveStack= new ExtendedMoveStack();
		numberStack      = new NumberStack();
		
		whiteTurn 				= true;
		hasHitFigure 			= false;
		numberOfMovesWithoutHit = 0;
		figureCount 			= 32;
		
		game = new SimpleArrayBoard( figureImageFactory );
		game.init( initialPosition );
		
		memorizeGame();
	}
	
	public void setSupervisor( ChessGameSupervisor supervisor )
	{
		normalSupervisor = supervisor;
		useNormalSupervisor();
	}
	
	public void useNormalSupervisor()
	{
		supervisor = normalSupervisor;
	}
	
	public void useDummySupervisor()
	{
		supervisor = dummySupervisor;
	}
	
	public Position getKingPosition( boolean whiteKing )
	{
		return game.getKingPosition( whiteKing );
	}
	
	public boolean isFreeArea( Position pos )
	{
		return game.isFreeArea(pos);
	}
	
	public Figure getFigure( Position pos )
	{
		return game.getFigure(pos);
	}
	
	private void setFigure( Position pos,Figure figure )
	{
		game.setFigure(pos,figure);
	}
	
	public FigureIterator getFigures()
	{
		return game.getFigures();
	}
	
	public boolean isSelectable( Position pos,boolean whitePlayer )
	{
		if( isFreeArea( pos ) ) return false;
		Figure figure = getFigure( pos );
		return figure.isWhite()==whitePlayer && figure.isSelectable( game );
	}
	
	public boolean isMoveable( Position from,Position to,boolean whitePlayer )
	{
		if( isFreeArea( from ) ) return false;
		Figure figure = getFigure( from );
		return figure.isWhite()==whitePlayer && figure.isMoveable( to,game );
	}
	
	public int countFigures()
	{
		return figureCount;
	}
	
	public int move( Move move )
	{
		assert !isFreeArea( move.from )
			:"the move moves a null value:"+move.toString();
		assert (getFigure( move.from ).isWhite()== whiteTurn )
			:"figure to be moved has wrong color";
	
		Rock rochadeRock  = extractRochadeRock(  move );
		//im Fall der Rochade wird der Zug jetzt so umgebogen,
		//das move.to dem Zielfeld des Königs entspricht
		//und nicht dem Feld des Rochadeturms
		if( rochadeRock!=null ) {
			final int row = move.to.row;
			final int column = move.to.column-move.from.column>0?6:2;
			move = Move.get( move.from,Position.get( row,column ) );
		}
		
		Pawn hitPawn      = handleEnpasent( move );
		Figure hitFigure  = moveFigure(move);
		
		informFiguresOfMove(move);
		reinsertRochadeRock( rochadeRock,move.to );
		boolean pawnTransformed = handlePawnTransformation( move );
		
		memorizeGame();
		memorizeMove( move,!whiteTurn,pawnTransformed,hitPawn,rochadeRock,hitFigure );
		
		return isEnd();
	}
	
	private Figure moveFigure(Move move)
	{
		final boolean toEqualsFrom = move.to.equalsPosition( move.from );//für manche Schach960rochaden true
		hasHitFigure = !isFreeArea( move.to ) && !toEqualsFrom;  //Enpasent wird nicht beachtet
		Figure fromFigure = getFigure( move.from );
		
		if( hasHitFigure ) {
			numberStack.figureHit();
			numberOfMovesWithoutHit=0;
			figureCount--;
		}else{
			numberStack.noFigureHit();
			numberOfMovesWithoutHit++;
		}
		
		Figure hitFigure = null;
		if( !toEqualsFrom ) {
			hitFigure = getFigure( move.to );
			setFigure( move.to,fromFigure );
			setFigure( move.from,null );
		}
		
		whiteTurn = !whiteTurn;
		
		return hitFigure;
	}
	
	public boolean hasHitFigure()
	{
		return hasHitFigure;
	}

	private Pawn handleEnpasent( Move move )
	{
		if(  getFigure(move.from).isPawn()
		  && move.from.column != move.to.column
		  && isFreeArea(move.to)
		) {
			Position pawnToBeHit = Position.get(move.from.row,move.to.column );
			Pawn pawn            = (Pawn)getFigure( pawnToBeHit );
		  setFigure( pawnToBeHit,null );
			figureCount--;
		  numberOfMovesWithoutHit = -1;			//die Variable wird dann von move Figure auf 0 gesetzt
		  return pawn;	
		}
		return null;
	}
	
	private Rock extractRochadeRock( Move move )
	{
		Figure movingFigure = getFigure( move.from );
		Figure rochadeRock = getFigure(  move.to );
		if( movingFigure.isKing() &&
			rochadeRock!=null &&
			rochadeRock.isWhite()==movingFigure.isWhite() )
		{
			setFigure( move.to,null );	//der Turm wird kurzfristig vom Brett genommen
			((King)movingFigure).performRochade();
			return (Rock)rochadeRock;
		}
		return null;
	}
	
	private void reinsertRochadeRock( Rock rochadeRock,Position moveTo )
	{
		if( rochadeRock!=null ) {
			Position rockFrom = rochadeRock.getPosition();
			Position rockTo   = moveTo.column==6?
									Position.get( moveTo.row,5 ):
									Position.get( moveTo.row,3 );
			rochadeRock.figureMoved( Move.get( rockFrom,rockTo ) );
			setFigure( rockTo,rochadeRock );
		}
	}
	
	private boolean handlePawnTransformation( Move move )
	{
		if( getFigure( move.to ).isPawn() ) {
			if( move.to.row==0 || move.to.row==7 ) {
				String figure = supervisor.askForPawnChange( move.to );
				boolean isWhite = move.to.row == 7;
				Figure newFigure = null;
				if( figure.equals( "Queen" ) ) {
					newFigure = figureFactory.getQueen( move.to,isWhite );
				}else if( figure.equals( "Rock" ) ) {
					newFigure = figureFactory.getRock( move.to,isWhite );
				}else if( figure.equals( "Knight" ) ) {
					newFigure = figureFactory.getKnight( move.to,isWhite );
				}else if( figure.equals( "Bishop" ) ) {
					newFigure = figureFactory.getBishop( move.to,isWhite );
				}else {
					throw new NullPointerException( "invalide pawn-transformation-string:"+figure );
				}
				setFigure( move.to,newFigure );
				return true;
			}
		}
		return false;
	}
	
	private int isEnd()
	{
		if( noMovesLeft( whiteTurn ) ) {
			if( isCheck( whiteTurn ) ) {
				return MATT;
			}else{
				return PATT;
			}
		}
		if( isDrawBecauseOfLowMaterial() ) {
			return DRAW;
		}
		if( isDrawBecauseOfThreeTimesSamePosition() ) {
			return THREE_TIMES_SAME_POSITION;
		}
		if( numberOfMovesWithoutHit==100 ) {
			return FIFTY_MOVES_NO_HIT;
		}
		return NO_END;
	}

	private void informFiguresOfMove(Move move)
	{
		FigureIterator figureIter = game.getFigures();
		while( figureIter.hasNext() ) {
			Figure figure = figureIter.next();
			figure.figureMoved( move );
		}
	}
	
	public void undo()
	{
		whiteTurn = !whiteTurn;
		numberOfMovesWithoutHit = numberStack.undo();
		mementoStack.popMemento();
		
		ExtendedMove lastMove    = extendedMoveStack.popExtendedMove();
		final boolean wasRochade = lastMove.isRochade();
		Figure activeFigure = getFigure( lastMove.to );
		setFigure( lastMove.from,activeFigure );
		if( !wasRochade || !lastMove.from.equalsPosition( lastMove.to ) ) {
			setFigure( lastMove.to  ,lastMove.getFigure() );
		}
		activeFigure.undoMove( lastMove.from );
		
		if( wasRochade )  			    undoRochade( lastMove );
		if( lastMove.isEnpassent() )    undoEnpassent( lastMove );
		if( lastMove.pawnTransformed() )undoPawnTransformation( lastMove );
		rebuildPawnEnpassentCapability();
	}
	
	private void undoRochade( ExtendedMove lastMove )
	{
		Rock rock = (Rock)lastMove.getEnpassentPawnOrRochadeRock();
		Position rockStartPos   = rock.getInitialPosition();
		Position rockCurrentPos = rock.getPosition();
		
		setFigure(		rockStartPos,rock );
		if( !rockStartPos.equalsPosition( rockCurrentPos ) && !lastMove.from.equalsPosition( rockCurrentPos ) ) {
			setFigure( 	rockCurrentPos,null );
		}
		rock.undoMove(	rockStartPos );
	}
	
	private void undoEnpassent( ExtendedMove lastMove )
	{
		Pawn hitPawn = (Pawn)lastMove.getEnpassentPawnOrRochadeRock();
		Position pawnPos = Position.get( lastMove.from.row,lastMove.to.column );
		setFigure( pawnPos,hitPawn );
		hitPawn.setCanBeHitByEnpasent();
	}
		
	private void undoPawnTransformation( ExtendedMove lastMove )
	{
		Position pawnPos = lastMove.from;
		Figure pawn = figureFactory.getPawn( pawnPos,lastMove.getColorOfMove() );
		setFigure( pawnPos,pawn );
	}
	
	private void rebuildPawnEnpassentCapability()
	{
		if( extendedMoveStack.isEmpty() ) return;
		
		ExtendedMove newLatestMove = extendedMoveStack.topExtendedMove();
		Figure figure = getFigure( newLatestMove.to );
		if( figure.isPawn() && 
				Math.abs( newLatestMove.from.row-newLatestMove.to.row )==2 )
		{
			((Pawn)figure).setCanBeHitByEnpasent();
		}
	}
	
	public String toString()
	{
		StringBuilder buffer = new StringBuilder( 512 );
		
		if( whiteTurn ) buffer.append( "white " );
		else            buffer.append( "black " );
		
		buffer.append( numberOfMovesWithoutHit );
		buffer.append( " " );
		buffer.append( game.toString() );
		
		return buffer.toString();
	}
	
	public String getHistory()
	{
		return extendedMoveStack.getLatestMoves( 4 );
	}
	
	private void initGame()
	{
	  initGame( 518 );	//normale Schachposition
	}
	
	public void initGame(  int chess960 )
	{
		whiteTurn = true;
		numberOfMovesWithoutHit = 0;
		figureCount = 32;
		mementoStack.clear();
		extendedMoveStack.clear();
		numberStack.init();
		
		game.init( chess960 );
		
		memorizeGame();
	}
	
	public boolean equals( ChessGame other )
	{
		if( whiteTurn!=other.whiteTurn ) return false;
		
		for( int row=0;row<8;row++ ) {
			for( int column=0;column<8;column++ ) {
				Position pos = Position.get( row,column );
				if( isFreeArea(pos)!=other.isFreeArea(pos) ) return false;
				if( !isFreeArea(pos) ) {
					Figure figure1 = getFigure( pos );
					Figure figure2 = other.getFigure( pos );
					if( !figure1.equals(figure2) ) return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean noMovesLeft( boolean caseWhite )
	{
		FigureIterator figureIter = game.getFigures();
		while( figureIter.hasNext() ) {
			Figure figure = figureIter.next();
			if( figure.isWhite()==caseWhite && 
				 figure.isSelectable( game ) ) {
				return false;
			}
		}
		return true;
	}
		
	private boolean isDrawBecauseOfLowMaterial()
	{
		int numberOfWhiteBishops = 0;
		int numberOfBlackBishops = 0;
		int numberOfWhiteKnights = 0;
		int numberOfBlackKnights = 0;
		
		FigureIterator figureIter = game.getFigures();
		while( figureIter.hasNext() ) {
			Figure figure = figureIter.next();
			if(  figure.isPawn()
				|| figure.isRock()
				|| figure.isQueen() ) {
					return false;
			}else if( figure.isBishop() ) {
				if( figure.isWhite() ) numberOfWhiteBishops++;
				else                   numberOfBlackBishops++;
			}else if( figure.isKnight() ) {
				if( figure.isWhite() ) numberOfWhiteKnights++;
				else                   numberOfBlackKnights++;
			}
		}
		
		if( numberOfWhiteBishops>1 || numberOfBlackBishops>1 ) {
			return false;
		}
		if( numberOfWhiteKnights>2 || numberOfBlackKnights>2 ) {
			return false;
		}
		if( numberOfWhiteBishops==1 && numberOfWhiteKnights>0 ) {
			return false;
		}
		if( numberOfBlackBishops==1 && numberOfBlackKnights>0 ) {
			return false;
		}
		return true;
	}
	
	private boolean isDrawBecauseOfThreeTimesSamePosition()
	{
		int occurencesOfMemento = mementoStack.countOccurencesOfLastMemento();
		return occurencesOfMemento >= 3;
	}
	
	public boolean isWhiteTurn()
	{
		return whiteTurn;
	}
	
	public boolean isCheck( boolean isWhiteInCheck)
	{
		return CheckSearch.isCheck( game,game.getKingPosition( isWhiteInCheck ) );
	}
	
	public List<Move> getPossibleMoves()
	{
		final Position kingPos = game.getKingPosition( whiteTurn );
		
		List<Move> possibleMoves = new ArrayList<>(50);
		FigureIterator figureIter = game.getFigures();

		getFigure( kingPos ).getPossibleMoves( game,possibleMoves );
		while( figureIter.hasNext() ) {
			Figure figure = figureIter.next();
			if( figure.isWhite()==whiteTurn && !figure.isKing() ) {
				figure.getPossibleMoves( game,possibleMoves );
			}
		}
			
		return possibleMoves;
	}
	
	public int countReachableMoves( boolean forWhite )
	{
		int count = 0;
		
		FigureIterator figureIter = game.getFigures();
		while( figureIter.hasNext() ) {
			Figure figure = figureIter.next();
			if( figure.isWhite()==forWhite ) {
				count += figure.countReachableMoves( game );
			}
		}
		
		return count;
	}
	
	public ChessGameInterface copyGame()
	{
		//TODO 3-Zug-Matt fällt so weg
		return new ChessGame( toString() );
//		return new ChessGame(this);
	}

	private void memorizeGame()
	{
		mementoStack.putMemento( new Memento( game,whiteTurn ) );
	}
	
	private void memorizeMove(	Move move,
								boolean whiteMove,
								boolean pawnTransformed,
								Pawn hitPawn,
								Rock rochadeRock,
								Figure hitFigure )
	{		
		boolean hitsEnpassent = hitPawn!=null;
		boolean isRochade     = rochadeRock!=null;
		Figure rochadeRockOrEnpassentPawn = hitPawn;
		if( isRochade ) {
			rochadeRockOrEnpassentPawn = rochadeRock;
		}
		ExtendedMove extendedMove = new ExtendedMove(	move.from,
														move.to,
														hitFigure,
														rochadeRockOrEnpassentPawn,
														whiteMove,
														isRochade,
														hitsEnpassent,
														pawnTransformed );
		extendedMoveStack.putExtendedMove( extendedMove );
	}
	
	private class Memento
	{
		final private int      figureCount;
		final private boolean  isWhite;
		final private byte[][] board = new byte[8][8];
		final private static byte eight = 8;
		
		private Memento( BasicChessGameInterface game,boolean isWhite )
		{
			final byte nullbyte = 0;
			int count = 0;
			for( byte row=0;row<eight;row++ ) {
				for( byte column=0;column<eight;column++ ) {
					Figure figure = game.getFigure( Position.get( row,column ) );
					if( figure!=null ) {
						board[row][column] = figure.getTypeInfo();
						count++;
					}else {
						board[row][column] = nullbyte;
					}
				}
			}
			this.isWhite    = isWhite;
			figureCount     = count;
		}
		
		private boolean hasDifferentNumberOfFiguresAs( Memento other )
		{
			return figureCount != other.figureCount;
		}
		
		private boolean equals( Memento other )
		{
			for( byte row=0;row<eight;row++ ) {
				for( byte column=0;column<eight;column++ ) {
					if( board[row][column] != other.board[row][column] ) {
						return false;
					}
				}
			}
			return isWhite == other.isWhite;
		}
	}
	
	private class MementoStack
	{
		private Memento[] mementoArray;
		private int index;
		
		MementoStack()
		{
			mementoArray = new Memento[ 200 ];
			index        = 0;
		}
		
		//Copy-Constructor
		MementoStack(MementoStack other)
		{
			mementoArray = new Memento[ other.mementoArray.length ];
			System.arraycopy(other.mementoArray, 0, mementoArray, 0, mementoArray.length);
			index        = other.index;
		}
		
		void putMemento( Memento memento )
		{
			ensureCapacity();
			mementoArray[index++] = memento;
		}
		
		void popMemento()
		{
			if( index>0 ) index--;
		}
		
		int countOccurencesOfLastMemento()
		{
			assert index>0;
			int count = 1;
			
			Memento lastMemento = mementoArray[index-1];
			for( int i=index-3;i>=0;i=i-2 ) {
				Memento previousMemento = mementoArray[i];
				if( previousMemento.hasDifferentNumberOfFiguresAs( lastMemento ) ) {
					break;
				}
				if( lastMemento.equals( previousMemento ) ) {
					count++;
				}
			}
			
			return count;
		}
		
		void clear()
		{
			index=0;
		}
		
		private void ensureCapacity()
		{
			if( index==mementoArray.length ) {
				Memento[] newMementoArray = new Memento[mementoArray.length*2];
				System.arraycopy( mementoArray,0,newMementoArray,0,index );
				mementoArray = newMementoArray;
			}
		}
	}
	
	private class ExtendedMoveStack
	{
		private ExtendedMove[] extendedMoveArray;
		private int index;
		
		ExtendedMoveStack()
		{
			extendedMoveArray = new ExtendedMove[ 100 ];
			index             = 0;
		}
		
		ExtendedMoveStack(ExtendedMoveStack other)
		{
			extendedMoveArray = new ExtendedMove[ other.extendedMoveArray.length ];
			System.arraycopy(other.extendedMoveArray, 0, extendedMoveArray, 0, extendedMoveArray.length);
			index             = other.index;
		}
		
		void putExtendedMove( ExtendedMove extendedMove )
		{
			ensureCapacity();
			extendedMoveArray[index++] = extendedMove;
		}
		
		ExtendedMove popExtendedMove()
		{
			assert index>0;
			return extendedMoveArray[--index];
		}
		
		ExtendedMove topExtendedMove()
		{
			assert index>0;
			return extendedMoveArray[index-1];
		}
		
		boolean isEmpty()
		{
			return index==0;
		}
		
		void clear()
		{
			index = 0;
		}
		
		String getLatestMoves( int count )
		{
			assert count>0;
			
			final int MIN_INDEX = Math.max( 0,index-count );
			StringBuilder sb = new StringBuilder( 24 );
			for( int i=index-1;i>=MIN_INDEX;i-- ) {
				sb.append( extendedMoveArray[i].toString() );
				sb.append( "," );
			}
			final int deleteCharAt = sb.length()-1;
			if( deleteCharAt<0 ) {
				return "*";
			}
			sb.deleteCharAt( deleteCharAt );
			
			return sb.toString();
		}
		
		private void ensureCapacity()
		{
			if( index==extendedMoveArray.length ) {
				ExtendedMove[] newExtendedMoveArray = new ExtendedMove[extendedMoveArray.length*2];
				System.arraycopy( extendedMoveArray,0,newExtendedMoveArray,0,index );
				extendedMoveArray = newExtendedMoveArray;
			}
		}
	}
	
	private class NumberStack
	{
		private int[] numberStack;
		private int index;
		
		NumberStack()
		{
			numberStack = new int[50];
			init();
		}
		
		//copy-Constructor
		NumberStack(NumberStack other)
		{
			numberStack = new int[ other.numberStack.length ];
			System.arraycopy(other.numberStack, 0, numberStack, 0, numberStack.length);
			index             = other.index;
		}
		
		void init()
		{
			for( int i=0;i<numberStack.length;i++ ) numberStack[i] = 0;
			index = 0;
		}
		
		void noFigureHit()
		{
			numberStack[index]++;
		}
		
		void figureHit()
		{
			ensureCapacity();
			index++;
		}
		
		int undo()
		{
			if( numberStack[index]==0 ) {
				index--;
			}else{
				numberStack[index]--;
			}
			return numberStack[index];
		}
		
		private void ensureCapacity()
		{
			if( index+1 == numberStack.length ) {
				int[] newNumberStack = new int[numberStack.length*2];
				System.arraycopy(numberStack,0,newNumberStack,0,numberStack.length);
				numberStack = newNumberStack;
			}
		}
	}
}
