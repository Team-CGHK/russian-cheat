package ru.coolgirlhotkey.russiancheat.consolewrapper; /**
 * Created by hotkey on 23.07.13.
 */

import ru.coolgirlhotkey.russiancheat.gamemechanics.Card;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ConsolePlayer extends Player {

    @Override
    public FirstTurnResult firstTurn() {
        try {
            System.out.println("There are no cards on board. Choose a card to declare from [2..K], please:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String userAnswerInput = br.readLine();
            while (!isCorrectDeclaredCardInput(userAnswerInput)) {
                System.out.println("Wrong input, follow the instruction! => Choose a card to declare from [2..K], please:");
                userAnswerInput = br.readLine();
            }
            Card.CardValue declaredCardValue = Card.parseCardValue(userAnswerInput);
            System.out.println("Choose cards by specifying their values only to put on the table from [2..A] for example: 2 2 A.");
            String[] cardValuesToPut = br.readLine().split("\\s+");
            while (!isCorrectCardValuesToPut(cardValuesToPut)) {
                System.out.println("Wrong input, follow the instruction! => Choose cards by specifying their values only to put on the table from [2..A] for example: 2 2 A.");
                cardValuesToPut = br.readLine().split("\\s+");
            }
            int[] indexes = toIndexesArray(cardValuesToPut);
            FirstTurnResult result = new FirstTurnResult();
            result.declaredCardValue = declaredCardValue;
            result.cards = indexes;
            return result;
        } catch (IOException ex) {
            return null;
        }
        //TODO fix exception handling
    }

    //TODO merge methods
    private boolean isCorrectCardValuesToPut(String[] inputCardValues) {
        int[] cardsOfValueChosen = new int[13];
        for (String cardValueStr : inputCardValues) {
            Card.CardValue value = Card.parseCardValue(cardValueStr);
            if (value == null)
                return false;
            cardsOfValueChosen[value.ordinal()]++;
        }
        for (Card.CardValue value : Card.CardValue.values()) {
            if (cardsOfValue(value) < cardsOfValueChosen[value.ordinal()])
                return false;
        }
        return true;
    }

    private int[] toIndexesArray(String[] inputCardValues) {
        int[] cardsOfValueChosen = new int[13];
        int[] result = new int[inputCardValues.length];
        int pos = 0;
        for (String cardValueStr : inputCardValues) {
            Card.CardValue value = Card.parseCardValue(cardValueStr);
            cardsOfValueChosen[value.ordinal()]++;
        }
        for (int i = 0; i < 13; i++) {
            for (Card.CardSuit cardSuit : Card.CardSuit.values()) {
                if (cardsOfValueChosen[i] == 0)
                    break;
                if (this.hasCard(Card.getCardIndex(Card.CardValue.values()[i], cardSuit))) {
                    cardsOfValueChosen[i]--;
                    result[pos++] = (Card.getCardIndex(Card.CardValue.values()[i], cardSuit));
                }
            }
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
