package ru.coolgitlhotkey.russiancheat.android;

import ru.coolgirlhotkey.russiancheat.gamemechanics.Card;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

import java.util.List;

/**
 * Created by hotkey on 21.08.13.
 */
public class AndroidRemotePlayer extends Player {
    AndroidRemotePlayer(int currentIndex) {
        super(currentIndex);
    }

    @Override
    public FirstTurnResult firstTurn(List<Card.CardValue> valuesInGame) {
        return null;
    }

    @Override
    public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount, int actualCardsCount, List<Card.CardValue> valuesInGame) {
        return null;
    }

    @Override
    public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount) {

    }

    @Override
    public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, boolean checkSuccess, int actualCardsCount) {

    }

    @Override
    public void notifyDroppedCardValues(int playerIndex, List<Card.CardValue> droppedValues) {

    }

    @Override
    public void notifyPlayerTakingCards(int playerIndex, int cardsCount) {

    }

    @Override
    public void notifyThisPlayerTakingCards(List<int[]> cards) {

    }

    @Override
    public void notifyEndGame(int[] places) {

    }
}
