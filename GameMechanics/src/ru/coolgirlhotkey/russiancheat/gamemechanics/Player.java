package ru.coolgirlhotkey.russiancheat.gamemechanics;

import java.util.List;

public abstract class Player {
    public Player(int currentIndex) {
        cards = new boolean[Card.MAX_DECK_SIZE];
        this.currentIndex = currentIndex;
    }

    protected boolean[] cards;
    protected String name;

    protected GameServer.PlayerInfo[] currentGamePlayersInfo;
    protected final int currentIndex;

    public String getName() {
        return name;
    }

    abstract public FirstTurnResult firstTurn(List<Card.CardValue> valuesInGame);

    abstract public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount, int actualCardsCount, List<Card.CardValue> valuesInGame);

    abstract public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount, int nextPlayerCardsNumber);

    abstract public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck,
                                             int showdown, boolean checkSuccess, int actualCardsCount,int nextPlayerCardsNumber);

    abstract public void notifyDroppedCardValues(int playerIndex, List<Card.CardValue> droppedValues);

    abstract public void notifyPlayerTakingCards(int playerIndex, int cardsCount);

    abstract public void notifyThisPlayerTakingCards(List<int[]> cards);

    abstract public void notifyEndGame(int[] places);

    public class FirstTurnResult {
        int declaredCardValueIndex;
        int[] cards;

        public FirstTurnResult(int declaredCardValueIndex, int[] cards) {
            this.declaredCardValueIndex = declaredCardValueIndex;
            this.cards = cards;
        }
    }

    public class DependentTurnResult {
        boolean isChecking;
        int cardToCheck;
        int[] cards;

        public DependentTurnResult(boolean isChecking, int cardToCheck, int[] cards) {
            this.isChecking = isChecking;
            if (isChecking) {
                this.cardToCheck = cardToCheck;
                cards = null;
            } else {
                this.cards = cards;
                this.cardToCheck = -1;
            }
        }
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

    public int cardsCount() {
        int result = 0;
        for (int i = 0; i < cards.length; i++)
            if (cards[i]) result++;
        return result;
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