package image;

import java.awt.Graphics;

import static org.testng.Assert.assertTrue;;

/**
 * @author stephan
 */
public class FigureImageMock implements FigureImage
{
  final public static FigureImageMock defaultInstance = new FigureImageMock( 10,20,30 );
  
	private int intended_x,intended_y,intended_areaSize;
	private boolean allOK;
	
	public FigureImageMock( int intended_x,int intended_y,int intended_areaSize )
  {
  	this.intended_x = intended_x;
  	this.intended_y = intended_y;
  	this.intended_areaSize = intended_areaSize;
  	allOK = false;
  }

	public void paint(Graphics g, int x_pos, int y_pos, int areaSize)
	{
		allOK = x_pos==intended_x && y_pos==intended_y && areaSize==intended_areaSize;
	}
	
	public void verify()
	{
		assertTrue(allOK, "wrong parameter" );
	}

}
