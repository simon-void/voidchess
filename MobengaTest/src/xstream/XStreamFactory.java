package xstream;

import com.thoughtworks.xstream.XStream;

/**
 * provides a Factory interface to retrieve a configured XSteam instance from.
 * @author Stephan Schröder
 *
 */
public interface XStreamFactory
{
  /**
   * @return a configured XStream instance ready to deserialise livescore XML.
   */
  public XStream getLivescoreInstance();
}
