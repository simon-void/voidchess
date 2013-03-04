package logic;

import java.io.IOException;

import xstream.dto.CategoryDTO;
import xstream.dto.MatchDTO;
import xstream.dto.ScoreDTO;
import xstream.dto.SportDTO;
import xstream.dto.TournamentDTO;

/**
 * A Listener class that collects information about all matches (with a current score element)
 * that the {@link LivescoreProcessor} encounters.
 * Implementations of this interface implement the functionallity specific to the three tasks
 * this application is written to perform.
 *  
 * @author Stephan Schröder
 */
public interface MatchScoreListener
{
  /**
   * notice this scored match and it's "path" in the object tree
   * derived from the LivescoreData XML
   * @param sport
   * @param category
   * @param tournament
   * @param match
   * @param currentScore
   */
  public void notice(
      SportDTO sport,
      CategoryDTO category,
      TournamentDTO tournament,
      MatchDTO match,
      ScoreDTO currentScore);
  
  /**
   * Informs the listener that it now knows about all matches.
   * It is assumed that each implementation transforms the information provided up to this point
   * and writes its result into an output file.   
   * @param outputDirPath the directory to write the output file into
   * @throws IOException
   */
  public void processNoticed(String outputDirPath) throws IOException;
}
