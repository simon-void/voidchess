package logic.impl;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

import utils.IOUtil;
import xstream.XStreamFactory;

/**
 * some tests for class LivescoreXmlDeserialiserDefaultImpl. Mockito helps with mocking.
 * @author Stephan Schröder
 */
public class LivescoreXmlDeserialiserDefaultImplTest
{
  private LivescoreXmlDeserialiserDefaultImpl deserialiser;
  private IOUtil ioUtilMock;
  private XStream xstreamMock;
  
  @BeforeMethod
  public void setup()
  {
    ioUtilMock = mock(IOUtil.class);
    xstreamMock = mock(XStream.class);
    XStreamFactory xstreamFactoryMock = mock(XStreamFactory.class);
    when(xstreamFactoryMock.getLivescoreInstance()).thenReturn(xstreamMock);
    
    deserialiser = new LivescoreXmlDeserialiserDefaultImpl(ioUtilMock, xstreamFactoryMock);
  }
  
  @Test
  public void testPurgeSimpleTag()
  {
    StringBuilder initXML = new StringBuilder();
    initXML.append("<Start>");
    initXML.append("<Status><Content>delete this</Content></Status>");
    initXML.append("</Start>");
    
    String expectedPurged = "<Start></Start>";
    String actualPurged = deserialiser.purgeUnsupportedTags(initXML, "Status");
    
    assertEquals(actualPurged, expectedPurged);
  }
  
  @Test
  public void testPurgeSimpleEmptyTag()
  {
    StringBuilder initXML = new StringBuilder("<Start><Winner/></Start>");
    
    String expectedPurged = "<Start></Start>";
    String actualPurged = deserialiser.purgeUnsupportedTags(initXML, "Winner");
    
    assertEquals(actualPurged, expectedPurged);
  }
  
  @Test(dependsOnMethods={"testPurgeSimpleTag", "testPurgeSimpleEmptyTag"})
  public void testPurgeComplexXml()
  {
    StringBuilder initXML = new StringBuilder();
    initXML.append("<Start>");
    initXML.append("<Status><Content>delete this</Content></Status>");
    initXML.append("<Test1/><Winner/><Test2 /><Lineups number=3 />");
    initXML.append("</Start>");
    
    String expectedPurged = "<Start><Test1/><Test2 /></Start>";
    String actualPurged = deserialiser.purgeUnsupportedTags(initXML, "Status", "Winner", "Lineups");
    
    assertEquals(actualPurged, expectedPurged);
  }
}
