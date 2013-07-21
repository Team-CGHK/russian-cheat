/**
 * Created by hotkey on 20.07.13.
 */

public class Card {

    // Card instances should never be created => the ctor is private
    private Card() {
    }

    public static enum CardSuit {
        Clubs(), Diamonds(), Hearts(), Spades();
    }
    public static enum CardValue {
        Two(), Three(), Four(), Five(), Six(), Seven(), Eight(), Nine(), Ten(), Jack(), Queen(), King(), Ace();
        //TODO decide, whether we need Jokers in a deck
    }

    public static final int MAX_DECK_SIZE = 52;

    public static int getCardIndex(CardValue value, CardSuit suit) {
        return suit.ordinal() * (MAX_DECK_SIZE / 4) + value.ordinal();
    }

    // cardIndex here and below is card position in 52-cards deck, for example, 11 == K♣
    public static CardSuit getCardSuit(int cardIndex) {
        int suitCode = cardIndex / (MAX_DECK_SIZE / 4);
        for (CardSuit suit : CardSuit.values())
            if (suit.ordinal() == suitCode)
                return suit;
        return null;
    }

    public static CardValue getCardValue(int cardIndex) {
        int valueCode = cardIndex % (MAX_DECK_SIZE / 4);
        for (CardValue value : CardValue.values())
            if (value.ordinal() == valueCode)
                return value;
        return null;
    }
}
