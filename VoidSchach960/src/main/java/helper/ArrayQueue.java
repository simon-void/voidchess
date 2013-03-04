/*
 * Created on 03.10.2006
 */

package helper;

import java.util.NoSuchElementException;

public class ArrayQueue
{
	private Object[] content;
	private int firstindex,lastindex;
	
	public ArrayQueue( int initialsize )
	{
		if( initialsize<1 ) throw new IllegalArgumentException("initial size must be greater than 0" );
		
		content = new Object[initialsize];
		clear();
	}

	public void clear()
	{
		firstindex   = 0;
		lastindex    = 0;
	}
	
	public void putLast( Object obj )
	{
		ensureNoBounds();
		content[lastindex++]=obj;
	}
	
	public Object popFirst()
	{
		if( firstindex==lastindex ) throw new NoSuchElementException();
		return content[firstindex++];
	}
	
	public int size()
	{
		return lastindex-firstindex;
	}
	
	public boolean contains( Object other )
	{
		for( int i=firstindex;i<lastindex;i++ ) {
			if( content[i].equals( other ) ) return true;
		}
		return false;
	}
	
	private void ensureNoBounds()
	{
		if( lastindex==content.length ) {
			final int size = size();
			if( firstindex!=0 && firstindex>=Math.sqrt( content.length ) ) {
				//shiftLeft();
				System.arraycopy( content,firstindex,content,0,size );
			}else {
				//doubleArrayLength();
				Object[] newcontent = new Object[ content.length*2 ];
				System.arraycopy( content,firstindex,newcontent,0,size );
				content = newcontent;
			}
			firstindex = 0;
			lastindex  = size;
		}
	}
}
