/**
 * Created by hotkey on 20.07.13.
 */

//PLEASE DON'T REFORMAT THIS FILE

public class Card {

    // Card instances should never be created => the ctor is private
    private Card() {
    }

    public static enum CardSuit {
        Clubs, Diamonds, Hearts, Spades
    }

    // value is 0..N-1, where 0 is the lowest card, N-1 is Ace
    // in 52-cards deck 0 == "2", 12 == Ace,
    // in 36-cards deck 0 == "6",  8 == Ace
    public static int getCardIndex(int value, CardSuit suit, int deckSize) {
        int suitOffset =
                suit == CardSuit.Clubs    ? 0 :
                suit == CardSuit.Diamonds ? 1 :
                suit == CardSuit.Hearts   ? 2 :
             /* suit == CardSuit.Spades  */ 3;
        int cardValueIn52Deck = value + (52 - deckSize) / 4;
        return suitOffset * deckSize / 4 + cardValueIn52Deck;
    }

    // cardIndex here and below is card position in 52-cards deck, for example, 11 == K♣
    public static CardSuit getCardSuit(int cardIndex, int deckSize)
    {
        int suitValue = cardIndex / (deckSize/4);
        return
                suitValue == 0 ? CardSuit.Clubs :
                suitValue == 1 ? CardSuit.Diamonds :
                suitValue == 2 ? CardSuit.Hearts :
             /* suitValue == 3 */CardSuit.Spades;
    }

    // return value is 0 for lowest card in a deck of specified size, (deckSize/4 - 1) for the Ace
    public static int getCardValue(int cardIndex, int deckSize)
    {
        int cardValueIn52Deck = cardIndex % (deckSize/4);
        return cardValueIn52Deck - (52-deckSize) / 4;
    }
}
