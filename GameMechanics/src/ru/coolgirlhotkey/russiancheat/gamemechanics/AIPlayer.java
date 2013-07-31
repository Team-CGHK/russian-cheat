package ru.coolgirlhotkey.russiancheat.gamemechanics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hotkey on 28.07.13.
 */
public class AIPlayer extends Player {
    private static int instancesCount = 0;
    //AI logics constants
    //choose value logic
    final double CARD_IN_MY_DECK_WEIGHT = 0.1;
    final double MAX_DIFF_TO_CONSIDER = 0.09;
    final double MAX_DIFF_TO_CONSIDER_CARD = 0.05;
    // choose cardsToPutNumber logic
    double aggressivenessOfCardsNumber;
    final double[] CARDS_TO_PUT_NUMBER_WEIGHT = new double[]{0, 0.45, 0.9, 0.99, 1.05, 1.07, 1.09};
    // truth and lie logic
    final double LIE_THRESHOLD = 0.6;
    final double CARD_IN_MY_DECK_TRUTH_WEIGHT = 0.07;
    final double NO_CARDS_OF_VALUE_TRUTH_FACTOR_MULTIPLIER = 0.90;
    final double EACH_CARD_TO_PUT_TRUTH_WEIGHT = 0.06;
    final double EACH_CARD_ON_BOARD_TRUTH_WEIGHT = 0.01;
    final double TRUTH_STATS_WEIGHT = 0.6;
    // choose Ace logic
    final double NON_ACE_THRESHOLD = 0.4;
    final double EACH_CARD_DECREASE_ACE_WEIGHT = 0.01;
    final double EACH_ACE_IN_DECK_WEIGHT = 0.1;
    // choose cards logic
    final double DEFAULT_CARD_FACTOR = 0.5;
    final double LOW_CARD_FACTOR = 0.1;
    final double HIGH_CARD_FACTOR = 0.9;
    // check logic
    final double DEFAULT_CHECK_THRESHOLD = 0.3;
    final double EACH_ACTUAL_CARD_CHECK_WEIGHT = 0.07;
    final double EACH_KNOWN_DECLARED_CARD_CHECK_WEIGHT = 0.10;
    final double EACH_DROPPED_CARD_VALUE_CHECK_WEIGHT = 0.01;
    final double CHECK_STATS_WEIGHT = 0.4;
    //


    int hadCardsOfDeclaredValue;
    boolean ignoreMyOwnFirstTurnNotification;
    //

    private static class HumanStats {
        static private int[] cardsCountToLieRecords = new int[7];
        static private int[] cardsCountToLieConfirmed = new int[cardsCountToLieRecords.length];

        static private int[] lapToLieRecords = new int[Card.MAX_DECK_SIZE / 2 + 1];
        static private int[] lapToLieConfirmed = new int[lapToLieRecords.length];

        static private int[] lapToCheckRecords = new int[Card.MAX_DECK_SIZE / 2 + 1];
        static private int[] lapToCheckConfirmed = new int[lapToCheckRecords.length];

        static private void fillArrayWithFileLine(BufferedReader br, int[] array) throws IOException {
            String[] parts = br.readLine().split("\\s+");
            for (int i = 0; i < array.length; i++)
                array[i] = Integer.parseInt(parts[i]);
        }

        static private void printArrayToFileLine(PrintWriter pw, int[] array) throws IOException {
            for (int i = 0; i < array.length; i++)
                pw.print(array[i] + " ");
            pw.println();
        }

