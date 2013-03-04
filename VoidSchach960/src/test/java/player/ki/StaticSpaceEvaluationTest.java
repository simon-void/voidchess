/*
 * Created on 08.11.2006
 */

package player.ki;

import helper.Position;

import java.util.*;

import board.ChessGame;
import junit.framework.TestCase;

public class StaticSpaceEvaluationTest
extends TestCase
{
	StaticSpaceEvaluation evaluation = new StaticSpaceEvaluation();
	
	public void testEvaluation()
	{
		String des = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		
		float evaluateForWhite = evaluation.evaluate(game,true);
		float evaluateForBlack = evaluation.evaluate(game,false);
		
		assertTrue( evaluateForWhite < 0 );
		assertTrue( evaluateForBlack > 0 );
		assertTrue( evaluateForBlack == -evaluateForWhite );
		
		
		des = "black 0 King-white-e1-0 Rock-black-b8-0 King-black-e8-0";
		ChessGame game2 = new ChessGame( des );
		
		float evaluateForWhite2 = evaluation.evaluate(game2,true);
		assertTrue( evaluateForWhite2 < evaluateForWhite );
	}
	
	public void testGetMaxFigureDistance()
	{
		assertEquals( 7,evaluation.getKingDistance( Position.get( "e1" ),Position.get( "e8" ) ) );
		assertEquals(  4,evaluation.getKingDistance( Position.get( "b7" ),Position.get( "e8" ) ) );
	}
	
	public void testCountRestSpace()
	{
		String des = "black 0 King-white-e1-0 Rock-black-a8-0 King-black-e8-0";
		ChessGame game = new ChessGame( des );
		
		List otherPositions = new LinkedList();
		otherPositions.add( Position.get( "e8" ) );
		otherPositions.add( Position.get( "a8" ) );
		Position kingPos = Position.get( "e1" );
		
		assertEquals( 48,evaluation.countRestSpace( game,kingPos,otherPositions ) );
		
		des = "black 0 King-white-e1-0 Rock-black-e7-0 King-black-e8-0";
		game = new ChessGame( des );
		
		otherPositions.clear();
		otherPositions.add( Position.get( "e8" ) );
		otherPositions.add( Position.get( "e7" ) );
		
		assertEquals( 43,evaluation.countRestSpace( game,kingPos,otherPositions ) );
	}
}
