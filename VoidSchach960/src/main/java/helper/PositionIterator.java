package helper;

/**
 * @author stephan
 */
public interface PositionIterator
{
	public Position next();
	public boolean hasNext();
	public int countPositions();
	public boolean isEmpty();
	public boolean contains( Position other );
}