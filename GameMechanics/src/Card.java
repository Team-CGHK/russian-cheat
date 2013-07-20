/**
 * Created by hotkey on 20.07.13.
 */

public class Card {

    // Card instances should never be created => the ctor is private
    private Card() {
    }

    public static enum CardSuit {
        Clubs(0), Diamonds(1), Hearts(2), Spades(3);

        private int code;

        public int getCode() {
            return code;
        }

        CardSuit(int c) {
            code = c;
        }
    }

    public static enum CardValue {
        Two(0), Three(1), Four(2), Five(3), Six(4), Seven(5), Eight(6), Nine(7), Ten(8), Jack(9), Queen(10), King(11), Ace(12);
        //TODO decide, whether we need Jokers in a deck

        private int code;

        public int getCode() {
            return code;
        }

        CardValue(int c) {
            code = c;
        }
    }

    public static final int MAX_DECK_SIZE = 52;

    public static int getCardIndex(CardValue value, CardSuit suit) {
        return suit.getCode() * (MAX_DECK_SIZE / 4) + value.getCode();
    }

    // cardIndex here and below is card position in 52-cards deck, for example, 11 == K♣
    public static CardSuit getCardSuit(int cardIndex, int deckSize) {
        int suitCode = cardIndex / (MAX_DECK_SIZE / 4);
        for (CardSuit suit : CardSuit.values())
            if (suit.getCode() == suitCode)
                return suit;
        return null;
    }

    public static CardValue getCardValue(int cardIndex, int deckSize) {
        int valueCode = cardIndex % (MAX_DECK_SIZE / 4);
        for (CardValue value : CardValue.values())
            if (value.getCode() == valueCode)
                return value;
        return null;
    }
}