        static public void fromFile(String fileName) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                fillArrayWithFileLine(br, cardsCountToLieRecords);
                fillArrayWithFileLine(br, cardsCountToLieConfirmed);
                fillArrayWithFileLine(br, lapToLieRecords);
                fillArrayWithFileLine(br, lapToLieConfirmed);
                fillArrayWithFileLine(br, lapToCheckRecords);
                fillArrayWithFileLine(br, lapToCheckConfirmed);
                br.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        static public void toFile(String fileName) {
            try {
                PrintWriter pw = new PrintWriter(fileName);
                printArrayToFileLine(pw, cardsCountToLieRecords);
                printArrayToFileLine(pw, cardsCountToLieConfirmed);
                printArrayToFileLine(pw, lapToLieRecords);
                printArrayToFileLine(pw, lapToLieConfirmed);
                printArrayToFileLine(pw, lapToCheckRecords);
                printArrayToFileLine(pw, lapToCheckConfirmed);
                pw.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        static public void recordLieStats(boolean isLie, int lap, int cardsCount) {
            if (cardsCount < cardsCountToLieRecords.length) {
                cardsCountToLieRecords[cardsCount]++;
                if (isLie)
                    cardsCountToLieConfirmed[cardsCount]++;
                lapToLieRecords[lap]++;
                if (isLie)
                    lapToLieConfirmed[lap]++;
            }
        }

        static public void recordCheckStats(boolean isCheck, int lap) {
            lapToCheckRecords[lap]++;
            if (isCheck)
                lapToCheckConfirmed[lap]++;
        }

        static public double getLieChanceOnCardsCount(int cardsCount) {
            return cardsCount < cardsCountToLieRecords.length ?
                    1.0 * cardsCountToLieConfirmed[cardsCount] / (cardsCountToLieRecords[cardsCount] + 1e-15) : 1;
            //1e-15 is to avoid division by zero and NaN as a result
        }

        static public double getLieChanceOnCardsCountConfidence(int cardsCount) {
            return cardsCount < cardsCountToLieRecords.length ?
                    cardsCountToLieRecords[cardsCount] > 1 ? 1 : 0 : 0;
        }

        static public double getLieChanceOnLap(int lap) {
            return lap < lapToLieRecords.length ?
                    1.0 * lapToLieConfirmed[lap] / (lapToLieRecords[lap] + 1e-15) : 1;
        }

        static public double getLieChanceOnLapConfidence(int lap) {
            return lapToLieRecords[lap] > 1 ? 1 : 0;
        }

        static public double getCheckChanceOnLap(int lap) {
            return lap < lapToCheckRecords.length ?
                    1.0 * lapToCheckConfirmed[lap] / (lapToCheckRecords[lap] + 1e-15) : 1;
        }

        static public double getCheckChanceOnLapConfidence(int lap) {
            return lapToCheckRecords[lap] > 1 ? 1 : 0;
        }
    }

    AIPlayer(int currentIndex) {
        super(currentIndex);
        name = "AI" + (++instancesCount);
        Random rng = new Random();
        aggressivenessOfCardsNumber = 0.8 + rng.nextDouble() * 0.3;
    }

    public static void loadStatsFromFile(String fileName) {
        HumanStats.fromFile(fileName);
    }

    public static void saveStatsToFile(String fileName) {
        HumanStats.toFile(fileName);
    }

    @Override
    public FirstTurnResult firstTurn(List<Card.CardValue> valuesInGame) {
        currentLap = 0;
        Random rng = new Random();
        double[] valueFactor = new double[valuesInGame.size()];
        for (int i = 0; i < valuesInGame.size(); i++) {
            valueFactor[i] += cardsOfValue(valuesInGame.get(i)) * CARD_IN_MY_DECK_WEIGHT;
            //TODO more factors to calculate valueFactor
        }
        // find the max factor value
        double maxFactor = 0;
        for (int i = 0; i < valueFactor.length; i++)
            if (valueFactor[i] > maxFactor) {
                maxFactor = valueFactor[i];
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

        ignoreMyOwnFirstTurnNotification = true; //needed to protect hadCardsOfDeclaredValue from being changed in the notification
        hadCardsOfDeclaredValue = cardsOfValue(valuesInGame.get(declaredValueIndex));

        return new FirstTurnResult(declaredValueIndex, chooseCardsToPut(0, valuesInGame, declaredValueIndex));
    }

    //this overload excludes cards in int[] exclude from the result
    private int cardsOfValue(Card.CardValue value, int[] exclude) {
        int result = cardsOfValue(value);
        for (int card : exclude)
            if (card > 0 && card < Card.MAX_DECK_SIZE && Card.getCardValue(card) == value)
                result--;
        return result;
    }

    private int[] chooseCardsToPut(int cardsOnBoard, List<Card.CardValue> valuesInGame, int declaredValueIndex) {
        Random rng = new Random();
        double cardsToPutNumberFactor = rng.nextDouble() * aggressivenessOfCardsNumber;
        int cardsToPutNumber = 1;
        for (int i = 0; i < CARDS_TO_PUT_NUMBER_WEIGHT.length; i++)
            if (cardsToPutNumberFactor > CARDS_TO_PUT_NUMBER_WEIGHT[i])
                cardsToPutNumber = i + 1;
            else
                break;
        cardsToPutNumber = Math.min(cardsToPutNumber, cardsCount());
        int[] cardsToPut = new int[cardsToPutNumber];
        for (int i = 0; i < cardsToPut.length; i++)
            cardsToPut[i] = -1;
        for (int i = 0; i < cardsToPutNumber; i++) {
            double[] cardFactor = new double[cards.length];
            for (int j = 0; j < cardFactor.length; j++) {
                cardFactor[j] = hasCard(j) ? DEFAULT_CARD_FACTOR : 0;
            } //exclude cards in cardsToPut from being chosen again
            for (int j = 0; j < i; j++) {
                cardFactor[cardsToPut[j]] = 0;
            }
            double lieFactor = rng.nextDouble();
            lieFactor *= NO_CARDS_OF_VALUE_TRUTH_FACTOR_MULTIPLIER +
                         cardsOfValue(valuesInGame.get(declaredValueIndex), cardsToPut) * CARD_IN_MY_DECK_TRUTH_WEIGHT;
            lieFactor += HumanStats.getCheckChanceOnLapConfidence(currentLap)
                         * (HumanStats.getCheckChanceOnLap(currentLap) - 0.5) * TRUTH_STATS_WEIGHT;
            for (Card.CardSuit suit : Card.CardSuit.values())
                if (cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] != 0)
                    cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] = lieFactor < LIE_THRESHOLD ? LOW_CARD_FACTOR : HIGH_CARD_FACTOR;
            if (lieFactor < LIE_THRESHOLD) {
                for (Card.CardValue value : Card.CardValue.values())
                    if (value != valuesInGame.get(declaredValueIndex) && cardsOfValue(value) == 1)
                        for (Card.CardSuit suit : Card.CardSuit.values())
                            if (cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] != 0)
                                cardFactor[Card.getCardIndex(valuesInGame.get(declaredValueIndex), suit)] = HIGH_CARD_FACTOR;
                double aceChoiceFactor = rng.nextDouble();
                aceChoiceFactor += EACH_ACE_IN_DECK_WEIGHT * cardsOfValue(Card.CardValue.Ace, cardsToPut);
                aceChoiceFactor -= EACH_CARD_DECREASE_ACE_WEIGHT * cardsCount();
                for (Card.CardSuit suit : Card.CardSuit.values())
                    if (cardFactor[Card.getCardIndex(Card.CardValue.Ace, suit)] != 0)
                        cardFactor[Card.getCardIndex(Card.CardValue.Ace, suit)] = aceChoiceFactor > NON_ACE_THRESHOLD ? HIGH_CARD_FACTOR : LOW_CARD_FACTOR;
            }
            double maxCardFactor = 0;
            for (int j = 0; j < cardFactor.length; j++)
                if (cardFactor[j] > maxCardFactor) {
                    maxCardFactor = cardFactor[j];
                }
            List<Integer> considerableCards = new ArrayList<Integer>();
            for (int j = 0; j < cardFactor.length; j++) {
                if (cardFactor[j] > 0 && maxCardFactor - cardFactor[j] < MAX_DIFF_TO_CONSIDER_CARD)
                    considerableCards.add(j);
            }
            cardsToPut[i] = considerableCards.get(rng.nextInt(considerableCards.size()));
        }
        for (int i = 0; i < cardsToPut.length; i++) {
            int j = rng.nextInt(cardsToPut.length);
            int t = cardsToPut[i];
            cardsToPut[i] = cardsToPut[j];
            cardsToPut[j] = t;
        }
        return cardsToPut;
    }

