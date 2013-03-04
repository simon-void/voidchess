package xstream.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A simple DTO XStream uses as part of the Object tree it deserialises LivescoreData XML into.
 * @author Stephan Schröder
 */

@XStreamAlias("Score")
public class ScoreDTO
{
  @XStreamAlias("type")
  @XStreamAsAttribute
  private String type;
  @XStreamAlias("Team1")
  private int pointsOfTeam1;
  @XStreamAlias("Team2")
  private int pointsOfTeam2;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public int getPointsOfTeam1()
  {
    return pointsOfTeam1;
  }

  public void setPointsOfTeam1(int pointsOfTeam1)
  {
    this.pointsOfTeam1 = pointsOfTeam1;
  }

  public int getPointsOfTeam2()
  {
    return pointsOfTeam2;
  }

  public void setPointsOfTeam2(int pointsOfTeam2)
  {
    this.pointsOfTeam2 = pointsOfTeam2;
  }
}
