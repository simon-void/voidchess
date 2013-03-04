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
		return getFigureByName(isWhite, "KING");
	}

	public FigureImage getQueen( boolean isWhite)
	{
		return getFigureByName(isWhite, "QUEEN");
	}

	public FigureImage getBishop( boolean isWhite)
	{
		return getFigureByName(isWhite, "BISHOP");
	}

	public FigureImage getKnight( boolean isWhite)
	{
		return getFigureByName(isWhite, "KNIGHT");
	}

	public FigureImage getRock( boolean isWhite)
	{
		return getFigureByName(isWhite, "ROCK");
	}

	public FigureImage getPawn( boolean isWhite)
	{
		return getFigureByName(isWhite, "PAWN");
	}
	
	private FigureImage getFigureByName(boolean isWhite, String figureName)
	{
	  final StringBuilder imgTypB = new StringBuilder(figureName.length()+2);
		if(isWhite) {
		  imgTypB.append('W');
		}else{
		  imgTypB.append('B');
		}
		imgTypB.append('_').append(figureName);
		final ImageType imageType = ImageType.valueOf(imgTypB.toString());
		
		return new FigureFileImage( imageObserver,imageType );
	}
	
	final class FigureFileImage implements FigureImage
	{
		private final ImageObserver observer;
		private final Image image;
	
		private FigureFileImage( ImageObserver observer,ImageType imageType)
		{
			this.observer = observer;
			image         = Images.get( imageType );
		}

		public void paint(Graphics g, int x_pos, int y_pos, int areaSize)
		{
			g.drawImage( image,x_pos,y_pos,areaSize,areaSize,observer );
		}

	}

}
