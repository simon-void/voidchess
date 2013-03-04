package logic;

import java.io.IOException;
import java.io.InputStream;

import xstream.dto.LivescoreDataDTO;

/**
 * Interface to taggle the problem of deserialising the LivescoreData XML into an DTO tree.
 * 
 * @author Stephan Schröder
 */
public interface LivescoreXmlDeserialiser
{
  /**
   * Deserialises the LivescoreData XML into an fully instanciated LivescoreDTO
   * (including it's children, at least the once neccessary to solve the given tasks)
   * @param xmlStream XML InputStream of LivescoreData XML source
   * @return the DTO tree corresponding to the XML
   * @throws IOException
   */
  public LivescoreDataDTO deserialise(InputStream xmlStream) throws IOException;
}
