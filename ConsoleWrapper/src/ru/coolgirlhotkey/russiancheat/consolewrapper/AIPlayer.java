package ru.coolgirlhotkey.russiancheat.consolewrapper;

import ru.coolgirlhotkey.russiancheat.gamemechanics.Card;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hotkey on 28.07.13.
 */
public class AIPlayer extends Player {
    //AI logics constants
    final double CARD_IN_MY_DECK_WEIGHT = 0.1;
    final double MAX_DIFF_TO_CONSIDER_VALUE = 0.15;
    //

    @Override
    public FirstTurnResult firstTurn(List<Card.CardValue> valuesInGame) {
        Random rng = new Random();
        double[] valueFactor = new double[valuesInGame.size()];
        for (int i = 0; i < valuesInGame.size(); i++) {
            valueFactor[i] += cardsOfValue(valuesInGame.get(i)) * CARD_IN_MY_DECK_WEIGHT;
            //TODO more factors to calculate valueFactor
        }
        // find the max factor value
        double maxFactor = 0;
        int maxFactorValueIndex = -1;
        for (int i = 0; i < valueFactor.length; i++)
            if (valueFactor[i] > maxFactor) {
                maxFactor = valueFactor[i];
                maxFactorValueIndex = i;
            }
        // choose indexes of card values which have factors close enough to the max factor
        // to consider them to be declared
        List<Integer> considerableValuesIndexes = new ArrayList<Integer>();
        for (int i = 0; i < valuesInGame.size(); i++) {
            if (maxFactor - valueFactor[i] < MAX_DIFF_TO_CONSIDER_VALUE)
                considerableValuesIndexes.add(i);
        }
        // choose randomly one of the considerable card values to declare
        int declaredValueIndex = considerableValuesIndexes.get(rng.nextInt(considerableValuesIndexes.size()));
        //TODO implement cards to put choice
        throw new NotImplementedException();
    }

    @Override
    public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount, int actualCardsCount) {
        throw new NotImplementedException();
    }

    @Override
    public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount) {
        throw new NotImplementedException();
    }

    @Override
    public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, int actualCardsCount) {
        throw new NotImplementedException();
    }

    @Override
    public void notifyDroppedCardValues(int playerIndex, List<Card.CardValue> droppedValues) {
        throw new NotImplementedException();
    }
}
