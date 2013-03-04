package player.ki;

import junit.framework.TestCase;

/**
 * @author stephan
 */
public class InverseValueComperatorTest extends TestCase
{
	private InverseValueComperator comp;

	public InverseValueComperatorTest(String arg0)
	{
		super(arg0);
	}

	protected void setUp() throws Exception
	{
		comp = new InverseValueComperator();
	}
	
	public void testCompare()
	{
		Float first = new Float( 1.2 );
		Float second= new Float(-2.3 );
		
		assertTrue( comp.compare( first,second)==-1 );
		assertTrue( comp.compare( second,first)== 1 );
		assertTrue( comp.compare( first,first)==  0 );
	}

}
