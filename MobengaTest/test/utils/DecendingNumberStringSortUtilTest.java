package utils;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * some tests for class DecendingNumberStringSortUtil
 * @author Stephan Schröder
 */
public class DecendingNumberStringSortUtilTest
{
  private DecendingNumberStringSortUtil util;
  
  @BeforeMethod
  public void setup()
  {
    //max_digits_of_sorting_number = 2 -> max_importance = 99
    util = new DecendingNumberStringSortUtil(2);
  }
  
  @Test
  public void testAddPrefix()
  {
    String prefixed = util.addSortingPrefix("a", 0);
    assertEquals(prefixed, "99a");
    
    prefixed = util.addSortingPrefix("a", 99);
    assertEquals(prefixed, "00a");
    
    prefixed = util.addSortingPrefix("a", 4);
    assertEquals(prefixed, "95a");
    
    prefixed = util.addSortingPrefix("a", 10);
    assertEquals(prefixed, "89a");
    
    //make sure that no '.' are inserted into prefixes (like in 123.432.987)
    DecendingNumberStringSortUtil utilWithBigMaxDigits = new DecendingNumberStringSortUtil(7);
    prefixed = utilWithBigMaxDigits.addSortingPrefix("a", 0);
    assertEquals(prefixed, "9999999a");
  }
  
  @Test(expectedExceptions=IllegalStateException.class)
  public void testAddPrefixUpperBound()
  {
    //importance value is one to high
    util.addSortingPrefix("a", 100);
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void testAddPrefixLowerBound()
  {
    //importance value is one to low
    util.addSortingPrefix("a", -1);
  }
  
  @Test
  public void testRemovePrefix()
  {
    String prestine = util.removeSortingPrefix("08a");
    assertEquals(prestine, "a");
  }
  
  /**
   * test whether the ordering acording to importance actually works
   */
  @Test
  public void testSorting()
  {
    Object[][] stringsWithImportance = new Object[][]{
        {"x",   99},
        {"asd",  5},
        {"blkd", 9},
        {"gsd",  0},
        {"x",    5}
    };
    SortedList<String> sortedPrefixed = new SortedList<String>();
    for(int i=0;i<stringsWithImportance.length;i++) {
      String prestine = (String)stringsWithImportance[i][0];
      int importance  = (Integer)stringsWithImportance[i][1];
      sortedPrefixed.add(util.addSortingPrefix(prestine, importance));
    }
    
    List<String> expectedPrestineOrder = Arrays.asList("x", "blkd", "asd", "x", "gsd");
    Iterator<String> actualPrefixOrder = sortedPrefixed.iterator();
    
    for(int i=0; i<stringsWithImportance.length; i++) {
      String expected = expectedPrestineOrder.get(i);
      String actual   = util.removeSortingPrefix(actualPrefixOrder.next());
      assertEquals(actual, expected);
    }
  }
}
