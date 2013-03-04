package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * This class contains some basic IO functionality.
 * @author Stephan Schröder
 */
public class IOUtil
{
  /**
   * @param inStream InputStream to read
   * @return the contents of the Inputsteam, each line is finished by an '\n' no matter
   *         what the original line seperator was
   * @throws IOException
   */
  public StringBuilder readInputStream(InputStream inStream)
  throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
    
    final int available = inStream.available();
    //ok, available doesn't have to return the number
    //of characters in the stream, but often it does.
    //And the StringBuilder can manage if more characters arrive!
    final int expectedBytes = available>0?available:1024;
    StringBuilder content = new StringBuilder(expectedBytes);
    
    String line = null;
    while((line=reader.readLine())!=null) {
      content.append(line);
      content.append('\n');
    }
    
    reader.close();
    
    return content;
  }
  
  /**
   * write the content into a file which is defined by directory and file name
   * @param outputDirPath location of the file to write in
   * @param outputFilename name of the file to write in
   * @param content content of the file to write
   * @throws IOException
   */
  public void write(String outputDirPath, String outputFilename, String content)
  throws IOException
  {
    FileOutputStream outStream = new FileOutputStream(
        new File(outputDirPath,outputFilename));
    write(outStream, content);
  }
  
  /**
   * write content into an OutputStream
   * @param outStream
   * @param content
   * @throws IOException
   */
  public void write(OutputStream outStream, String content)
  throws IOException
  {
    outStream.write(content.getBytes());
    outStream.flush();
    outStream.close();
  }
}
