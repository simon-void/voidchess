package player.ki;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author stephan
 */
public class InverseValueComperator implements Serializable, Comparator
{

	public int compare(Object o1, Object o2)
	{
		Float first = (Float)o1;
		Float second= (Float)o2;
		
		return second.compareTo( first );
	}

}
