package com.stenudd.tarot.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.stenudd.tarot.client.event.CardSelectedEvent;
import com.stenudd.tarot.client.event.CardSelectionListener;

public class CelticCrossSpread
extends Composite
implements CardSelectionListener
{
  private final Position[] cardPositions = new Position[2];
  private final AbsolutePanel absolutePanel;
  private final Grid descriptionGrid;
  private final TarotCardDeck deck;
  private final Button dealCardsButton;
  
  //count the uncovered cards
  int cardsUncovered = 0;
  
  public CelticCrossSpread()
  {
    deck = new TarotCardDeck(100);
    
    absolutePanel = new AbsolutePanel();
    descriptionGrid = new Grid(10,2);
    initDescriptionGrid();
    
    dealCardsButton = new Button("Deal Cards");
    dealCardsButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event)
      {
        reset();
      }
    });
    
    reset();
    //after the app has been loaded
    //the user should be able to get new cards dealt
    //(although there are already 10 covered cards)
    enableDealCard(true);

    initWidget(absolutePanel);
  }
  
  private void initDescriptionGrid()
  {
    descriptionGrid.setStyleName("resultsTable");
    descriptionGrid.getColumnFormatter().addStyleName(0, "tdText");
    
    for(int i=0; i<10; i++) {
      descriptionGrid.setText(i, 0, TarotCardIndexMeanings.meaning(i)+':');
    }
  }
  
  public void reset()
  {
    //disable the dealCards-Button
    cardsUncovered = 0;
    enableDealCard(false);
    
    //clear the grid links
    for(int i=0; i<10; i++) {
      descriptionGrid.setText(i, 1, "");
      descriptionGrid.getCellFormatter().setStyleName(i, 1, "tdEmptyLink");
    }
    
    //clear the cards
    absolutePanel.clear();
    
    final TarotCard[] cards = deck.pickRandomCards(10);
    final int width = cards[0].getPixelWidth();
    final int heigth = cards[0].getPixelHeight();
    final int space = 10;
    final int cluster_separation_space = 50;
    
    final int[] left = {
        0,
        width+space,
        space+(int)Math.rint((width+heigth)*0.5),
        width+heigth+2*space,
        2*width+2*space+cluster_separation_space+heigth,
        3*width+2*space+cluster_separation_space+heigth
    };
    
    final int[] top = {
        0,
        0,
        heigth+space,
        space+(int)Math.rint(1.5*heigth-0.5*width),
        0,
        2*(heigth+space),
        (int)Math.rint(2.5*(heigth+space)),
        3*(heigth+space),
        4*heigth+3*space+5
    };
    
    cards[1].rotate();
    //place cards
    addCard(cards[0], left[2], top[2]);
    addCard(cards[1], left[1], top[3]);
    addCard(cards[2], left[2], top[0]);
    addCard(cards[3], left[2], top[5]);
    addCard(cards[4], left[3], top[2]);
    addCard(cards[5], left[0], top[2]);
    addCard(cards[6], left[4], top[7]);
    addCard(cards[7], left[4], top[5]);
    addCard(cards[8], left[4], top[2]);
    addCard(cards[9], left[4], top[0]);
    
    //place button
    absolutePanel.add(dealCardsButton, left[0], top[0]);
    
    //place grid
    final int gridBorder = width/5;
    absolutePanel.add(descriptionGrid, gridBorder+20, top[7]+gridBorder);
    
    setSize(left[5], top[8]+80+gridBorder);
    
    // add open-new-tab-support for descriptionGrid
    descriptionGrid.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event)
      {
        Cell cellClicked = descriptionGrid.getCellForEvent(event);
        TarotCard cardSelected = cards[cellClicked.getRowIndex()];
        Window.open(cardSelected.getExplanationUrl(), "explanation", "");
      }
    });
  }
  
  public void setSize(int pixelWidth, int pixelHeight)
  {
    absolutePanel.setHeight(pixelHeight+"px");
    absolutePanel.setWidth( pixelWidth +"px");
  }
  
  private void addCard(TarotCard card, int left, int top)
  {
    final int cardIndex = card.getCardIndex();
    if(cardIndex==0 || cardIndex==1) {
      cardPositions[cardIndex] = new Position(left, top);
    }
    card.addCardSelectionListener(this);
    absolutePanel.add(card, left, top);
  }
  
  private void topCard(TarotCard card)
  {
    final int cardIndex = card.getCardIndex();
    if(cardIndex==0 || cardIndex==1) {
      Position position = cardPositions[cardIndex];
      absolutePanel.add(card, position.left, position.top);
    }
  }
  
  public void cardSelected(CardSelectedEvent event)
  {
    TarotCard selectedCard = event.getSelectedCard();
    topCard(selectedCard);
    
    if(event.isUncovered()) {
      descriptionGrid.setText(selectedCard.getCardIndex(), 1, selectedCard.getCardName());
      descriptionGrid.getCellFormatter().setStyleName(selectedCard.getCardIndex(), 1, "tdLink");
      
      //enable the deal-cards button if all 10 cards have been uncovered
      //disable it otherwise
      enableDealCard(++cardsUncovered==10);
    }
  }
  
  private class Position{
    final int left;
    final int top;
    public Position(int left, int top)
    {
      this.left = left;
      this.top  = top;
    }
  }
  
  private void enableDealCard(boolean enable)
  {
    if(enable) {
      dealCardsButton.setEnabled(true);
      dealCardsButton.setTitle("Click here to get new cards.");
      dealCardsButton.setFocus(true);
    } else {
      dealCardsButton.setEnabled(false);
      dealCardsButton.setTitle("First uncover all cards.");
    }
    
  }
}
