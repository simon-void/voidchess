package image;

import image.Images;
import java.awt.image.ImageObserver;
import java.awt.*;

/**
 * @author stephan
 */
public class FileFigureImageFactory implements FigureImageFactory
{
	private ImageObserver imageObserver;
	
	public FileFigureImageFactory( ImageObserver imageObserver )
	{
		this.imageObserver = imageObserver;
	}
	
	public FigureImage getKing( boolean isWhite)
	{
		return getFigureByName(isWhite, "_KÖNIG");
	}

	public FigureImage getQueen( boolean isWhite)
	{
		return getFigureByName(isWhite, "_DAME");
	}

	public FigureImage getBishop( boolean isWhite)
	{
		return getFigureByName(isWhite, "_LÄUFER");
	}

	public FigureImage getKnight( boolean isWhite)
	{
		return getFigureByName(isWhite, "_PFERD");
	}

	public FigureImage getRock( boolean isWhite)
	{
		return getFigureByName(isWhite, "_TURM");
	}

	public FigureImage getPawn( boolean isWhite)
	{
		return getFigureByName(isWhite, "_BAUER");
	}
	
	private FigureImage getFigureByName(boolean isWhite, String figureName)
	{
		if(isWhite) {
			figureName="W"+figureName;
		}else{
			figureName="S"+figureName;
		}
		return new FigureFileImage( imageObserver,figureName );
	}
	
	final class FigureFileImage implements FigureImage
	{
		private final ImageObserver observer;
		private final Image image;
	
		private FigureFileImage( ImageObserver observer,String imageName)
		{
			this.observer = observer;
			image         = Images.get( imageName );
		}

		public void paint(Graphics g, int x_pos, int y_pos, int areaSize)
		{
			g.drawImage( image,x_pos,y_pos,areaSize,areaSize,observer );
		}

	}

}
