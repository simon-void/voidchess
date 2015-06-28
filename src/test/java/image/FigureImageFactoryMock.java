package image;

import static org.testng.Assert.assertTrue;

/**
 * @author stephan
 */
public class FigureImageFactoryMock implements FigureImageFactory
{
	private String methodToTest;
	private boolean isWhite;
	private boolean allOk;
	
	private FigureImage figureImage;

	public FigureImageFactoryMock(String testMethod,boolean isWhite)
  {
  	methodToTest = testMethod;
  	this.isWhite = isWhite;
  	allOk        = false;
  	
  	figureImage = new FigureImageMock( 10,20,30 );
  }
  
  public void verify()
  {
  	assertTrue( allOk, "falsche Methode,vermutlich von FigureFactory aufgerufen" );
  }
  
	public FigureImage getKing(boolean isWhite)
	{
		allOk = methodToTest.equals( "King") && isWhite==this.isWhite;
		return figureImage;
	}

	public FigureImage getQueen(boolean isWhite)
	{
		allOk = methodToTest.equals( "Queen") && isWhite==this.isWhite;
		return figureImage;
	}

	public FigureImage getBishop(boolean isWhite)
	{
		allOk = methodToTest.equals( "Bishop") && isWhite==this.isWhite;
		return figureImage;
	}

	public FigureImage getKnight(boolean isWhite)
	{
		allOk = methodToTest.equals( "Knight") && isWhite==this.isWhite;
		return figureImage;
	}

	public FigureImage getRock(boolean isWhite)
	{
		allOk = methodToTest.equals( "Rock") && isWhite==this.isWhite;
		return figureImage;
	}

	public FigureImage getPawn(boolean isWhite)
	{
		allOk = methodToTest.equals( "Pawn") && isWhite==this.isWhite;
		return figureImage;
	}
}
