package xstream.dto.converter;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * a special XStream converter that transforms a {@link java.util.Calendar} from and into the
 * specific time format found in the XML 
 * (that for some reason DOESN'T PROVIDE TIME ZONE INFORMATION!).
 * 
 * @author Stephan Schröder
 */
public class CalendarConverter implements Converter
{
  /**
   * {@inheritDoc}
   */
  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer,
      MarshallingContext context)
  {

    Calendar calendar = (Calendar) source;
    writer.setValue(calendarToString(calendar));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object unmarshal(HierarchicalStreamReader reader,
      UnmarshallingContext context)
  {

    String date = reader.getValue();
    Calendar calendar = stringToCalendar(date);

    return calendar;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({
    "rawtypes"
  })
  @Override
  public boolean canConvert(Class type)
  {
    return Calendar.class.isAssignableFrom(type);
  }

  /**
   * this method is public to make easy testing possible.
   * @param calendar to serialise into string
   * @return a string representation of the calendar
   */
  public String calendarToString(Calendar calendar)
  {
    NumberFormat formater = NumberFormat.getIntegerInstance();
    formater.setMinimumIntegerDigits(2);
    formater.setGroupingUsed(false);

    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH) + 1;
    final int day = calendar.get(Calendar.DAY_OF_MONTH);
    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
    final int minute = calendar.get(Calendar.MINUTE);
    final int sec = calendar.get(Calendar.SECOND);

    // generate "2011-01-05T07:30:00"
    StringBuilder dateBuilder = new StringBuilder(19);
    dateBuilder.append(year);
    dateBuilder.append('-');
    dateBuilder.append(formater.format(month));
    dateBuilder.append('-');
    dateBuilder.append(formater.format(day));
    dateBuilder.append('T');
    dateBuilder.append(formater.format(hour));
    dateBuilder.append(':');
    dateBuilder.append(formater.format(minute));
    dateBuilder.append(':');
    dateBuilder.append(formater.format(sec));

    return dateBuilder.toString();
  }

  /**
   * this method is public to make easy testing possible.
   * @param string to deserialise into a calendar
   * @return the calendar that corresponds to the string
   */
  public Calendar stringToCalendar(String date)
  {
    // reconstruct from "2011-01-05T07:30:00"
    StringTokenizer dateTokenizer = new StringTokenizer(date, "-T:", false);
    final int year = Integer.parseInt(dateTokenizer.nextToken());
    final int month = Integer.parseInt(dateTokenizer.nextToken()) - 1;
    final int day = Integer.parseInt(dateTokenizer.nextToken());
    final int hour = Integer.parseInt(dateTokenizer.nextToken());
    final int minute = Integer.parseInt(dateTokenizer.nextToken());
    final int sec = Integer.parseInt(dateTokenizer.nextToken());

    GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour,
        minute, sec);

    return calendar;
  }
}
