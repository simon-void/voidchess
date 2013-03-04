package logic.impl;

import java.io.IOException;
import java.io.InputStream;

import com.thoughtworks.xstream.XStream;

import utils.IOUtil;
import xstream.XStreamFactory;
import xstream.dto.LivescoreDataDTO;
import logic.LivescoreXmlDeserialiser;

/**
 * The implementations deserialises the LivescoreData XML into a DTO tree representation
 * to enable easier access.
 *  
 * @author Stephan Schröder
 */

public class LivescoreXmlDeserialiserDefaultImpl
implements LivescoreXmlDeserialiser
{
  private final IOUtil ioUtil;
  final XStream xstream;
  
  public LivescoreXmlDeserialiserDefaultImpl(IOUtil ioUtil, XStreamFactory xStreamFactory)
  {
    this.ioUtil = ioUtil;
    this.xstream = xStreamFactory.getLivescoreInstance();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LivescoreDataDTO deserialise(InputStream xmlStream)
  throws IOException
  {
    //read the XML from the InputStream
    final StringBuilder xmlBuilder = ioUtil.readInputStream(xmlStream);
    //get rid of all xml elements that i didn't write a DTO for
    //(or else XStream will complain about it)
    final String finalXml = purgeUnsupportedTags(
        xmlBuilder,
        "Status", "Winner", "LastGoal", "Goals", "Cards",
        "Substitutions", "Lineups", "CurrentPeriodStart");
    //create a DTO tree from this reduced LivescoreData XML
    final LivescoreDataDTO livescore = (LivescoreDataDTO)xstream.fromXML(finalXml);
    //and return it
    return livescore;
  }
  
  /**
   * this method is public to allow for better testing.
   * @param xmlContent normal LivescoreData xml file content
   * @param unsupportedTags xml tags that should be removed (because there is not DTO for it) 
   * @return clean LivescoreData xml that can be deserialised by XStream 
   */
  public String purgeUnsupportedTags(StringBuilder xmlContent, String... unsupportedTags)
  {
    for(String unsupportedTag: unsupportedTags) {
      //look for all occurrences of unsupported xml tags in the xml 
      final String tagstart = '<' + unsupportedTag;
      final String tagend = new StringBuilder(unsupportedTag.length()+3)
                                .append("</")
                                .append(unsupportedTag)
                                .append('>')
                                .toString();
      
      int startIndex = xmlContent.indexOf(tagstart);
      while(startIndex!=-1) {
        //tags can be closed by '/>'
        int endIndex = getEmptyTagEnd(xmlContent, startIndex);
        if( endIndex==-1) {
          //or by '</...>
          endIndex = xmlContent.indexOf(tagend, startIndex);
          //for every start tag there should be an end tag
          if(endIndex==-1) {
            throw new IllegalArgumentException("xml lacks closing tag for tag: "+unsupportedTag);
          }
          //remove the whole closing tag
          endIndex+=tagend.length();
        }
        
        //delete the unsupportedTag
        xmlContent.delete(startIndex, endIndex);
        //check if there is a further use of this tag
        startIndex = xmlContent.indexOf(tagstart, startIndex);
      }
    }
    
    return xmlContent.toString();
  }
  
  /**
   * check if the current tag is ended by '/>'
   * @param xmlContent xml content
   * @param startIndex index of the corresponding opening '<...'
   * @return endInded for neccessary substring call or -1 if tag is not ended by '/>'
   */
  private int getEmptyTagEnd(StringBuilder xmlContent, int startIndex)
  {
    int emptyTagEndIndex = xmlContent.indexOf("/>", startIndex);
    if(emptyTagEndIndex==-1) return -1;
    int tagEndIndex = xmlContent.indexOf(">", startIndex);
    
    if(tagEndIndex==emptyTagEndIndex+1) return tagEndIndex+1;
    else return -1;
  }
}
