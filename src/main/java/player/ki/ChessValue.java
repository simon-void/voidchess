/*
 * Created on 02.10.2006
 */

package player.ki;

import java.text.DecimalFormat;

public class ChessValue
{

	final private static ChessValue singelton = new ChessValue( 1000,0.2f );

	private final float	MAXIMAL_MATT_DEPTH	= 100;
	public final float INITAL 				= Float.MIN_VALUE;
	final private float MIN_FLOAT_VALUE;
	final private float MAX_FLOAT_VALUE;
	final private float EQUALITY_RADIUS;
	final private DecimalFormat formater;
	
	public static ChessValue getInstance()
	{
		return singelton;
	}
	
	private ChessValue( final float max_float_value,final float equality_radius )
	{
		assert max_float_value>MAXIMAL_MATT_DEPTH && max_float_value<1000000
			: "max_float_value should be at least 100";
		assert equality_radius<1   && equality_radius>0
			: "if equality_radius isn't smaller than one, problems with hasAlmostSameValue() will occur";
		
		MIN_FLOAT_VALUE = -max_float_value;
		MAX_FLOAT_VALUE =  max_float_value;
		EQUALITY_RADIUS =  equality_radius;
		
		formater = new DecimalFormat();
		formater.setMinimumFractionDigits( 2 );
		formater.setMaximumFractionDigits( 2 );
	}
	
	public float getFloatValue( float value )
	{
		assert value>=MIN_FLOAT_VALUE && value<=MAX_FLOAT_VALUE
			: "chess value is going to be truncated";
		
		if( value<MIN_FLOAT_VALUE ) return MIN_FLOAT_VALUE;
		if( value>MAX_FLOAT_VALUE ) return MIN_FLOAT_VALUE;
		
		return value;
	}
	
	public float getDrawValue()
	{
		return 0;
	}
	
	public float getOtherPlayerIsMatt( int depth )
	{
		assert depth<MAXIMAL_MATT_DEPTH && depth>0;
		return MIN_FLOAT_VALUE-(MAXIMAL_MATT_DEPTH-depth);
	}

	public float getThisComputerPlayerIsMatt( int depth )
	{
		assert depth<MAXIMAL_MATT_DEPTH && depth>0;
		return MAX_FLOAT_VALUE+(MAXIMAL_MATT_DEPTH-depth);
	}
	
	public boolean hasAlmostSameValue( float first,float second )
	{
		return Math.abs( first-second ) <= EQUALITY_RADIUS;
	}
	
	public String chessvalueToString( float value )
	{
		if( value<MIN_FLOAT_VALUE ) {
			return "ich bin matt in " + Math.round( value+MAXIMAL_MATT_DEPTH-MIN_FLOAT_VALUE );
		}
		if( value>MAX_FLOAT_VALUE ) {
			return "du bist matt in " + Math.round( MAXIMAL_MATT_DEPTH+MAX_FLOAT_VALUE-value );
		}
		return formater.format( value );
	}
}
