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
    private Player[] players;
    private int[] places;
    private int playersInGame;

    int currentPlayerIndex;

    private enum GameState {
        hasNotStarted, hasStarted, hasFinished
    }

    private GameState currentGameState;
    // board
    private List<int[]> cardsOnBoard;
    private Card.CardValue declaredCard;

    private int deckSize;

    private void StartGame() throws Player.DeckException {
        Deal();
        currentGameState = GameState.hasStarted;
        Random r = new Random();
        currentPlayerIndex = r.nextInt(players.length);
        while (currentGameState == GameState.hasStarted) {
            if (cardsOnBoard.size() == 0) {
                Player.FirstTurnResult result = players[currentPlayerIndex].firstTurn();
                declaredCard = result.declaredCardValue;
                cardsOnBoard.add(result.cards);
                for (int card : result.cards)
                    players[currentPlayerIndex].dropCard(card);
            } else {
                int onBoardCardsCount = 0;
                for (int[] a : cardsOnBoard)
                    onBoardCardsCount += a.length;
                Player.DependentTurnResult result = players[currentPlayerIndex].dependentTurn
                        (declaredCard, onBoardCardsCount, cardsOnBoard.get(cardsOnBoard.size() - 1).length);
                if (result.isChecking) {
                    // if the guess is right, all the cards on board go to the previous player, and the next turn
                    // will be the current player's turn. otherwise, current player takes the cards and loses his turn;
                    if (Card.getCardValue(cardsOnBoard.get(cardsOnBoard.size() - 1)[result.cardToCheck]) == declaredCard)
                        currentPlayerIndex--;
                    for (int[] cardLayer : cardsOnBoard)
                        for (int card : cardLayer)
                            players[currentPlayerIndex].takeCard(card);
                    //TODO a method to check if the player has four cards of any value (make him drop them in this case)
                    //may be, the best way is to check only values, present in cardsOnBoard. use Player.dropCard(..);
                    cardsOnBoard.clear();
                } else {
                    cardsOnBoard.add(result.cards);
                    for (int card : result.cards)
                        players[currentPlayerIndex].dropCard(card);
                }
            }
            currentPlayerIndex++;
            currentPlayerIndex %= players.length;
        }
    }

    private void Deal() throws Player.DeckException {
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
            boolean playerHasCards = places[i] != 0 && players[i].hasCards();
            if (!playerHasCards && places[i] == 0) {
                places[i] = players.length - (--playersInGame);
            }
        }
        if (playersInGame == 1 || isDraw()) {
            currentGameState = GameState.hasFinished;
        }
    }

    private boolean isDraw() {
        //the draw is the situation, when only Aces are present in players' decks
        for (Card.CardValue value : Card.CardValue.values()) {
            if (value != Card.CardValue.Ace)
                for (Player player : players)
                    if (player.cardsOfValue(value) > 0)
                        return false;
        }
        //TODO may be optimized: we can store card values that have been dropped and not check them
        return true;
    }

}
