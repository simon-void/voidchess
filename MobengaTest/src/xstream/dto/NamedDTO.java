package xstream.dto;

import java.util.List;


/**
 * A interface for all DTO that contain a list of NameDTOs.
 * @author Stephan Schröder
 */

public interface NamedDTO
{
  /**
   * @return the list of names (in different languages) of this DTO
   */
  public List<NameDTO> getNames();
}
