package ru.coolgirlhotkey.russiancheat.consolewrapper; /**
 * Created by hotkey on 23.07.13.
 */

import ru.coolgirlhotkey.russiancheat.gamemechanics.Card;
import ru.coolgirlhotkey.russiancheat.gamemechanics.GameServer;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsolePlayer extends Player {

    public ConsolePlayer(String name, int currentIndex) {
        super(currentIndex);
        super.name = name;
    }

    @Override
    public FirstTurnResult firstTurn(List<Card.CardValue> valuesInGame) {
        System.out.printf("%s's turn: (press Enter)\n", getName());
        try { //catch (IOException)
        System.in.read();
        System.out.println("There are no cards on board.");
        printPlayersCards();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String userAnswerInput;
            String availableCardsOutput = "";
            for (int i = 0; i < valuesInGame.size(); i++)
                availableCardsOutput += valuesInGame.get(i).toString()+" ";
            availableCardsOutput = availableCardsOutput.trim();
            for (; ; ) {
                System.out.printf("Choose a card value to declare: [%s]\n", availableCardsOutput);
                userAnswerInput = br.readLine().toUpperCase();
                if (isCorrectDeclaredCardInput(userAnswerInput, valuesInGame))
                    break;
                else
                    System.out.println("Wrong input, follow the instruction!");
            }
            int declaredCardValueIndex = valuesInGame.indexOf(Card.parseCardValue(userAnswerInput));

            int[] cardsToPutIndexes;
            for (; ; ) {
                System.out.println("Choose the cards to put on the board by specifying their values only: [2..A]+ e.g. \"2 2 3 A\"");
                userAnswerInput = br.readLine().toUpperCase();
                //Regex to find cardValueStrings
                List<String> cardStrings = new ArrayList<String>();
                Matcher m = Pattern.compile("(10)|[(2-9)JQKA]").matcher(userAnswerInput);
                while (m.find()) {
                    cardStrings.add(m.group());
                }
                //end^
                cardsToPutIndexes = cardStringsToIndexesArray(cardStrings);
                if (cardsToPutIndexes != null)
                    break;
                else
                    System.out.println("Wrong input, follow the instruction!");
            }
            for (int i = 0; i<20; i++)
                System.out.println();
            return new FirstTurnResult(declaredCardValueIndex, cardsToPutIndexes);
        } catch (IOException ex) {
            System.out.print(ex.getMessage());
            return null;
        }
        // TODO fix exception handling. It's not fully fixed yet
    }

    //return value is null if and only if the cardStrings input is wrong
    private int[] cardStringsToIndexesArray(List<String> cardStrings) {
        if (cardStrings.size() == 0)
            return null; //no cards in input while at least one is required
        int[] cardsOfValueCount = new int[Card.CardValue.values().length];
        int[] result = new int[cardStrings.size()];
        int pos = 0;
        for (String cardValueStr : cardStrings) {
            Card.CardValue value = Card.parseCardValue(cardValueStr);
            cardsOfValueCount[value.ordinal()]++;
        }
        for (Card.CardValue cardValue : Card.CardValue.values()) {
            for (Card.CardSuit cardSuit : Card.CardSuit.values()) {
                // TODO optimise: for (x : A) loop start is time-expensive, there's no need in inner loop if cardsOfValueCount[i] == 0
                if (cardsOfValueCount[cardValue.ordinal()] == 0)
                    break;
                if (this.hasCard(Card.getCardIndex(cardValue, cardSuit))) {
                    cardsOfValueCount[cardValue.ordinal()]--;
                    result[pos++] = Card.getCardIndex(cardValue, cardSuit);
                }
            }
            if (cardsOfValueCount[cardValue.ordinal()] > 0)
                return null; //player has not enough cards of cardValue
        }
        return result;
    }

    private void printPlayersCards() {
        System.out.println("Your cards:");
        for (Card.CardValue value : Card.CardValue.values()) {
            boolean newLine = false;
            for (Card.CardSuit suit : Card.CardSuit.values())
                if (hasCard(Card.getCardIndex(value, suit))) {
                    System.out.print(value.toString() + suit.toString() + " ");
                    newLine = true;
                }
            if (newLine)
                System.out.println();
        }
    }

    private boolean isCorrectDeclaredCardInput(String input, List<Card.CardValue> availableValues) {
        String pattern = "^";
        for (int i = 0; i < availableValues.size(); i++)
            pattern += "(" + availableValues.get(i).toString() + ")" + (i < availableValues.size() - 1 ? "|" : "");
        pattern += "$";
        return input.matches(pattern);
    }

    @Override
    public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount, int actualCardsCount, List<Card.CardValue> valuesInGame) {
        System.out.printf("%s's turn: (press Enter)\n", getName());
        try {
        System.in.read();
        System.out.printf("The declared card value is %s\n", declaredCard.name());
        printPlayersCards();
        System.out.printf("There are %d cards on board, %d of them are actual\n", cardsOnBoardCount, actualCardsCount);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean isChecking;
            for (; ; ) {
                System.out.print("Do you want to check a card or to put some more cards? Type \"C\" for check, \"P\" for put: ");
                String userAnswerInput = br.readLine().toUpperCase();
                if (userAnswerInput.contains("C")) {
                    isChecking = true;
                    break;
                }
                if (userAnswerInput.contains("P")) {
                    if (!hasCards()) {
                        System.out.println("Sorry, you have no cards to put.");
                        continue;
                    }
                    isChecking = false;
                    break;
                }
                System.out.println("Wrong input, follow the instruction!");
            }
            if (isChecking) {
                int cardToCheck;
                for (; ; ) {
                    System.out.printf("Choose a card to check [1%s%s]:", actualCardsCount > 1 ? ".." : "", actualCardsCount > 1 ? actualCardsCount : "");
                    String userAnswerInput = br.readLine();
                    if (userAnswerInput.matches("[0-9]+")) {
                        cardToCheck = Integer.parseInt(userAnswerInput);
                        if (cardToCheck >= 1 && cardToCheck <= actualCardsCount)
                            break;
                    }
                    System.out.println("Wrong input, follow the instruction!");
                }
                for (int i = 0; i<20; i++)
                    System.out.println();
                return new DependentTurnResult(true, cardToCheck - 1, null);
            }
            //else
            int[] cardsToPutIndexes;
            for (; ; ) {
                System.out.println("Choose the cards to put on the board by specifying their values only: [2..A]+ e.g. \"2 2 3 A\"");
                String userAnswerInput = br.readLine().toUpperCase();
                //Regex to find cardValueStrings
                List<String> cardStrings = new ArrayList<String>();
                Matcher m = Pattern.compile("(10)|[(2-9)JQKA]").matcher(userAnswerInput);
                while (m.find()) {
                    cardStrings.add(m.group());
                }
                //end^
                cardsToPutIndexes = cardStringsToIndexesArray(cardStrings);
                if (cardsToPutIndexes != null)
                    break;
                else
                    System.out.println("Wrong input, follow the instruction!");
            }
            for (int i = 0; i<20; i++)
                System.out.println();
            return new DependentTurnResult(false, -1, cardsToPutIndexes);
        } catch (IOException ex) {
            System.out.print(ex.getMessage());
            return null;
        }
        //TODO the same fix for exception handling
    }

    @Override
    public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount) {
        System.out.printf("%s's notification: %s has declared %s and put %d cards\n", getName(), currentGamePlayersInfo[currentPlayerIndex].name, declaredCard.name(), actualCardsCount);
    }

    @Override
    public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, boolean checkSuccess, int actualCardsCount) {
        if (isChecking)
            System.out.printf("%s's notification: %s has checked a card #%d, it was %s\n", getName(), currentGamePlayersInfo[currentPlayerIndex].name, cardToCheck, Card.getCardValue(showdown).name());
        else
            System.out.printf("%s's notification: %s has put %d cards\n", getName(), currentGamePlayersInfo[currentPlayerIndex].name, actualCardsCount);
    }


    @Override
    public void notifyDroppedCardValues(int playerIndex, List<Card.CardValue> droppedValues) {
        String droppedValuesStrings = "";
        for (int i = 0; i < droppedValues.size(); i++)
            droppedValuesStrings += droppedValues.get(i).name() + (i < droppedValues.size() - 1 ? ", " : "");
        System.out.printf("%s's notification: %s has dropped the following card values: %s\n", getName(), currentGamePlayersInfo[playerIndex].name, droppedValuesStrings);
    }

    @Override
    public void notifyPlayerTakingCards(int playerIndex, int cardsCount) {
        System.out.printf("%s's notification: %s has taken %d cards from the board\n", getName(), currentGamePlayersInfo[playerIndex].name, cardsCount);
    }

    @Override
    public void notifyThisPlayerTakingCards(List<int[]> cards) {
        System.out.printf("%s's notification: you have taken the following cards from the board:\n", getName());
        for (int[] cardLayer : cards) {
            for (int card : cardLayer) {
                System.out.printf("%s%s ", Card.getCardValue(card), Card.getCardSuit(card));
            }
            System.out.println();
        }
    }

    @Override
    public void notifyEndGame(int[] places) {
        System.out.printf("%s's notification: the game has ended, the results:\n", getName());
        GameServer.PlayerInfo[] infosSorted = new GameServer.PlayerInfo[places.length];
        for (int i = 0; i < places.length; i++)
            if (places[i] != 0)
                infosSorted[places[i] - 1] = currentGamePlayersInfo[i];
        for (int i = 0; i < infosSorted.length && infosSorted[i] != null; i++)
            System.out.printf("%d. %s\n", i + 1, infosSorted[i].name);
        for (int i = 0; i < places.length; i++)
            if (places[i] == 0)
                System.out.printf("Loser: %s\n", currentGamePlayersInfo[i].name);

    }

}
