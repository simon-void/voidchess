package xstream.dto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import xstream.dto.converter.CalendarConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * A simple DTO XStream uses as part of the Object tree it deserialises LivescoreData XML into.
 * @author Stephan Schröder
 */

@XStreamAlias("Match")
public class MatchDTO
{
  @XStreamAlias("MatchId")
  @XStreamAsAttribute
  private long id;
  @XStreamAlias("MatchDate")
  @XStreamConverter(CalendarConverter.class)
  private Calendar date;
  @XStreamAlias("Scores")
  private List<ScoreDTO> scores = new ArrayList<ScoreDTO>(4);
  @XStreamAlias("Team1")
  private TeamDTO team1;
  @XStreamAlias("Team2")
  private TeamDTO team2;
  
  public long getId()
  {
    return id;
  }
  
  public void setId(long id)
  {
    this.id = id;
  }
  
  public Calendar getDate()
  {
    return date;
  }
  
  public void setDate(Calendar date)
  {
    this.date = date;
  }
  
  public TeamDTO getTeam1()
  {
    return team1;
  }
  
  public void setTeam1(TeamDTO team1)
  {
    this.team1 = team1;
  }
  
  public TeamDTO getTeam2()
  {
    return team2;
  }
  
  public void setTeam2(TeamDTO team2)
  {
    this.team2 = team2;
  }
  
  public void add(ScoreDTO score)
  {
    scores.add(score);
  }

  public List<ScoreDTO> getScores()
  {
    return Collections.unmodifiableList(scores);
  }
}
