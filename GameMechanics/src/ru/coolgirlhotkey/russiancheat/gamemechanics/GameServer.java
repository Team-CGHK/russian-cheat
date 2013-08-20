package ru.coolgirlhotkey.russiancheat.gamemechanics;

import ru.coolgirlhotkey.russiancheat.consolewrapper.ConsolePlayer;

import java.util.ArrayList;
import java.util.Collections;
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

    public GameServer(int playersCount, boolean[] isPlayerAI, String[] playerNames) {
        this.players = new Player[playersCount];
        places = new int[players.length];
        playersInfo = new PlayerInfo[players.length];
        for (int playerIndex = 0; playerIndex < playersCount; playerIndex++) {
            players[playerIndex] = isPlayerAI[playerIndex] ?
                    new AIPlayer(playerIndex) :
                    new ConsolePlayer(playerNames[playerIndex], playerIndex);
            playersInfo[playerIndex] = new PlayerInfo(players[playerIndex].getName(), !isPlayerAI[playerIndex]);
            players[playerIndex].currentGamePlayersInfo = playersInfo;
        }
        currentGameState = GameState.hasNotStarted;
        cardsOnBoard = new ArrayList<int[]>();
        deckSize = Card.MAX_DECK_SIZE;
    }

    private Player[] players;
    private PlayerInfo[] playersInfo;

    private int[] places;
    private int playersInGame;

    //private int turnsCount=1;
    //private int turnsCountInLap;

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
        playersInGame = players.length;
        valuesInGame = new ArrayList<Card.CardValue>();
        Collections.addAll(valuesInGame, Card.CardValue.values());
        for (int i = 0; i < players.length; i++) {
            List<Card.CardValue> droppedValues = dropSameValueCards(i);
            if (droppedValues.size() > 0)
                for (Player p : players)
                    p.notifyDroppedCardValues(i, droppedValues);
        }
        //for now 4 same value cards will just be dropped. looks like IRL we'd do the same
        currentGameState = GameState.hasStarted;
        Random r = new Random();
        currentPlayerIndex = r.nextInt(players.length);
        int currentPlayer=0;
        while (currentGameState == GameState.hasStarted) {
            if (cardsOnBoard.size() == 0) {
                List<Card.CardValue> declarableValues = new ArrayList<Card.CardValue>();
                for (Card.CardValue value : valuesInGame)
                    if (value != Card.CardValue.Ace)
                        declarableValues.add(value);
                Player.FirstTurnResult result = players[currentPlayerIndex].firstTurn(declarableValues);
                declaredCard = declarableValues.get(result.declaredCardValueIndex % declarableValues.size());
                cardsOnBoard.add(result.cards);
                onBoardCardsCount = result.cards.length;
                for (int card : result.cards) {
                    takeCardFromPlayer(currentPlayerIndex, card);
                }
                for (Player player : players) {
                    player.notifyFirstTurn(currentPlayerIndex, declaredCard, result.cards.length,
                            (playersInGame - currentPlayer)>1 ? players[currentPlayer+1].cardsCount() :
                                    players[0].cardsCount());
                    currentPlayer++;
                    if (currentPlayer==playersInGame)
                        currentPlayer = 0;
                }
            } else {
                Player.DependentTurnResult result = players[currentPlayerIndex].dependentTurn
                        (declaredCard, onBoardCardsCount, cardsOnBoard.get(cardsOnBoard.size() - 1).length, valuesInGame.subList(0,valuesInGame.size()));
                for (Player player : players) {
                    player.notifyDependentTurn(currentPlayerIndex, result.isChecking, result.isChecking ? result.cardToCheck : -1,
                                               result.isChecking ? cardsOnBoard.get(cardsOnBoard.size() - 1)[result.cardToCheck] : -1,
                                               result.isChecking ? Card.getCardValue(cardsOnBoard.get(cardsOnBoard.size() - 1)[result.cardToCheck]) != declaredCard : false,
                                               !result.isChecking ? result.cards.length : cardsOnBoard.get(cardsOnBoard.size() - 1).length,
                            (playersInGame - currentPlayer)>1 ? players[currentPlayer+1].cardsCount() :
                                    players[0].cardsCount());
                    currentPlayer++;
                    if (currentPlayer==playersInGame)
                        currentPlayer = 0;
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
                    players[currentPlayerIndex].notifyThisPlayerTakingCards(cardsOnBoard);
                    for (int i = 0; i < players.length; i++)
                        if (i != currentPlayerIndex)
                            players[i].notifyPlayerTakingCards(currentPlayerIndex, onBoardCardsCount);
                    for (int[] cardLayer : cardsOnBoard)
                        for (int card : cardLayer)
                            giveCardToPlayer(currentPlayerIndex, card);
                    List<Card.CardValue> droppedValues = dropSameOnBoardValueCards(currentPlayerIndex);
                    if (droppedValues.size() > 0)
                        for (Player p : players)
                            p.notifyDroppedCardValues(currentPlayerIndex, droppedValues);
                    cardsOnBoard.clear();
                    onBoardCardsCount = 0;
                    checkPlayersStates();
                } else {
                    cardsOnBoard.add(result.cards);
                    onBoardCardsCount += result.cards.length;
                    for (int card : result.cards) {
                        takeCardFromPlayer(currentPlayerIndex, card);
                    }
                }
            }
            do {
                currentPlayerIndex++;
                currentPlayerIndex %= players.length;
            } while (places[currentPlayerIndex] != 0); // ignoring players with no cards
        }
    }

    private List<Card.CardValue> dropSameValueCards(int playerIndex) throws Player.DeckException {
        List<Card.CardValue> droppedValues = new ArrayList<Card.CardValue>();
        for (int card = 0; card < Card.CardValue.values().length; card++) {
            Card.CardValue valueToCheck = Card.getCardValue(card);
            if (valueToCheck != Card.CardValue.Ace) {
                if (players[playerIndex].cardsOfValue(valueToCheck) == 4) {
                    droppedValues.add(valueToCheck);
                    for (Card.CardSuit suit : Card.CardSuit.values())
                        takeCardFromPlayer(playerIndex, Card.getCardIndex(valueToCheck, suit));
                    valuesInGame.remove(valueToCheck);
                }
            }
        }
        return droppedValues;
    }

    private List<Card.CardValue> dropSameOnBoardValueCards(int playerIndex) throws Player.DeckException {
        List<Card.CardValue> droppedValues = new ArrayList<Card.CardValue>();
        boolean[] checkedValues = new boolean[Card.CardValue.values().length];
        for (int[] cardLayer : cardsOnBoard) {
            for (int card : cardLayer) {
                Card.CardValue valueToCheck = Card.getCardValue(card);
                if (valueToCheck != Card.CardValue.Ace && !checkedValues[valueToCheck.ordinal()]) {
                    checkedValues[valueToCheck.ordinal()] = true;
                    if (players[playerIndex].cardsOfValue(valueToCheck) == 4) {
                        droppedValues.add(valueToCheck);
                        for (Card.CardSuit suit : Card.CardSuit.values())
                            takeCardFromPlayer(playerIndex, Card.getCardIndex(valueToCheck, suit));
                        valuesInGame.remove(valueToCheck);
                    }
                }
            }
        }
        return droppedValues;
    }

    private void giveCardToPlayer(int playerIndex, int card) throws Player.DeckException {
        players[playerIndex].takeCard(card);
        playersInfo[playerIndex].cardsCount++;
    }

    private void takeCardFromPlayer(int playerIndex, int card) throws Player.DeckException {
        players[playerIndex].dropCard(card);
        playersInfo[playerIndex].cardsCount--;
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
                giveCardToPlayer(i, deck[i * cardsPerPlayer + j]);
        int player = 0;
        for (int i = cardsPerPlayer * players.length; i < deckSize; i++)
            giveCardToPlayer(player++, deck[i]);
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
            for (Player player : players)
                player.notifyEndGame(places);
        }
    }

    private boolean isDraw() {
        //the draw is the situation, when only Aces are present in players' decks
        return valuesInGame.size() == 1;
    }

    public class PlayerInfo {
        public final String name;
        public final boolean isHuman;

        public PlayerInfo(String name, boolean isHuman) {
            this.name = name;
            this.isHuman = isHuman;
        }

        private int cardsCount;

        public int getCardsCount() {return cardsCount;}
    }

}
