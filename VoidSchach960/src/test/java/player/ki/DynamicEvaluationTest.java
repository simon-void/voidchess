/*
 * Created on 15.10.2006
 */

package player.ki;

import board.*;
import helper.*;
import junit.framework.TestCase;

public class DynamicEvaluationTest
extends TestCase
{
	public void testEvaluateMove()
	{
		String des = "black 0 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false";
		ChessGame game = new ChessGame( des );
		DynamicEvaluation dynamicEvaluation  = new DynamicEvaluation();
		Move move      = Move.get("a6-b6");
		
		dynamicEvaluation.evaluateMove( game,move );
		//Invariante: evaluateMove darf game nicht �ndern
		assertEquals( des,game.toString() );
	}
	
	public void testMinMaxSheme()
	{
		String des = "white 0 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "+
		"Queen-white-d1 King-white-e1-0 Bishop-white-c1 Knight-white-g1 Rock-white-h1-0 "+
		"Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false "+
		"Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false "+
		"Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false "+
		"Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false "+
		"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "+
		"Queen-black-d8 King-black-e8-0 Bishop-black-c8 Knight-black-g8 Rock-black-h8-0";
		
		ChessGame game = new ChessGame( des );
		game.move( Move.get("d2-d3") );
		game.move( Move.get("d7-d6") );
		game.move( Move.get("c1-g5") );
		game.move( Move.get("e7-e6") );	//??Dame kann geschlagen werden
		
		DynamicEvaluation dynamicEvaluation  = new DynamicEvaluation();
		
		float value = dynamicEvaluation.evaluateMove( game,Move.get("g5-d8") );
		
		//der Wert des Zuges sollte um 6 liegen,
		//da ich eine Dame(9P) gegen einen L�ufer(3P) getauscht habe
		//+Wei� kann durch den Damenzug noch Raum gewinnen
		if( value<5.5 ) {
			//Schlagen der K�niging ist der beste Zug
			fail( "Min-Max-Berechnung ist zu niedrig,Soll:~6.5,Ist:"+value );
		}
		if( value>7.5 ) {
			//Schlagen der K�niging ist der beste Zug
			fail( "Min-Max-Berechnung ist zu hoch,Soll:~6.5,Ist:"+value );
		}
	}

}