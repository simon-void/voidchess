package spring.config;

import java.util.Arrays;

import logic.LivescoreProcessor;
import logic.LivescoreXmlDeserialiser;
import logic.MatchScoreListener;
import logic.impl.LivescoreXmlDeserialiserDefaultImpl;
import logic.impl.Task1MatchScoreListener;
import logic.impl.Task2MatchScoreListener;
import logic.impl.Task3MatchScoreListener;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import utils.DecendingNumberStringSortUtil;
import utils.IOUtil;
import xstream.XStreamFactory;
import xstream.XStreamFactoryImpl;

/**
 * Spring Dependency Injection Configuration.
 * @author Stephan Schröder
 */
@Configuration
public class AppConfig
{
  /**
   * application will end with an error message if the sum of goals 
   * (within one match (task 2) or of all teams starting with the same starting letter (task 3))
   * has more digits than this element. (And yes, i could also put this variable into an
   * configuration file, so that no recompile would be neccessary should 
   * this number have to be increased. But i just don't think that this case will ever happen.)
   */
  final static private int MAX_DIGITS_OF_GOAL_SUM = 5;
  
  //Beans with Singleton scope
  
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public IOUtil ioUtil()
  {
    return new IOUtil();
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public XStreamFactory xstreamFactory()
  {
    return new XStreamFactoryImpl();
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public LivescoreXmlDeserialiser livescoreXmlDeserialiser()
  {
    return new LivescoreXmlDeserialiserDefaultImpl(ioUtil(), xstreamFactory());
  }
  
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public DecendingNumberStringSortUtil decendingNumberStringSortUtil()
  {
    return new DecendingNumberStringSortUtil(MAX_DIGITS_OF_GOAL_SUM); 
  }
  
  //Beans with Prototype scope

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public LivescoreProcessor livescoreProcessor()
  {
    LivescoreProcessor livescoreProcessor = new LivescoreProcessor(livescoreXmlDeserialiser());
    livescoreProcessor.setMatchScoreListener(Arrays.asList(
        task1Listener(),
        task2Listener(),
        task3Listener()
    ));
    return livescoreProcessor;
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public MatchScoreListener task1Listener()
  {
    Task1MatchScoreListener taskListener = new Task1MatchScoreListener();
    taskListener.setIoUtil(ioUtil());
    return taskListener;
  }
  
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public MatchScoreListener task2Listener()
  {
    Task2MatchScoreListener taskListener = new Task2MatchScoreListener();
    taskListener.setIoUtil(ioUtil());
    taskListener.setSortPrefixUtil(decendingNumberStringSortUtil());
    return taskListener;
  }
  
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public MatchScoreListener task3Listener()
  {
    Task3MatchScoreListener taskListener = new Task3MatchScoreListener();
    taskListener.setIoUtil(ioUtil());
    taskListener.setSortPrefixUtil(decendingNumberStringSortUtil());
    return taskListener;
  }
}
