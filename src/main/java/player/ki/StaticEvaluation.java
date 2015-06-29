package player.ki;

import java.util.List;

import board.*;
import helper.*;
import figures.*;

/**
 * @author stephan
 */
class StaticEvaluation implements StaticEvaluationInterface
{
	final private static double PAWN_VALUE   = 1f;
	final private static double ROCK_VALUE   = 4.5f;
	final private static double KNIGHT_VALUE = 3f;
	final private static double BISHOP_VALUE = 3f;
	final private static double QUEEN_VALUE  = 9f;
		
	public float evaluate(ChessGameInterface game,final boolean forWhite)
	{
		return	evaluateFigures(   game,forWhite )
		      +	evaluateRuledArea( game,forWhite ) 
				  + evaluatePosition(  game,forWhite );
	}
	
	private float evaluateFigures( ChessGameInterface game,final boolean forWhite )
	{
		float whiteFigures = 0;
		float blackFigures = 0;
		
		final List<Figure> figures = game.getFigures();
    for(Figure figure: figures) {
			if( figure.isWhite() ) {
				if( figure.isPawn() ) {
					whiteFigures += PAWN_VALUE;
				}else if( figure.isRock() ) {
					whiteFigures += ROCK_VALUE;
				}else if( figure.isKnight() ) {
					whiteFigures += KNIGHT_VALUE;
				}else if( figure.isBishop() ) {
					whiteFigures += BISHOP_VALUE;
				}else if( figure.isQueen() ) {
					whiteFigures += QUEEN_VALUE;
				}
			}else{
				if( figure.isPawn() ) {
					blackFigures += PAWN_VALUE;
				}else if( figure.isRock() ) {
					blackFigures += ROCK_VALUE;
				}else if( figure.isKnight() ) {
					blackFigures += KNIGHT_VALUE;
				}else if( figure.isBishop() ) {
					blackFigures += BISHOP_VALUE;
				}else if( figure.isQueen() ) {
					blackFigures += QUEEN_VALUE;
				}
			}
		}
		
		if( forWhite ) return whiteFigures-blackFigures;
		else           return blackFigures-whiteFigures;
	}
	
	private float evaluateRuledArea( ChessGameInterface game,final boolean forWhite )
	{
		final float VALUE_OF_AREA = 0.015f;
		final float whiteMoves = game.countReachableMoves( true );
		final float blackMoves = game.countReachableMoves( false);
		
		float difference;
		if( forWhite ) difference = whiteMoves - blackMoves;
		else           difference = blackMoves - whiteMoves;
		
		return difference * VALUE_OF_AREA;
	}
	
	private float evaluatePosition( ChessGameInterface game,final boolean forWhite )
	{
		float whiteEvaluation = 0;
		float blackEvaluation = 0;
		
		final List<Figure> figures = game.getFigures();
    for(Figure figure: figures) {
			Position pos  = figure.getPosition();
			if( figure.isPawn() ) {
				if( figure.isWhite() ) whiteEvaluation += evaluatePawn( game,pos );
				else                   blackEvaluation += evaluatePawn( game,pos );
			}else if( figure.isKing() ) {
				if( figure.isWhite() ) whiteEvaluation += evaluateKing( game,pos );
				else                   blackEvaluation += evaluateKing( game,pos );
			}else if( figure.isKnight() ) {
				if( figure.isWhite() ) whiteEvaluation += evaluateKnight( game,pos );
				else                   blackEvaluation += evaluateKnight( game,pos );
			}else if( figure.isBishop() ) {
				if( figure.isWhite() ) whiteEvaluation += evaluateBishop( game,pos );
				else                   blackEvaluation += evaluateBishop( game,pos );
			}
		}
		
		if( forWhite ) return whiteEvaluation - blackEvaluation;
		else           return blackEvaluation - whiteEvaluation;
	}
	
