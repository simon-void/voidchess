package player;

import helper.Position;
/**
 * @author stephan
 */
public interface PlayerInterface
{
	public void setIsPlaying(boolean isPlaying);
	public void play();
	public String askForPawnChange( Position pawnPosition );
	public void setColor( boolean isWhite );
}
