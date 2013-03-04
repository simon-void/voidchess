package logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import xstream.dto.CategoryDTO;
import xstream.dto.LivescoreDataDTO;
import xstream.dto.MatchDTO;
import xstream.dto.ScoreDTO;
import xstream.dto.SportDTO;
import xstream.dto.TournamentDTO;

/**
 * The class iterates over all matches and informs its listeners
 * about each match with a current score.
 * 
 * @author Stephan Schröder
 */
public class LivescoreProcessor
{
  final private LivescoreXmlDeserialiser xmlDeserialiser;
  private List<MatchScoreListener> matchScoreListeners = new ArrayList<MatchScoreListener>(0);
  
  public LivescoreProcessor(LivescoreXmlDeserialiser xmlDeserialiser)
  {
    this.xmlDeserialiser = xmlDeserialiser;
  }
  
  public void setMatchScoreListener(List<MatchScoreListener> matchScoreListeners)
  {
    if(matchScoreListeners!=null) {
      this.matchScoreListeners = matchScoreListeners;
    }
  }
  
  /**
   * Iterate over all matches and inform listerners about the ones with scores
   * @param xmlStream contains the LivescoreData XML
   * @param outputDirPath where to put result files
   * @throws IOException
   */
  public void processLivescoreDataXml(InputStream xmlStream, String outputDirPath)
  throws IOException
  {
    //transform the LivescoreData XML into an DTO tree
    final LivescoreDataDTO livescore = xmlDeserialiser.deserialise(xmlStream);
    
    //iterate over all matches
    for(SportDTO sport: livescore.getSports()) {
      for(CategoryDTO category: sport.getCategories()) {
        for(TournamentDTO tournament: category.getTournaments()) {
          for(MatchDTO match: tournament.getMatches()) {
            for(ScoreDTO score: match.getScores()) {
              if("Current".equals(score.getType())) {
                //if the match contains a current score, infor all listeners
                informListeners(sport, category, tournament, match, score);
                break;
              }
            }
          }
        }
      }
    }
    
    //inform all listerners that they now now about all matches with a score
    //which triggers also output file generation 
    processNoticedListeners(outputDirPath);
  }
  
  /**
   * inform all listeners about a match with current score
   * @param sport
   * @param category
   * @param tournament
   * @param match
   * @param currentScore
   */
  private void informListeners(
      SportDTO sport, CategoryDTO category,
      TournamentDTO tournament, MatchDTO match, ScoreDTO currentScore)
  {
    for(MatchScoreListener listener: matchScoreListeners) {
      listener.notice(sport, category, tournament, match, currentScore);
    }
  }
  
  /**
   * inform all listeners that they now know about all matches with a score 
   * @param outputDirPath path to the directory the output file should be put in
   * @throws IOException
   */
  private void processNoticedListeners(String outputDirPath)
  throws IOException
  {
    for(MatchScoreListener listener: matchScoreListeners) {
      listener.processNoticed(outputDirPath);
    }
  }
}
