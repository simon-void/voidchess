package image;

/**
 * @author stephan
 */
public interface FigureImageFactory
{
	public FigureImage getKing( boolean isWhite );
	public FigureImage getQueen( boolean isWhite );
	public FigureImage getBishop( boolean isWhite );
	public FigureImage getKnight( boolean isWhite );
	public FigureImage getRock( boolean isWhite );
	public FigureImage getPawn( boolean isWhite );
}