	private float evaluateBishop( ChessGameInterface game,Position pos )
	{
		final float BISHOP_ON_STARTPOSITION_PUNISHMENT   = -0.45f;
		final float BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT = -0.2f;
		
		final boolean isWhite = game.getFigure( pos ).isWhite();
		final int startRow    = isWhite?0:7;
				
		if( pos.row==startRow &&
				(pos.column==2 || pos.column==5) ) {
			return BISHOP_ON_STARTPOSITION_PUNISHMENT; 
		}
		
		
		final int blockingRow    = isWhite?2:5;
		final int blockedPawnRow = isWhite?1:6;
		Position possiblePawn    = Position.get( blockedPawnRow,pos.column );
		
		if( pos.row==blockingRow &&
		    (pos.column==3 || pos.column==4) &&
		     containsFigureOfColor( game,Pawn.class,possiblePawn,isWhite ) ) {
			return BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT; 
		}
				
		return 0;
	}
	
	private float evaluateKnight( ChessGameInterface game,Position pos )
	{
    final float BORDER_KNIGHT_PUNISHMENT = -0.45f;
    if( pos.row==0 || pos.row==7 || pos.column==0 || pos.column==7 ) {
    	return BORDER_KNIGHT_PUNISHMENT; 
    }
    return 0;
	}
	
	private float evaluatePawn( ChessGameInterface game,Position pos )
	{
		float pawnValue = evaluatePawnPosition(game,pos );
		pawnValue       += evaluatePawnDefense( game,pos );
		
		return pawnValue;
	}

	private float evaluatePawnPosition(ChessGameInterface game, Position pos)
	{
		float additionalPawnValue    = 0;
		final float MOVES_GONE_VALUE = 0.2f;
		
		Pawn pawn = (Pawn)game.getFigure( pos );
		int startRow = pawn.isWhite()?1:6;
		int movesGone= Math.abs( pos.row-startRow );
		additionalPawnValue   += movesGone * MOVES_GONE_VALUE;
		
		return additionalPawnValue;
	}
	
	private float evaluatePawnDefense(ChessGameInterface game, Position pos)
	{
		final float DEFENSE_VALUE = 0.10f;				   //Bonuswert, wenn Bauer anderen Bauern deckt,bzw.gedeckt wird
		final float NEXT_TO_VALUE = 0.06f;				   //Bonuswert, wenn Bauer einen Nachbarn hat
		final float UNPROTECTED_BORDER_PAWN_VALUE = -0.2f; //Bestrafung für ungedeckte Bauern am Rand
		
		Pawn pawn             = (Pawn)game.getFigure( pos );
		final boolean isWhite = pawn.isWhite();
		
		int forwardRow  = isWhite?pos.row+1:pos.row-1;
		int backwardRow = isWhite?pos.row-1:pos.row+1;
		
		if( pos.column != 0 ) {
			Position leftForwardPosition = Position.get( forwardRow,pos.column-1 );
			Position leftBackwardPosition = Position.get( backwardRow,pos.column-1 );
			Position leftPosition = Position.get( pos.row,pos.column-1 );
			if( containsFigureOfColor( game,Pawn.class,leftForwardPosition, isWhite ) ) {
				return DEFENSE_VALUE;
			}
			if( containsFigureOfColor( game,Pawn.class,leftBackwardPosition, isWhite ) ) {
				return DEFENSE_VALUE;
			}
			if( containsFigureOfColor( game,Pawn.class,leftPosition, isWhite ) ) {
				return NEXT_TO_VALUE;
			}
		}else{
			Position rightPosition = Position.get( pos.row,1 );
			Position rightForwardPosition = Position.get( forwardRow,1 );
			Position rightBackwardPosition = Position.get( backwardRow,1 );
			if( !(containsFigureOfColor( game,Pawn.class,rightPosition, isWhite )
					|| containsFigureOfColor( game,Pawn.class,rightForwardPosition, isWhite )
					|| containsFigureOfColor( game,Pawn.class,rightBackwardPosition, isWhite )
			    ) ) {
				return  UNPROTECTED_BORDER_PAWN_VALUE;
			}
		}
		
		if( pos.column != 7 ) {
			Position rightForwardPosition = Position.get( forwardRow,pos.column+1 );
			Position rightBackwardPosition = Position.get( backwardRow,pos.column+1 );
			Position rightPosition = Position.get( pos.row,pos.column+1 );
			if( containsFigureOfColor( game,Pawn.class,rightForwardPosition, isWhite ) ) {
				return DEFENSE_VALUE;
			}
			if( containsFigureOfColor( game,Pawn.class,rightBackwardPosition, isWhite ) ) {
				return DEFENSE_VALUE;
			}
			if( containsFigureOfColor( game,Pawn.class,rightPosition, isWhite ) ) {
				return NEXT_TO_VALUE;
			}
		}else{
			Position leftPosition = Position.get( pos.row,6 );
			Position leftForwardPosition = Position.get( forwardRow,6 );
			Position leftBackwardPosition = Position.get( backwardRow,6 );
			if( !(containsFigureOfColor( game,Pawn.class, leftPosition, isWhite )
					|| containsFigureOfColor( game,Pawn.class,leftForwardPosition, isWhite )
					|| containsFigureOfColor( game,Pawn.class,leftBackwardPosition, isWhite )
					) ) {
						return  UNPROTECTED_BORDER_PAWN_VALUE;
			}
		}
		
		return 0;
	}
	
