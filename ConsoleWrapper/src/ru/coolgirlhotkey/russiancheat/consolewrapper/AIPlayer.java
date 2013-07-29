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
    final double MAX_DIFF_TO_CONSIDER = 0.15;
    final double[] CARDS_TO_PUT_NUMBER_WEIGHT = new double[]{0, 0.33, 0.66, 0.93, 1.05, 1.14, 1.17};
    final double NO_CARDS_OF_VALUE_TRUTH_FACTOR = 0.90;
    final double CARD_IN_MY_DECK_TRUTH_WEIGHT = 0.07;
    final double EACH_CARD_TO_PUT_TRUTH_WEIGHT = 0.06;
    final double EACH_CARD_ON_BOARD_TRUTH_WEIGHT = 0.01;
    final double DEFAULT_ACE_WEIGHT = 0.4;
    final double EACH_CARD_DECREASE_ACE_WEIGHT = 0.01;
    final double EACH_ACE_IN_DECK_WEIGHT = 0.1;
    final double DEFAULT_CARD_FACTOR = 0.5;
    final double LOW_CARD_FACTOR = 0.1;
    final double HIGH_CARD_FACTOR = 0.9;
    final double LIE_WEIGHT = 0.45;
    final double LOW_CHECK_FACTOR = 0.3;
    final double EACH_ACTUAL_CARD_CHECK_WEIGHT = 0.05;
    final double EACH_OUR_DECLARED_CARD_CHECK_WEIGHT = 0.15;
    final double EACH_DROPPED_CARD_VALUE_CHECK_WEIGHT = 0.01;
    double aggressivenessOfCardsNumber;
    //

    AIPlayer() {
        super();
        name = "AI";
        Random rng = new Random();
        aggressivenessOfCardsNumber = rng.nextDouble() * 1.2;
    }

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
            if (maxFactor - valueFactor[i] < MAX_DIFF_TO_CONSIDER)
                considerableValuesIndexes.add(i);
        }
        // choose randomly one of the considerable card values to declare
        int declaredValueIndex = considerableValuesIndexes.get(rng.nextInt(considerableValuesIndexes.size()));
        return new FirstTurnResult(declaredValueIndex, chooseCardsToPut(0, valuesInGame, declaredValueIndex));
    }

    private int[] chooseCardsToPut(int cardsOnBoard, List<Card.CardValue> valuesInGame, int declaredValueIndex) {
        Random rng = new Random();
        double cardsToPutNumberFactor = rng.nextDouble() * aggressivenessOfCardsNumber;
        int cardsToPutNumber = 1;
        for (int i = 0; i < CARDS_TO_PUT_NUMBER_WEIGHT.length; i++)
            if (CARDS_TO_PUT_NUMBER_WEIGHT[i] > cardsToPutNumberFactor && (i == CARDS_TO_PUT_NUMBER_WEIGHT.length - 1 || cardsToPutNumberFactor < CARDS_TO_PUT_NUMBER_WEIGHT[i + 1]))
                cardsToPutNumber = i;
        int[] cardsToPut = new int[cardsToPutNumber];
        for (int i = 0; i < cardsToPutNumber; i++) {
            double[] cardFactor = new double[cards.length];
            for (int j = 0; j < cardFactor.length; j++) {
                cardFactor[j] = hasCard(j) ? DEFAULT_CARD_FACTOR : 0;
            }
            double truthFactor = rng.nextDouble();
            truthFactor *= NO_CARDS_OF_VALUE_TRUTH_FACTOR + cardsOfValue(valuesInGame.get(declaredValueIndex)) * CARD_IN_MY_DECK_TRUTH_WEIGHT;
            truthFactor += (cardsToPutNumber - 1) * EACH_CARD_TO_PUT_TRUTH_WEIGHT;
            //TODO cardsOnBoard factor
            for (Card.CardSuit suit : Card.CardSuit.values())
                if (cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] > 0)
                    cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] = truthFactor < LIE_WEIGHT ? LOW_CARD_FACTOR : HIGH_CARD_FACTOR;
            if (truthFactor < LIE_WEIGHT) {
                for (Card.CardSuit suit : Card.CardSuit.values())
                    if (cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] > 0)
                        cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] = LOW_CARD_FACTOR;
                double aceChoiceFactor = rng.nextDouble();
                aceChoiceFactor += EACH_ACE_IN_DECK_WEIGHT * cardsOfValue(Card.CardValue.Ace);
                aceChoiceFactor -= EACH_CARD_DECREASE_ACE_WEIGHT * cardsCount();
                for (Card.CardSuit suit : Card.CardSuit.values())
                    if (cardFactor[Card.getCardIndex(Card.CardValue.Ace, suit)] > 0)
                        cardFactor[Card.getCardIndex(Card.CardValue.Ace, suit)] = aceChoiceFactor > DEFAULT_ACE_WEIGHT ? HIGH_CARD_FACTOR : LOW_CARD_FACTOR;
            }
            double maxCardFactor = 0;
            for (int j = 0; j < cardFactor.length; j++)
                if (cardFactor[i] > maxCardFactor) {
                    maxCardFactor = cardFactor[j];
                }
            List<Integer> considerableCards = new ArrayList<Integer>();
            for (int j = 0; i < cardFactor.length; i++) {
                if (maxCardFactor - cardFactor[i] < MAX_DIFF_TO_CONSIDER)
                    considerableCards.add(i);
                cardsToPut[i] = considerableCards.get(rng.nextInt(considerableCards.size()));
            }
        }
        return cardsToPut;
    }

    @Override
    public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount,
                                             int actualCardsCount, List<Card.CardValue> valuesInGame) {
        Random rnd = new Random();
        double checkThreshold = LOW_CHECK_FACTOR + EACH_ACTUAL_CARD_CHECK_WEIGHT * actualCardsCount +
                EACH_OUR_DECLARED_CARD_CHECK_WEIGHT * cardsOfValue(declaredCard) - EACH_DROPPED_CARD_VALUE_CHECK_WEIGHT *
                (Card.CardValue.values().length - valuesInGame.size());
        double checkFactor = rnd.nextDouble();
        if (checkFactor < checkThreshold)  //check
            return new DependentTurnResult(true, rnd.nextInt(actualCardsCount), null);
        return new DependentTurnResult(false, -1,
                chooseCardsToPut(cardsOnBoardCount, valuesInGame, valuesInGame.indexOf(declaredCard)));
    }

    @Override
    public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount) {

    }

    @Override
    public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, int actualCardsCount) {

    }

    @Override
    public void notifyDroppedCardValues(int playerIndex, List<Card.CardValue> droppedValues) {

    }
}
