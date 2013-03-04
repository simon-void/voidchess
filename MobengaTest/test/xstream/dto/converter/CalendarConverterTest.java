package xstream.dto.converter;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import xstream.dto.converter.CalendarConverter;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * some tests for class CalendarConverter. Mockito helps with mocking.
 * @author Stephan Schröder
 */
public class CalendarConverterTest
{

  private CalendarConverter converter;

  @BeforeMethod
  public void setup()
  {
    converter = new CalendarConverter();
  }

  @Test
  public void testCalendarToString()
  {
    GregorianCalendar cal = new GregorianCalendar(
        2001, Calendar.DECEMBER, 24, 18, 30, 55);

    String date = converter.calendarToString(cal);

    assertEquals(date, "2001-12-24T18:30:55", "date format");
  }

  @Test
  public void testConversionCycle()
  {
    final String date = "2006-01-05T09:05:00";

    Calendar cal = converter.stringToCalendar(date);

    final String retrievedDate = converter.calendarToString(cal);

    assertEquals(retrievedDate, date);
  }

  @Test
  public void testCanConvert()
  {
    assertTrue(converter.canConvert(GregorianCalendar.class),
        "should convert GregorianCalendar");
    assertTrue(converter.canConvert(Calendar.class), "should convert Calendar");
    assertFalse(converter.canConvert(Object.class), "should convert Object");
  }

  @Test
  public void testMarshal()
  {
    final GregorianCalendar cal = new GregorianCalendar(
        2001, Calendar.DECEMBER, 1, 8, 3, 5);

    HierarchicalStreamWriter writerMock = mock(HierarchicalStreamWriter.class);

    converter.marshal(cal, writerMock, null);

    verify(writerMock).setValue("2001-12-01T08:03:05");
  }

  @Test
  public void testUnmarshal()
  {
    HierarchicalStreamReader readerMock = mock(HierarchicalStreamReader.class);
    when(readerMock.getValue()).thenReturn("2001-12-01T08:03:05");

    final Calendar actualCal = (Calendar) converter.unmarshal(readerMock, null);
    final Calendar expectedCal = new GregorianCalendar(
        2001, Calendar.DECEMBER, 1, 8, 3, 5);
    assertEquals(actualCal, expectedCal);
  }
}
