package utils;

import java.text.NumberFormat;

/**
 * this class contains 2 helper methods, one to extend a string by a number so that the natural ordering
 * of strings will sort by decending number and ascending string.
 * The other retransforms the modified string by to the original. 
 * 
 * @author Stephan Schröder
 *
 */
public class DecendingNumberStringSortUtil
{
  private final int max_digits_of_sorting_number;
  private final int max_sorting_number;
  private final NumberFormat intFormater;
  
  /**
   * standard constructor
   * @param max_digits_of_sorting_number the maximum number of digits the "sorting number"
   *        is allowed to have
   */
  public DecendingNumberStringSortUtil(int max_digits_of_sorting_number)
  {
    intFormater = NumberFormat.getIntegerInstance();
    intFormater.setMinimumIntegerDigits(max_digits_of_sorting_number);
    intFormater.setGroupingUsed(false);
    
    this.max_digits_of_sorting_number = max_digits_of_sorting_number;
    //that's the biggest number with max_digits_of_sorting_number digits
    max_sorting_number = (int)Math.pow(10, max_digits_of_sorting_number)-1;
  }
  
  /**
   * a no argument constructor is provided to enable mocking of this class
   */
  public DecendingNumberStringSortUtil()
  {
    //provided that we talk about the number of goals in one game
    //4 digits should be enough
    this(4);
  }
  
  /**
   * add a prefix to prestineString, so that the natural order of the prefixed string
   * sorts by importance first and alphabetical order second 
   * @param prestineString
   * @param importance a higher importance means the prefixed string should be sorted
   *        before an other prefixed string with lower importance.
   *        importance must be a positive integer with at most
   *        max_digits_of_sorting_number (constructor argument) digits.
   * @return importance prefix concatenated with the prestineString
   */
  public String addSortingPrefix(String prestineString, int importance)
  {
    //make sure the max_digits_of_sorting_number constraint is not violated
    if(importance>max_sorting_number) {
      StringBuilder errorBuilder = new StringBuilder(128);
      errorBuilder.append("DecendingNumberStringSortUtil.max_digits_of_sorting_number is too small. ");
      errorBuilder.append("Encountered a sorting number of value: ").append(importance);
      throw new IllegalStateException(errorBuilder.toString());
    }
    if(importance<0) {
      StringBuilder errorBuilder = new StringBuilder(64);
      errorBuilder.append("importance argument mustn't be negative but is: ").append(importance);
      throw new IllegalArgumentException(errorBuilder.toString());
    }
    
    //the prefix contains exactly max_digits_of_sorting_number digits
    String prefix = intFormater.format(max_sorting_number-importance);
    
    //concatenate and return the prefix and the original string
    return prefix+prestineString;
  }
  
  /**
   * @param prefixedString
   * @return removes the importance prefix from the string added by addSortingPrefix
   */
  public String removeSortingPrefix(String prefixedString)
  {
    return prefixedString.substring(max_digits_of_sorting_number);
  }
}
