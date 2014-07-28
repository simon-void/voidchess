package player.ki;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author stephan
 */
public class InverseValueComperator implements Serializable, Comparator<Float>
{

	public int compare(Float first, Float second)
	{		
		return second.compareTo( first );
	}

}
