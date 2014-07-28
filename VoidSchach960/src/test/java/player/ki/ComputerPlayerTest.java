package player.ki;

import java.util.LinkedList;
import java.util.List;

import image.FigureImageFactoryMock;
import junit.framework.*;
import board.*;
import helper.*;


/**
 * @author stephan
 */
public class ComputerPlayerTest extends TestCase
{

	public ComputerPlayerTest(String arg0)
	{
		super(arg0);
	}
	
	public void testPlay()
	{
		String des = "black 0 King-white-g1-2 "
					+"Pawn-white-f2-false Bishop-black-e3 Pawn-white-g3-false "
					+"Pawn-white-e4-false "
					+"Bishop-black-b7 "
					+"King-black-g8-2";
		
		ChessGame game = new ChessGame( des );
		game.move( Move.get("e3-f2" ) );
		testTermination( game );
		
		
		game = new ChessGame( new ChessGameSupervisorDummy(),new FigureImageFactoryMock("",true ) );
		game.move( Move.get("c2-c3") );
		testTermination( game );
		
		
		des = "black 0 King-white-h1-3 Pawn-white-c7-false "
			 +"King-black-b7-3-false";
		
		game = new ChessGame( des );
		testTermination( game );
		
		
		des = "black 0 King-white-h1-3 Pawn-white-c7-false "
			 +"Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
			 +"Knight-black-a7 King-black-b7-3-false";
		
		game = new ChessGame( des );
		testTermination( game );
		
		
		game = new ChessGame( 621 );
		game.move( Move.get("f2-f3") );
		testTermination( game );
		
		des = "black 0 Bishop-white-b1 King-white-h1-3 "
			 +"Pawn-black-f7-false King-black-e8-0 Rock-black-f8-0";
		
		game = new ChessGame( des );
		testTermination( game );
		
		des = "white 0 Rock-black-e1-8 "
			 +"Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 "
			 +"Knight-white-g4 Queen-black-e5 King-black-g7-3";
		
		game = new ChessGame( des );
		testTermination( game );
		
		game=new ChessGame( 314 );
		testTermination( game );
		
		game=new ChessGame( 621 );
		game.move( Move.get("g2-g3") );
		game.move( Move.get("f7-f6") );
		game.move( Move.get("c2-c3") );
		game.move( Move.get("g8-b3") );
		game.move( Move.get("d1-c2") );
		testTermination( game );
		
		game=new ChessGame( 707 );
		game.move( Move.get("e1-f3") );
		game.move( Move.get("b7-b6") );
		game.move( Move.get("f1-e3") );
		game.move( Move.get("g7-g6") );
		testTermination( game );
	}
	
	public void testInvarianz()
	{
		String des = "black 0 Rock-white-a1-0 King-white-e1-0 Pawn-white-a5-false "+
								 "Pawn-black-b7-false King-black-e8-0 Rock-black-h8-3";
		ChessGame game = new ChessGame( des );
		game.move( Move.get("b7-b5") );
		testTermination( game );
		String newDes = "white 1 Rock-white-a1-0 King-white-e1-0 Pawn-white-a5-false "+
										"Pawn-black-b5-true King-black-e8-0 Rock-black-h8-3";
		assertEquals( newDes,game.toString() );
	}
	
	public void testTermination( ChessGame game )
	{
		testTermination( game,new SimplePruner( 1,2,2 ) );
	}
		
	public void testTermination( ChessGame game,SearchTreePruner pruner )
	{
		final String initDescription = game.toString();
		DynamicEvaluation dynamicEvaluation  = new DynamicEvaluation( pruner,new StaticEvaluation() );
		
		List<Move> possibleMoves = new LinkedList<Move>();
		game.getPossibleMoves(possibleMoves);
		try {
			for(Move move: possibleMoves) {
				dynamicEvaluation.evaluateMove( game,move );
				//Invariante: evaluateMove darf game nicht ändern
				String msg = "after Move:"+move.toString()+" History:"+game.getHistory();
				assertEquals( msg,initDescription,game.toString() );
			}
		}catch(Exception e) {
			String gamestring = game.toString();
			throw new RuntimeException( e.toString()+"-after Moves:"+game.getHistory()+" -leading to position:"+gamestring );
		}catch( AssertionError e ) {
			AssertionError extendedE = new AssertionError( e.getMessage()+" History:"+game.getHistory() );
			extendedE.setStackTrace( e.getStackTrace() );
			throw extendedE;
		}
	}
	
	public static void main( String[] args )
	{
		//Loadtest
		System.out.println( "Loadtest: Start" );
		
		//Grundaufstellung ohne Bauern
		String des = "white 0 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "+
		"Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 "+
		"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "+
		"Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rock-black-h8-0";
		loadTest( des );
		
		//Grundaufstellung mit Bauern vor König und ohne Läufer
		des = "white 0 Rock-white-a1-0 Knight-white-b1 "+
		"Queen-white-d1 King-white-e1-0 Knight-white-g1 Rock-white-h1-0 "+
		"Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "+
		"Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "+
		"Rock-black-a8-0 Knight-black-b8 "+
		"Queen-black-d8 King-black-e8-0 Knight-black-g8 Rock-black-h8-0";
		//loadTest( des );
		
		//Grundaufstellung mit Bauern vor König und ohne Königsläufer
		des = "white 0 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "+
		"Queen-white-d1 King-white-e1-0 Knight-white-g1 Rock-white-h1-0 "+
		"Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "+
		"Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "+
		"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "+
		"Queen-black-d8 King-black-e8-0 Knight-black-g8 Rock-black-h8-0";
		//loadTest( des );
		
		//Grundaufstellung mit Bauern vor König
		des = "white 0 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "+
		"Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 "+
		"Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "+
		"Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "+
		"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "+
		"Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rock-black-h8-0";
		//loadTest( des );
		
		//Grundaufstellung mit Bauern vor König und ohne Damen
		des = "white 0 Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 "+
		"King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 "+
		"Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "+
		"Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "+
		"Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 "+
		"King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rock-black-h8-0";
		//loadTest( des );
		
//		Zeit von
//		Grundaufstellung mit Bauern vor König und ohne Königsläufer
//		ist in etwa so groß wie
//		Grundaufstellung mit Bauern vor König und ohne Dame!!! Warum?
		System.out.println( "Loadtest: Ende" );
		System.exit( 0 );
	}

	private static void loadTest( String des )
	{
		ChessGame game = new ChessGame( des );
		ComputerPlayerTest computerPlayer = new ComputerPlayerTest( "loadtest" );
		
		try {
			System.out.println( "Loadtest: Berechnung gestartet" );
			long time = System.currentTimeMillis();
			computerPlayer.testTermination( game,new SimplePruner( 2,3,2 ) );
			System.out.println( "Loadtest: Dauer:"+(System.currentTimeMillis()-time)+"ms" );
			RuntimeFacade.printMemoryUsage( "Speicherverbrauch used/total" );
		}catch( RuntimeException e ) {
			System.out.println( "Loadtestfehler:"+e.getMessage() );
		}
	}

}
