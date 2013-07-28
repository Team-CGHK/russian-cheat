package ru.coolgirlhotkey.russiancheat.gamemechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 19.07.13
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class GameServer {

    public GameServer(Player[] players) {
        this.players = players;
        places = new int[players.length];
        currentGameState = GameState.hasNotStarted;
        cardsOnBoard = new ArrayList<int[]>();
        deckSize = Card.MAX_DECK_SIZE;
        //TODO decide whether players creation must be encapsulated into GameServer code
    }

    private Player[] players;
    private int[] places;
    private int playersInGame;

    int currentPlayerIndex;

    private enum GameState {
        hasNotStarted, hasStarted, hasFinished
    }

    private GameState currentGameState;

    private List<Card.CardValue> valuesInGame;

    private List<int[]> cardsOnBoard;
    private int onBoardCardsCount;

    private Card.CardValue declaredCard;

    private int deckSize;

    public void startGame() throws Player.DeckException {
        if (currentGameState != GameState.hasNotStarted)
            return;
        //TODO throw an exception may be?
        deal();
        valuesInGame = new ArrayList<Card.CardValue>();
        for (Card.CardValue value : Card.CardValue.values())
            valuesInGame.add(value);
        for (int i = 0; i < players.length; i++) {
            List<Card.CardValue> droppedValues = dropSameValueCards(players[i]);
            if (droppedValues.size() > 0)
                for (Player p : players)
                    p.notifyDroppedCardValues(currentPlayerIndex, droppedValues);
        }
        //for now 4 same value cards will just be dropped. looks like IRL we'd do the same
        currentGameState = GameState.hasStarted;
        Random r = new Random();
        currentPlayerIndex = r.nextInt(players.length);
        while (currentGameState == GameState.hasStarted) {
            if (cardsOnBoard.size() == 0) {
                Player.FirstTurnResult result = players[currentPlayerIndex].firstTurn(valuesInGame.subList(0, valuesInGame.size() - 1));
                declaredCard = valuesInGame.get(result.declaredCardValueIndex % valuesInGame.size());
                cardsOnBoard.add(result.cards);
                onBoardCardsCount = result.cards.length;
                for (int card : result.cards)
                    players[currentPlayerIndex].dropCard(card);
                for (Player player : players) {
                    player.notifyFirstTurn(currentPlayerIndex, declaredCard, result.cards.length);
                }
            } else {
                Player.DependentTurnResult result = players[currentPlayerIndex].dependentTurn
                        (declaredCard, onBoardCardsCount, cardsOnBoard.get(cardsOnBoard.size() - 1).length);
                for (Player player : players) {
                    player.notifyDependentTurn(currentPlayerIndex, result.isChecking, result.isChecking ? result.cardToCheck : -1,
                                               result.isChecking ? cardsOnBoard.get(cardsOnBoard.size() - 1)[result.cardToCheck] : -1,
                                               !result.isChecking ? result.cards.length : -1);
                }
                if (result.isChecking) {
                    // if the guess is wrong (the checked card is a card of the declared value), all the cards on board
                    // go to the previous player, and the next turn will be the current player's turn.
                    // otherwise, current player takes the cards and loses his turn;
                    if (Card.getCardValue(cardsOnBoard.get(cardsOnBoard.size() - 1)[result.cardToCheck]) != declaredCard) {
                        do {
                            currentPlayerIndex = (currentPlayerIndex + players.length - 1) % players.length; //previous player
                        }
                        while (places[currentPlayerIndex] != 0);
                    }
                    for (int[] cardLayer : cardsOnBoard)
                        for (int card : cardLayer)
                            players[currentPlayerIndex].takeCard(card);
                    List<Card.CardValue> droppedValues = dropSameOnBoardValueCards(players[currentPlayerIndex]);
                    if (droppedValues.size() > 0)
                        for (Player p : players)
                            p.notifyDroppedCardValues(currentPlayerIndex, droppedValues);
                    cardsOnBoard.clear();
                    onBoardCardsCount = 0;
                } else {
                    cardsOnBoard.add(result.cards);
                    onBoardCardsCount += result.cards.length;
                    for (int card : result.cards)
                        players[currentPlayerIndex].dropCard(card);
                }
            }
            checkPlayersStates();
            do {
                currentPlayerIndex++;
                currentPlayerIndex %= players.length;
            } while (places[currentPlayerIndex] != 0); // ignoring players with no cards
        }
    }

    private List<Card.CardValue> dropSameValueCards(Player player) throws Player.DeckException {
        List<Card.CardValue> droppedValues = new ArrayList<Card.CardValue>();
        for (int card = 0; card < Card.CardValue.values().length; card++) {
            Card.CardValue valueToCheck = Card.getCardValue(card);
            if (valueToCheck != Card.CardValue.Ace) {
                if (player.cardsOfValue(valueToCheck) == 4) {
                    droppedValues.add(valueToCheck);
                    for (Card.CardSuit suit : Card.CardSuit.values())
                        player.dropCard(Card.getCardIndex(valueToCheck, suit));
                    valuesInGame.remove(valueToCheck);
                }
            }
        }
        return droppedValues;
    }

    private List<Card.CardValue> dropSameOnBoardValueCards(Player player) throws Player.DeckException {
        List<Card.CardValue> droppedValues = new ArrayList<Card.CardValue>();
        boolean[] checkedValues = new boolean[Card.CardValue.values().length];
        for (int[] cardLayer : cardsOnBoard) {
            for (int card : cardLayer) {
                Card.CardValue valueToCheck = Card.getCardValue(card);
                if (valueToCheck != Card.CardValue.Ace && !checkedValues[valueToCheck.ordinal()]) {
                    checkedValues[valueToCheck.ordinal()] = true;
                    if (player.cardsOfValue(valueToCheck) == 4) {
                        droppedValues.add(valueToCheck);
                        for (Card.CardSuit suit : Card.CardSuit.values())
                            player.dropCard(Card.getCardIndex(valueToCheck, suit));
                        valuesInGame.remove(valueToCheck);
                    }
                }
            }
        }
        return droppedValues;
    }


    private void deal() throws Player.DeckException {
        int[] deck = new int[deckSize];
        for (int i = 0; i < deckSize; i++)
            deck[i] = i;
        Random r = new Random();
        for (int i = 0; i < deckSize; i++) {
            int j = r.nextInt(deckSize);
            int t = deck[i];
            deck[i] = deck[j];
            deck[j] = t;
        }
        int cardsPerPlayer = deckSize / players.length;
        for (int i = 0; i < players.length; i++)
            for (int j = 0; j < cardsPerPlayer; j++)
                players[i].takeCard(deck[i * cardsPerPlayer + j]);
        int player = 0;
        for (int i = cardsPerPlayer * players.length; i < deckSize; i++)
            players[player++].takeCard(deck[i]);
    }

    private void checkPlayersStates() {
        for (int i = 0; i < players.length; i++) {
            boolean playerHasCards = places[i] != 0 || players[i].hasCards();
            if (!playerHasCards && places[i] == 0) {
                places[i] = players.length - (--playersInGame);
            }
        }
        if (playersInGame == 1 || isDraw()) {
            currentGameState = GameState.hasFinished;
            //TODO notify the players about end game
        }
    }

    private boolean isDraw() {
        //the draw is the situation, when only Aces are present in players' decks
        return valuesInGame.size() == 1;
    }

}
