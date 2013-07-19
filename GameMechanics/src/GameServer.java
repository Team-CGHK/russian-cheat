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
    private int declaredCard;

    private int deckSize;

    private void StartGame() throws Exception {
        Deal();
        currentGameState = GameState.hasStarted;
        Random r = new Random();
        currentPlayerIndex = r.nextInt(players.length);
        while (currentGameState == GameState.hasStarted) {
            if (cardsOnBoard.size() == 0) {
                Player.FirstTurnResult result = players[currentPlayerIndex].firstTurn();
                declaredCard = result.declaredCardValue;
                cardsOnBoard.add(result.cards);
            } else {
                int onBoardCardsCount = 0;
                for (int[] a : cardsOnBoard)
                    onBoardCardsCount += a.length;
                Player.DependentTurnResult result = players[currentPlayerIndex].dependentTurn
                        (declaredCard, onBoardCardsCount, cardsOnBoard.get(cardsOnBoard.size() - 1).length);
                if (result.isChecking)
                {
                    //TODO checking
                }
                else
                    cardsOnBoard.add(result.cards);
            }
            currentPlayerIndex++;
            currentPlayerIndex %= players.length;
        }
    }

    private void Deal() throws Exception {
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
        //TODO draw situation
        if (playersInGame == 1) {
            currentGameState = GameState.hasFinished;
        }
    }

}
