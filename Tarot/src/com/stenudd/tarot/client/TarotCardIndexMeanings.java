package com.stenudd.tarot.client;

public enum TarotCardIndexMeanings
{
  CONDITION_PRESENT("Your condition at present"),
  OBSTACLES_PRESENT("Your obstacle and trouble at present"),
  BEST_OUTCOME("The best possible outcome for you"),
  CAUSE_PRESEND("The cause to your present situation"),
  YOUR_PAST("Your immediate past"),
  YOUR_FUTURE("Your immediate future"),
  YOUR_PRESENT("You at present"),
  SURROUNDINGS_PRESENT("Your surroundings at present"),
  HOPES_FEARS("Your hopes and fears"),
  OUTCOME("The outcome");
  
  final private String explanation;
  
  private TarotCardIndexMeanings(String explanation)
  {
    this.explanation = explanation;
  }
  
  @Override
  public String toString()
  {
    return explanation;
  }
  
  public static String meaning(int index)
  {
    return TarotCardIndexMeanings.values()[index].toString();
  }
}
