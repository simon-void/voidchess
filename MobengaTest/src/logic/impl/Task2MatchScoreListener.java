package logic.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.DecendingNumberStringSortUtil;
import utils.SortedList;
import xstream.dto.CategoryDTO;
import xstream.dto.MatchDTO;
import xstream.dto.ScoreDTO;
import xstream.dto.SportDTO;
import xstream.dto.TournamentDTO;

/**
 * MatchScoreListener implementation that solves task 2.
 * 
 * @author Stephan Schröder
 */
public class Task2MatchScoreListener extends AbstractMatchScoreListener
{
  /**
   * while it is highly unlikly that the result will contain an element
   * more than one time, it's not impossible. So instead of using
   * a SortedSet, let's use a SortedMap that tracks
   * how often an element occured*/
  private SortedList<String> linesWithGoals = new SortedList<String>();
  private DecendingNumberStringSortUtil sortPrefixUtil;

  /**
   * {@inheritDoc}
   * 
   * sort lines first by the number of goals in the match and
   * secondary by the alphabetic ordering of the line.
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
    
    final int goal_sum = currentScore.getPointsOfTeam1()+currentScore.getPointsOfTeam2();
    final String prefixedLine = sortPrefixUtil.addSortingPrefix(lineBuilder.toString(), goal_sum);
    String line = prefixedLine;
    linesWithGoals.add(line);
  }

  /**
   * {@inheritDoc}
   * 
   * writes its result into big-matchlist-goalasort.txt
   */
  @Override
  public void processNoticed(String outputDirPath)
  throws IOException
  {
    List<String> lines = new ArrayList<String>();
    for(String lineWithGoalSum: linesWithGoals) {
      //the goal sum is removed from the line
      String line = sortPrefixUtil.removeSortingPrefix(lineWithGoalSum);
      //before it gets added to lines
      lines.add(line);
    }
    
    //write the lines
    writeLines(outputDirPath, "big-matchlist-goalsort.txt", lines.iterator());
    
    //clear the state for the next invocation
    reset();
  }
  
  /**
   * clear the state for the next invocation
   */
  private void reset()
  {
    linesWithGoals.clear();
  }
  
  public void setSortPrefixUtil(DecendingNumberStringSortUtil sortPrefixUtil)
  {
    this.sortPrefixUtil = sortPrefixUtil;
  }
}
