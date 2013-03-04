package logic.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.DecendingNumberStringSortUtil;
import utils.SortedList;
import xstream.dto.CategoryDTO;
import xstream.dto.MatchDTO;
import xstream.dto.ScoreDTO;
import xstream.dto.SportDTO;
import xstream.dto.TeamDTO;
import xstream.dto.TournamentDTO;

/**
 * MatchScoreListener implementation that solves task 3.
 * 
 * @author Stephan Schröder
 */
public class Task3MatchScoreListener extends AbstractMatchScoreListener
{
  private Map<Character, Integer> letterMap = new HashMap<Character, Integer>(); 
  private DecendingNumberStringSortUtil sortPrefixUtil;
  
  /**
   * {@inheritDoc}
   * 
   * look for the first letter of Team names and add how many goals they did
   */
  @Override
  public void notice(SportDTO sport, CategoryDTO category,
      TournamentDTO tournament, MatchDTO match, ScoreDTO currentScore)
  {
    addTeamStartingWithGoals(match.getTeam1(), currentScore.getPointsOfTeam1());
    addTeamStartingWithGoals(match.getTeam2(), currentScore.getPointsOfTeam2());
  }

  /**
   * {@inheritDoc}
   * 
   * writes its result into big-toplist.txt
   */
  @Override
  public void processNoticed(String outputDirPath) throws IOException
  {
    SortedList<String> linesWithGoals = new SortedList<String>();
    for(Entry<Character, Integer> entry: letterMap.entrySet()) {
      char letter = entry.getKey();
      int goalSum = entry.getValue();
      
      StringBuilder lineBuilder = new StringBuilder(6);
      lineBuilder.append(letter).append(": ").append(goalSum);
      
      String lineWithGoalSum = sortPrefixUtil.addSortingPrefix(lineBuilder.toString(), goalSum);
      linesWithGoals.add(lineWithGoalSum);
    }
    
    List<String> lines = new ArrayList<String>();
    for(String lineWithGoalSum: linesWithGoals) {
      //the goal sum is removed from the line
      String line = sortPrefixUtil.removeSortingPrefix(lineWithGoalSum);
      //before it gets added to lines
      lines.add(line);
    }
    
    //write the lines
    writeLines(outputDirPath, "big-toplist.txt", lines.iterator());
    
    //clear the state for the next invocation
    reset();
  }
  
  /**
   * Add the first letter of the team name and the goals, if goals is bigger than 0.
   * @param team team that scored goals
   * @param goals number of goals
   */
  private void addTeamStartingWithGoals(TeamDTO team, Integer goals)
  {
    //only save goals if at least one goals occurred
    if(goals<1) return;
    
    final Character letter = getName(team,LANGUAGE).charAt(0);
    final Integer oldGoals = letterMap.get(letter);
    if(oldGoals==null) {
      letterMap.put(letter, goals);
    }else{
      letterMap.put(letter, goals+oldGoals);
    }
  }

  /**
   * clear the state for the next invocation
   */
  private void reset()
  {
    letterMap.clear();
  }
  
  public void setSortPrefixUtil(DecendingNumberStringSortUtil sortPrefixUtil)
  {
    this.sortPrefixUtil = sortPrefixUtil;
  }
}
