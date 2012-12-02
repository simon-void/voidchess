package com.stenudd.tarot.client.event;

import com.stenudd.tarot.client.TarotCard;

public class CardSelectedEvent
{
  final private TarotCard card;
  final private boolean uncoverd;
  
  public CardSelectedEvent(TarotCard card, boolean uncovered)
  {
    this.card = card;
    this.uncoverd = uncovered;
  }

  public boolean isUncovered()
  {
    return uncoverd;
  }

  public TarotCard getSelectedCard()
  {
    return card;
  }
}
