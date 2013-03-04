package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Not the fastest implementation of a sorted list but it does it's job.
 * A SortedList allows duplicates. Iterating over an SortedList gives you the elements
 * in natural order according to the Comparable implementation of the elements.
 *  
 * @author Stephan Schröder
 *
 * @param <T> what Object to store, T must extend Comparable<T>
 */
public class SortedList<T extends Comparable<T>>
implements Iterable<T>
{
  private SortedMap<T, Integer> sortedMap = new TreeMap<T, Integer>();
  private int size = 0;
  
  /**
   * add an element to the list
   * @param element to add
   */
  public void add(T element)
  {
    Integer count = sortedMap.get(element);
    if(count==null) {
      sortedMap.put(element, 1);
    }else{
      sortedMap.put(element, count+1);
    }
    size++;
  }
  
  /**
   * @return true if the list contains no elements
   */
  public boolean isEmpty()
  {
    return sortedMap.isEmpty();
  }
  
  /**
   * @return the size of the list
   */
  public int size()
  {
    return size;
  }
  
  /**
   * removes all elements from the list
   */
  public void clear()
  {
    sortedMap.clear();
  }
  

  /**
   * @return an iterator which provides sorted iteration over the elements
   */
  @Override
  public Iterator<T> iterator()
  {
    //copy the sorted elements (if neccessary multiple times) into an list 
    List<T> sortedList = new ArrayList<T>(size);
    for(Entry<T, Integer> entry: sortedMap.entrySet()) {
      T element = entry.getKey();
      int count = entry.getValue();
      
      for(int i=0; i<count; i++) {
        sortedList.add(element);
      }
    }
    
    return sortedList.iterator();
  }
}
