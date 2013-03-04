package helper;

/**
 * @author stephan
 */
class CompositePositionIterator extends BasicPositionIterator
{
	public void addPositionIterator( PositionIterator iter )
	{
		while( iter.hasNext() ) {
			addPosition( iter.next() );
		}
	}
}
