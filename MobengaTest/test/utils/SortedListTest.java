package utils;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

/**
 * a test for class SortedList
 * @author Stephan Schröder
 */
public class SortedListTest
{
  /**
   * test whether the ordering with multiple occurences of the same element actually works
   */
  @Test
  public void testSorting()
  {
    String[] strings = {"x", "asd", "blkd", "gsd", "x"};
    SortedList<String> sorted = new SortedList<String>();
    for(int i=0;i<strings.length;i++) {
      //add strings to SortedList
      sorted.add(strings[i]);
    }
    
    //expected order is sorted
    List<String> expectedOrder = Arrays.asList("asd", "blkd", "gsd", "x", "x");
    //hopefully the actual order, too
    Iterator<String> actualOrder = sorted.iterator();
    
    //let's find out
    for(int i=0; i<strings.length; i++) {
      String expected = expectedOrder.get(i);
      String actual   = actualOrder.next();
      assertEquals(actual, expected);
    }
  }
}