    @Override
    public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount,
                                             int actualCardsCount, List<Card.CardValue> valuesInGame) {
        Random rnd = new Random();
        double checkThreshold = DEFAULT_CHECK_THRESHOLD
                                + EACH_ACTUAL_CARD_CHECK_WEIGHT * actualCardsCount
                                + EACH_KNOWN_DECLARED_CARD_CHECK_WEIGHT * (hadCardsOfDeclaredValue - cardsOfValue(declaredCard))
                                - EACH_DROPPED_CARD_VALUE_CHECK_WEIGHT * (Card.CardValue.values().length - valuesInGame.size())
                                /* STATS INFLUENCE ON CHECK DECISION */
                                + HumanStats.getLieChanceOnLapConfidence(currentLap)
                                  * (HumanStats.getLieChanceOnLap(currentLap) - 0.5) * CHECK_STATS_WEIGHT
                                + HumanStats.getLieChanceOnCardsCountConfidence(actualCardsCount)
                                  * (HumanStats.getLieChanceOnCardsCount(actualCardsCount) - 0.5) * CHECK_STATS_WEIGHT;
        double checkFactor = rnd.nextDouble();
        if (checkFactor < checkThreshold || !hasCards())  //check
            return new DependentTurnResult(true, rnd.nextInt(actualCardsCount), null);
        else
            return new DependentTurnResult(false, -1, chooseCardsToPut(cardsOnBoardCount, valuesInGame, valuesInGame.indexOf(declaredCard)));
    }

    // laps logic
    int currentLap;
    int lapStarterIndex;
    int previousPlayerIndex;
    // lap is the time period between two turns of the lap starter (player at lapStarterIndex)
    // lap == 0 is the situation when the lap starter has not made another turn yet

    @Override
    public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCardValue, int actualCardsCount) {
        if (!ignoreMyOwnFirstTurnNotification) {
            hadCardsOfDeclaredValue = cardsOfValue(declaredCardValue);
            ignoreMyOwnFirstTurnNotification = false; //is not used anywhere else, will be set into true on next first turn
        }
        // laps logic
        lapStarterIndex = currentPlayerIndex;
        currentLap = 0;
        //
        previousPlayerIndex = currentPlayerIndex;
    }

    @Override
    public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, boolean checkedLie, int actualCardsCount) {
        //gathering stats
        if (currentGamePlayersInfo[currentPlayerIndex].isHuman)
            HumanStats.recordCheckStats(isChecking, currentLap); //gather checking stats
        if (currentGamePlayersInfo[previousPlayerIndex].isHuman && isChecking)  //gather lie stats
            HumanStats.recordLieStats(checkedLie, currentLap, actualCardsCount);

        //laps logic
        if (currentPlayerIndex == lapStarterIndex)
            currentLap++;

        previousPlayerIndex = currentPlayerIndex;
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
