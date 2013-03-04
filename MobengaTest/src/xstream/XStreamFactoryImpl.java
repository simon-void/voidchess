package xstream;

import xstream.dto.CategoryDTO;
import xstream.dto.LivescoreDataDTO;
import xstream.dto.MatchDTO;
import xstream.dto.NameDTO;
import xstream.dto.ScoreDTO;
import xstream.dto.SportDTO;
import xstream.dto.TeamDTO;
import xstream.dto.TournamentDTO;

import com.thoughtworks.xstream.XStream;

/**
 * Implementation of the {@link XStreamFactory}. Instanciates, configures and
 * provides access to a xstream instance.
 * @author Stephan Schröder
 */
public class XStreamFactoryImpl implements XStreamFactory
{
  /**one configured XStream instance*/
  final private XStream xstream;
  
  public XStreamFactoryImpl()
  {
    //configure one XStream instance to process all DTO classes
    xstream = new XStream();
    xstream.processAnnotations(
      new Class[] {
        CategoryDTO.class,
        LivescoreDataDTO.class,
        MatchDTO.class,
        NameDTO.class,
        ScoreDTO.class,
        SportDTO.class,
        TeamDTO.class,
        TournamentDTO.class
      }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public XStream getLivescoreInstance()
  {
    return xstream;
  }

}
