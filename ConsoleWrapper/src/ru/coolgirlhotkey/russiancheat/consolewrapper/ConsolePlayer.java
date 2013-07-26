package ru.coolgirlhotkey.russiancheat.consolewrapper; /**
 * Created by hotkey on 23.07.13.
 */

import ru.coolgirlhotkey.russiancheat.gamemechanics.Card;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsolePlayer extends Player {

    @Override
    public FirstTurnResult firstTurn() {
        try {
            System.out.println("There are no cards on board.");
            // TODO encapsulate player's cards output into a method
            System.out.println("Your cards:");
            for (Card.CardValue value : Card.CardValue.values()) {
                boolean newLine = false;
                for (Card.CardSuit suit : Card.CardSuit.values())
                    if (hasCard(Card.getCardIndex(value, suit))) {
                        System.out.print(value.toString() + suit.toString());
                        newLine = true;
                    }
                if (newLine)
                    System.out.println();
            }
            //end^
            System.out.println("Choose a card value to declare: [2..K]");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String userAnswerInput = br.readLine();
            while (!isCorrectDeclaredCardInput(userAnswerInput)) {
                System.out.println("Wrong input, follow the instruction!\n" +
                        "Choose a card to declare: [2..K]");
                userAnswerInput = br.readLine();
            }
            Card.CardValue declaredCardValue = Card.parseCardValue(userAnswerInput);
            System.out.println("Choose the cards to put on the board by specifying their values only: [2..A]+ e.g. \"2 2 3 A\"");
            int[] cardsToPutIndexes;
            for (; ; ) {
                userAnswerInput = br.readLine();
                //Regex to find cardValueStrings
                List<String> cardStrings = new ArrayList<String>();
                Matcher m = Pattern.compile("[(2-9)(10)JQKA]").matcher(userAnswerInput);
                while (m.find()) {
                    cardStrings.add(m.group());
                }
                //end^
                cardsToPutIndexes = cardStringsToIndexesArray(cardStrings);
                if (cardsToPutIndexes != null)
                    break;
                else
                    System.out.println("Wrong input, follow the instruction!\n" +
                            "Choose the cards to put on the board by specifying their values only: [2..A]+ e.g. \"2 2 3 A\"");
            }
            FirstTurnResult result = new FirstTurnResult();
            result.declaredCardValue = declaredCardValue;
            result.cards = cardsToPutIndexes;
            return result;
        } catch (IOException ex) {
            return null;
        }
        // TODO fix exception handling or tear hotkey's hands off
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

    private boolean isCorrectDeclaredCardInput(String input) {
        return input.matches("[(2-9)(10)JQK]");
    }

    @Override
    public DependentTurnResult dependentTurn(Card.CardValue declaredCard, int cardsOnBoardCount, int actualCardsCount) {
        return null;
    }

    @Override
    public void notifyFirstTurn(int currentPlayerIndex, Card.CardValue declaredCard, int actualCardsCount) {

    }

    @Override
    public void notifyDependentTurn(int currentPlayerIndex, boolean isChecking, int cardToCheck, int showdown, int actualCardsCount) {

    }

}
