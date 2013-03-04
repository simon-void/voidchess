package helper;

import junit.framework.TestCase;

/**
 * @author stephan
 */
public class CompositePositionIteratorTest extends TestCase
{

	public CompositePositionIteratorTest(String arg0)
	{
		super(arg0);
	}
	
	public void testAddPositionIterator()
	{
		BasicPositionIterator basic         = new BasicPositionIterator();
		basic.addPosition( Position.get("a2") );
		basic.addPosition( Position.get("b2") );
		CompositePositionIterator composite = new CompositePositionIterator();
		composite.addPositionIterator( basic );
		assertEquals( 2,composite.countPositions() );
	}

}