	private boolean containsFigureOfColor( ChessGameInterface game,Class clazz,Position pos, final boolean white )
	{
		if( game.isFreeArea( pos ) ) return false;
		Figure figure = game.getFigure( pos );
		return figure.isWhite()==white && clazz.isInstance( figure );
	}
	

	
	private float evaluateKing( ChessGameInterface game,Position pos )
	{
		float value = evaluateRochade(game, pos);
		value       += evaluateKingDefense( game,pos );
		return value;
	}

	private float evaluateRochade(ChessGameInterface game, Position pos)
	{
		final float NO_ROCHADE_PUNISHMENT = -0.4f;
		King king = (King)game.getFigure( pos );
		
		if( gameStillContainsQueenOfColor( game,!king.isWhite() )
				&& !king.didRochade() ) {
			return NO_ROCHADE_PUNISHMENT;
		}
		return 0;
	}

	private float evaluateKingDefense(ChessGameInterface game, Position pos)
	{
		final float BIG_KING_DEFENSE_VALUE   = 0.6f;
		final float SMALL_KING_DEFENSE_VALUE = 0.2f;
		
		
		King king             = (King)game.getFigure( pos );
		final boolean isWhite = king.isWhite();
		float value          = 0;
		int groundRow         = isWhite? 0:7;
		int secondRow         = isWhite? 1:6;
		
		if( king.didRochade() && pos.row==groundRow ) {
			int minColumn = Math.max( 0,pos.row-1 );
			int maxColumn = Math.min( 7,pos.row+1 );
			for( int column=minColumn;column<=maxColumn;column++ ) {
				Position infrontOfKingPos = Position.get( secondRow,column );
				if( containsFigureOfColor( game,Pawn.class,infrontOfKingPos, isWhite ) ) {
					if( gameStillContainsQueenOfColor( game,!isWhite ) ) {
						value += BIG_KING_DEFENSE_VALUE;
					}else{
						value += SMALL_KING_DEFENSE_VALUE;
					}
				}
			}
		}
		
		return value;
	}
	
	private boolean gameStillContainsQueenOfColor( ChessGameInterface game,final boolean white )
	{
	  final List<Figure> figures = game.getFigures();
    for(Figure figure: figures) {
			if( figure.isWhite()==white && figure.isQueen() ) {
				return true;
			}
		}
		
		return false;
	}
}
