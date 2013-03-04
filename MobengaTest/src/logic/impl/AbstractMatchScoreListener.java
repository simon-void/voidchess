package logic.impl;

import java.io.IOException;
import java.util.Iterator;

import utils.IOUtil;
import xstream.dto.NameDTO;
import xstream.dto.NamedDTO;
import logic.MatchScoreListener;

/**
 * this abstract super class for MatchScoreListener implementations contains some
 * common helper methods.
 * 
 * @author Stephan Schröder
 */
abstract class AbstractMatchScoreListener implements MatchScoreListener
{
  /** the line seperator to use */
  protected final static String LINE_SEPERATOR = "\n";
  /** the desired language of the names */
  protected final static String LANGUAGE = "en";
  private IOUtil ioUtil;
  
  public void setIoUtil(IOUtil ioUtil)
  {
    this.ioUtil = ioUtil;
  }

  /**
   * write lines into a file.
   * @param outputDirPath directory of the output file
   * @param outputFilename name of the output file
   * @param lines line content of the output file 
   *        (they will be concatenated and seperated by a line seperator)
   * @throws IOException
   */
  protected void writeLines(String outputDirPath, String outputFilename, Iterator<String> lines)
  throws IOException
  {
    StringBuilder content = new StringBuilder();
    //concatenate all lines
    while(lines.hasNext()) {
      content.append(lines.next()).append(LINE_SEPERATOR);
    }
    //remove the last line seperator
    final int length = content.length();
    if(length>0) {
      content.delete(length-LINE_SEPERATOR.length(), length);
    }
    
    //delegate the output file generation to IoUtil 
    ioUtil.write(outputDirPath, outputFilename, content.toString());
  }
  
  /**
   * get the name of a NamedDTO
   * @param named NamedDTO to get name from
   * @param language the language the name should be in
   * @return the name of the NamedDTO
   * @throws IllegalArgumentException if now name in the specified language could be found
   */
  protected String getName(NamedDTO named, String language)
  {
    for(NameDTO name: named.getNames()) {
      if(name.getLanguage().equals(language)) {
        return name.getName();
      }
    }
    throw new IllegalArgumentException(
        "language "+language+" is missing for Element "+
        namedDtoToString(named));
  }
  
  /**
   * serialised a NamedDTO into a human readable String representation
   * @param named
   * @return a human readable String representation of the NamedDTO instance
   */
  private String namedDtoToString(NamedDTO named)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(named.getClass().getSimpleName());
    sb.append('[');
    for(NameDTO name: named.getNames()) {
      sb.append('<').append(name.getLanguage()).append(',');
      sb.append(name.getName()).append('>');
    }
    sb.append(']');
    
    return sb.toString();
  }
}
