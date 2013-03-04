package xstream.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * A simple DTO XStream uses as part of the Object tree it deserialises LivescoreData XML into.
 * @author Stephan Schröder
 */

@XStreamAlias("Name")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {
  "name"
})
public class NameDTO
{
  @XStreamAsAttribute
  private String language;
  private String name;

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }
}
