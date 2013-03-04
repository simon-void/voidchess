/*
 * Created on 03.10.2006
 */

package helper;

import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class ArrayQueueTest
extends TestCase
{
	private ArrayQueue queue;

	protected void setUp() throws Exception
	{
		queue = new ArrayQueue(1);
	}
	
	public void testAll()
	{
		Integer i1 = new Integer( 1 );
		Integer i2 = new Integer( 2 );
		
		assertTrue( queue.size()==0 );
		assertFalse( queue.contains( i1 ) );
		queue.putLast( i1 );
		assertTrue( queue.contains( i1 ) );
		assertTrue( queue.size()==1 );
		queue.putLast( i2 );
		assertTrue( queue.size()==2 );
		assertTrue( queue.popFirst()==i1 );
		assertFalse( queue.contains( i1 ) );
		assertTrue( queue.size()==1 );
		assertTrue( queue.popFirst()==i2 );
		assertTrue( queue.size()==0 );
		
		queue.putLast( i1 );
		queue.putLast( i1 );
		queue.putLast( i1 );
		queue.putLast( i1 );
		assertTrue( queue.size()==4 );
		queue.clear();
		assertTrue( queue.size()==0 );
		assertFalse( queue.contains( i1 ) );
		
		try {
			queue.popFirst();
			fail( "NoSuchElement should have been thrown" );
		}catch( NoSuchElementException e) {}
	}

}
