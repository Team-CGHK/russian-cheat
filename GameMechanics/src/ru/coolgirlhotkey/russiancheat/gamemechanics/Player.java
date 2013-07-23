package ru.coolgirlhotkey.russiancheat.gamemechanics;

public abstract class Player {
    private boolean[] cards;

    private String name;

    public String getName() {
        return name;
    }

    abstract public FirstTurnResult firstTurn();
    abstract public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount, int actualCardsCount);

    abstract public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount);
    abstract public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, int actualCardsCount);
    //TODO get check result from ru.coolgirlhotkey.russiancheat.gamemechanics.GameServer

    public class FirstTurnResult {
        Card.CardValue declaredCardValue;
        int[] cards;
    }

    public class DependentTurnResult {
        boolean isChecking;
        int cardToCheck;
        int[] cards;
    }

    public class DeckException extends Exception {
        Player problemPlayer;
        int problemCard;

        DeckException(String msg, int card) {
            super(msg);
            problemPlayer = Player.this;
            problemCard = card;
        }
    }

    public void takeCard(int card) throws DeckException {
        if (!cards[card])
            cards[card] = true;
        else
            throw new DeckException("The player's deck already contains the card", card);
    }

    public void dropCard(int card) throws DeckException {
        if (cards[card])
            cards[card] = false;
        else
            throw new DeckException("The player's deck doesn't contain the card to remove", card);
    }

    public boolean hasCards() {
        boolean result = false;
        for (int i = 0; i < cards.length && !result; i++)
            result |= cards[i];
        return result;
    }

    //card is 52-cards deck card value
    public boolean hasCard(int card) {
        return cards[card];
    }

    public int cardsOfValue(Card.CardValue value) {
        int count = 0;
        for (Card.CardSuit suit : Card.CardSuit.values()) {
            if (hasCard(Card.getCardIndex(value, suit)))
                count++;
        }
        return count;
    }
}