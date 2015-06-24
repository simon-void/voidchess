package image;

import java.awt.Graphics;
import junit.framework.Assert;

/**
 * @author stephan
 */
public class FigureImageMock implements FigureImage
{
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
		Assert.assertTrue("wrong parameter",allOK );
	}

}
