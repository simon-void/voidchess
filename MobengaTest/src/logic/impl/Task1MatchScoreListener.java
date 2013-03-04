package logic.impl;

import java.io.IOException;

import utils.SortedList;
import xstream.dto.CategoryDTO;
import xstream.dto.MatchDTO;
import xstream.dto.ScoreDTO;
import xstream.dto.SportDTO;
import xstream.dto.TournamentDTO;

/**
 * MatchScoreListener implementation that solves task 1.
 * 
 * @author Stephan Schröder
 */
public class Task1MatchScoreListener extends AbstractMatchScoreListener
{
  /**while it is highly unlikly that the result will contain an element
   * more than one time, it's not impossible. So instead of using
   * a SortedSet, let's use a SortedMap that tracks
   * how often an element occured*/
  private SortedList<String> lines = new SortedList<String>();

  /**
   * {@inheritDoc}
   * 
   * build a quick match state representation and
   * sort lines by the alphabetic ordering of the line.
   */
  @Override
  public void notice(SportDTO sport, CategoryDTO category,
      TournamentDTO tournament, MatchDTO match, ScoreDTO currentScore)
  {
    final String SEPERATOR = " | ";
    StringBuilder lineBuilder = new StringBuilder();
    
    lineBuilder.append(getName(sport, LANGUAGE));
    lineBuilder.append(SEPERATOR);
    lineBuilder.append(getName(category, LANGUAGE));
    lineBuilder.append(SEPERATOR);
    lineBuilder.append(getName(tournament, LANGUAGE));
    lineBuilder.append(SEPERATOR);
    lineBuilder.append(getName(match.getTeam1(), LANGUAGE));
    lineBuilder.append(" - ");
    lineBuilder.append(getName(match.getTeam2(), LANGUAGE));
    lineBuilder.append(" : ");
    lineBuilder.append(currentScore.getPointsOfTeam1());
    lineBuilder.append(" - ");
    lineBuilder.append(currentScore.getPointsOfTeam2());
    String line = lineBuilder.toString();
    
    lines.add(line);
  }

  /**
   * {@inheritDoc}
   * 
   * writes its result into big-matchlist-alphasort.txt
   */
  @Override
  public void processNoticed(String outputDirPath)
  throws IOException
  {
    writeLines(outputDirPath, "big-matchlist-alphasort.txt", lines.iterator());
    
    //clear the state for the next invocation
    reset();
  }
  
  /**
   * clear the state for the next invocation
   */
  private void reset()
  {
    lines.clear();
  }
}
