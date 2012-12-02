package com.stenudd.tarot.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Image;
import com.stenudd.tarot.client.event.CardSelectedEvent;
import com.stenudd.tarot.client.event.CardSelectionListener;

public class TarotCard
extends Composite
{
  final private Image image;
  final private List<CardSelectionListener> selectionListeners = new LinkedList<CardSelectionListener>();
  final private String explanationUrl;
  final private String cardName;
  final private int cardIndex;

  final private int pixelWidth;
  final private int pixelHeight;
  //pixelborder of the decorator panel
  final private int borderPixels = 5;
  
  private boolean covered = true;
  private String imgUrl;
  private String imgRotateUrl;
  
  private static String BACKSIDE_IMG_PATH = "./images/cardbackside.jpg";
  private static String BACKSIDE_ROTATED_IMG_PATH = "./images/rotated/cardbackside.jpg";
  
  public static void prefetchBackside()
  {
    Image.prefetch(BACKSIDE_IMG_PATH);
    Image.prefetch(BACKSIDE_ROTATED_IMG_PATH);
  }
  
  public void addCardSelectionListener(CardSelectionListener listener)
  {
    selectionListeners.add(listener);
  }
  
  public TarotCard(final int cardIndex, final String cardName, final String explanationUrl, final String imgUrl, String imgRotateUrl, final int pixelWidth)
  {
    this.cardIndex = cardIndex;
    this.cardName  = cardName;
    this.explanationUrl = explanationUrl;
    this.pixelWidth = pixelWidth;
    pixelHeight = (int)Math.ceil(pixelWidth*1.6475);
    this.imgUrl = imgUrl;
    this.imgRotateUrl = imgRotateUrl;
    
//    StringBuilder htmlB = new StringBuilder(128);
//    htmlB.append("<div class=\"links\"><a target=\"explaination\" href=\"");
//    htmlB.append(explanationUrl);
//    htmlB.append("\" title=\"Click to learn more about the meaning of this card\">");
//    htmlB.append(cardName);
//    htmlB.append("</a></div>");
//    explanationLink = htmlB.toString();
    
    //initialize the image
    image = new Image(BACKSIDE_IMG_PATH);
    image.setStyleName("cardImage");
    image.setAltText("couldn't load image for "+cardName);
    Image.prefetch(imgUrl);
    image.setWidth(pixelWidth+"px");
    image.setHeight(pixelHeight+"px");
    image.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event)
      {
        cardSelected();
      }
    });
    
    DecoratorPanel cardDecorator = new DecoratorPanel();
    cardDecorator.setWidget(image);
    cardDecorator.addStyleName("cardBorderPanel");
    
    initWidget(cardDecorator);
  }
  
  public int getPixelWidth()
  {
    return pixelWidth+2*borderPixels;
  }

  public int getPixelHeight()
  {
    return pixelHeight+2*borderPixels;
  }

  public void rotate()
  {
    image.setUrl(BACKSIDE_ROTATED_IMG_PATH);
    Image.prefetch(imgRotateUrl);
    imgUrl = imgRotateUrl;
    
    image.setWidth( pixelHeight+"px");
    image.setHeight(pixelWidth +"px");
  }
  
  private void cardSelected()
  {
    boolean uncovered = covered;
    if(covered) {
      //reveal card
      image.setUrl(imgUrl);
      covered = false;
      
      image.setTitle(TarotCardIndexMeanings.meaning(cardIndex));
    }
    
    final CardSelectedEvent event = new CardSelectedEvent(this, uncovered);
    for(CardSelectionListener listener: selectionListeners) {
      listener.cardSelected(event);
    }
  }
  
  public String getExplanationUrl()
  {
    return explanationUrl;
  }

  public String getCardName()
  {
    return cardName;
  }

  public int getCardIndex()
  {
    return cardIndex;
  }
}
