package helper;

/**
 * @author stephan
 */
public interface MoveIterator
{
	public boolean hasMoreMoves();
	public Move nextMove();
	public int totalNumberOfMoves();
	public boolean isEmpty();
}
