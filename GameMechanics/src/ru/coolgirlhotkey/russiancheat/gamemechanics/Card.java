package ru.coolgirlhotkey.russiancheat.gamemechanics;

/**
 * Created by hotkey on 20.07.13.
 */

public class Card {

    // Card instances should never be created => the ctor is private
    private Card() {
    }

    public static enum CardSuit {
        Clubs("♣"), Diamonds("♦"), Hearts("♥"), Spades("♠");

        private final String stringRepresentation;

        public String toString() {
            return stringRepresentation;
        }

        CardSuit(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
    }

    public static enum CardValue {
        Two("2"), Three("3"), Four("4"), Five("5"), Six("6"), Seven("7"), Eight("8"), Nine("9"), Ten("10"), Jack("J"), Queen("Q"), King("K"), Ace("A");

        private final String stringRepresentation;

        public String toString() {
            return stringRepresentation;
        }

        CardValue(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
    }

    public static CardValue parseCardValue(String stringRepresentation) {
        for (CardValue cardValue : CardValue.values())
            if (cardValue.toString().equals(stringRepresentation))
                return cardValue;
        return null;
    }

    public static final int MAX_DECK_SIZE = 52;

    public static int getCardIndex(CardValue value, CardSuit suit) {
        return suit.ordinal() * (MAX_DECK_SIZE / 4) + value.ordinal();
    }

    // cardIndex here and below is card position in 52-cards deck, for example, 11 == K♣
    public static CardSuit getCardSuit(int cardIndex) {
        int suitCode = cardIndex / (MAX_DECK_SIZE / 4);
        return CardSuit.values()[suitCode];
    }

    public static CardValue getCardValue(int cardIndex) {
        int valueCode = cardIndex % (MAX_DECK_SIZE / 4);
        return CardValue.values()[valueCode];
    }
}
