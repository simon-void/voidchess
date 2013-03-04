package xstream.dto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A simple DTO XStream uses as part of the Object tree it deserialises LivescoreData XML into.
 * @author Stephan Schröder
 */

@XStreamAlias("Category")
public class CategoryDTO
implements NamedDTO
{
  @XStreamAlias("CategoryId")
  @XStreamAsAttribute
  private long id;
  @XStreamImplicit
  private List<NameDTO> names = new LinkedList<NameDTO>();
  @XStreamImplicit
  private List<TournamentDTO> tournaments = new LinkedList<TournamentDTO>();

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public void add(NameDTO name)
  {
    names.add(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<NameDTO> getNames()
  {
    return Collections.unmodifiableList(names);
  }
  
  public void add(TournamentDTO tournament)
  {
    tournaments.add(tournament);
  }

  public List<TournamentDTO> getTournaments()
  {
    return Collections.unmodifiableList(tournaments);
  }
}
