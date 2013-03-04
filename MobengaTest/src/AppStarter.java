

import java.io.File;
import java.io.FileInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import spring.config.AppConfig;

import logic.LivescoreProcessor;

/**
 * The main class of this application. The user can choose a xml file either by
 * providing it as a comand line argument or (if no argument was provided) by 
 * selecting it with a file chooser UI. The xml document is than deserialised with
 * XStream into an DTO tree. Then each match of the tree is visited and a list of listerners
 * are informend that represent the actual tasks. The result files are writen into the same
 * directory as the selected xml file.
 * 
 * @author Stephan Schröder
 *
 */
public class AppStarter
{
  /**
   * This method bootstraps the application that is loosly coupled and 
   * tied together by Spring Dependency Injection.
   * 
   * @param args 0 arguments means the user wants a UI to work with the application.
   *             1 argument is expected to be the path (absolute or relative) to an xml file.
   *             In this case the application will assume a command line style.
   */
  public static void main(String[] args)
  {
    //selection/messages over commandline or by UI depending on wheter the user provided
    //a xml file path as argument to the main function
    boolean commandLineMode = true;
    //try to find a xml file from the command line arguments
    File xmlFile = checkCommandLineArgumentsForFile(args);
    if(xmlFile==null) {
      //if no xml file path was provided switch to ui mode and open a file chosser
      //for the user to choose a xml file from
      commandLineMode = false;
      xmlFile = chooseFile();
    }
    
    //init Spring Dependency Injection and instanciate the data processor 
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    LivescoreProcessor processor = ctx.getBean(LivescoreProcessor.class);

    try {
      //process the xml file
      processor.processLivescoreDataXml(
          new FileInputStream(xmlFile),
          xmlFile.getParentFile().getCanonicalPath()
      );
    } catch (Exception e) {
      //if an exection occures show a message and close the application
      exitWithMessage(
          e.toString(),
          commandLineMode,
          1
      );
    }
    //show a success message and close the application
    exitWithMessage(
        "finnished successfully, check generated files",
        commandLineMode,
        0
    );
  }
  
  /**
   * @param args null or one xml file path (else the application stops with an error message)
   * @return null if no xml file path was provided
   */
  private static File checkCommandLineArgumentsForFile(String[] args)
  {
    //if no arguments were provided, no xml file can be found
    if(args==null||args.length==0) return null;
    
    //arguments where provide!
    //make sure it's just one
    if(args.length>1) {
      exitWithMessage(
          "only on argument is expected: the file path to an livescoredata xml file",
          true,
          1
      );
    }
    //make sure the path points to an existing file
    File xmlFile = new File(args[0]);
    if(!xmlFile.exists()) {
      exitWithMessage(
          "file doesn't exist",
          true,
          1
      );
    }
    //make sure it's an xml file
    if(xmlFile.getName().endsWith(".xml")) {
      exitWithMessage(
          "the file you provided is not an xml file",
          true,
          1
      );
    }
    //return the xml file
    return xmlFile;
  }
  
  /**
   * select a single xml file via an FileChooser ui (or end the program)
   * @return a selected xml file
   */
  private static File chooseFile()
  {
    JFileChooser chooser = new JFileChooser(".");
    chooser.setMultiSelectionEnabled(false);
    FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files", "xml");
    chooser.setFileFilter(filter);
    
    //select a file or end the application
    if(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }else{
      System.exit(0);
      //unreachable
      return null;
    }
  }
  
  /**
   * display a message and end the application
   * @param msg message to display before ending the application
   * @param commandLineMode display the message on the command line or via ui
   * @param errorCode error code to end the application with
   */
  private static void exitWithMessage(String msg, boolean commandLineMode, int errorCode)
  {
    if(commandLineMode) {
      System.out.println(msg);
    }else{
      JOptionPane.showMessageDialog(null, msg);
    }
    System.exit(errorCode);
  }
}
