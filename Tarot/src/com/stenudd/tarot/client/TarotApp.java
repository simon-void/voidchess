package com.stenudd.tarot.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.stenudd.tarot.client.css.CssBundle;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TarotApp implements EntryPoint
{
  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
    //prefetch the backside of the card
    //because it will be visible from the very beginning
    TarotCard.prefetchBackside();
    
    //make sure the Tarot.css file is read
    CssBundle.INSTANCE.css().ensureInjected();
    
    //initialize the cards
    final CelticCrossSpread cardSpread = new CelticCrossSpread();
    
    //wait for halv a second to make sure
    //that the CardBacksides have been prefetched.
    //making it shorter than halv a second is risky,
    //because the user might only see a flicker, without
    //being able to perceive what was just shown
    Timer timedLaunch = new Timer() {
      @Override
      public void run()
      {
        //remove the splash screen
        DOM.getElementById("tarotAppSplash").removeFromParent();        
        //add the application
        RootPanel.get("tarotAppContainer").add(cardSpread);
      }
    };
    timedLaunch.schedule(500);
  }
}
