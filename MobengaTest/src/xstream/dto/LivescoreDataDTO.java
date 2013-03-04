package xstream.dto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A simple DTO XStream uses as part of the Object tree it deserialises LivescoreData XML into.
 * @author Stephan Schröder
 */

@XStreamAlias("LivescoreData")
public class LivescoreDataDTO
{
  @XStreamImplicit
  private List<SportDTO> sports = new LinkedList<SportDTO>();

  public void add(SportDTO sport)
  {
    sports.add(sport);
  }

  public List<SportDTO> getSports()
  {
    return Collections.unmodifiableList(sports);
  }
}
