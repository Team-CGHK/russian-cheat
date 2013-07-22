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
        int onBoardCardsCount = 0;
        while (currentGameState == GameState.hasStarted) {
            if (cardsOnBoard.size() == 0) {
                Player.FirstTurnResult result = players[currentPlayerIndex].firstTurn();
                declaredCard = result.declaredCardValue;
                cardsOnBoard.add(result.cards);
                onBoardCardsCount = result.cards.length;
                for (int card : result.cards)
                    players[currentPlayerIndex].dropCard(card);
            } else {

                Player.DependentTurnResult result = players[currentPlayerIndex].dependentTurn
                        (declaredCard, onBoardCardsCount, cardsOnBoard.get(cardsOnBoard.size() - 1).length);
                if (result.isChecking) {
                    // if the guess is wrong (the checked card is a card of the declared value), all the cards on board
                    // go to the previous player, and the next turn will be the current player's turn.
                    // otherwise, current player takes the cards and loses his turn;
                    if (Card.getCardValue(cardsOnBoard.get(cardsOnBoard.size() - 1)[result.cardToCheck]) != declaredCard)  // != declaredCard
                        currentPlayerIndex = (currentPlayerIndex+players.length-1)%players.length; //previous player
                    for (int[] cardLayer : cardsOnBoard)
                        for (int card : cardLayer)
                            players[currentPlayerIndex].takeCard(card);
                    DropFourIdenticalValueCards();
                    //may be, the best way is to check only values, present in cardsOnBoard. use Player.dropCard(..);
                    cardsOnBoard.clear();
                } else {
                    cardsOnBoard.add(result.cards);
                    onBoardCardsCount +=result.cards.length;
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

    private void DropFourIdenticalValueCards() throws Player.DeckException  {

       boolean [] CardValues = new boolean [13];
       for (int [] c: cardsOnBoard) {
           for (int one : c)   {
               one = Card.getCardValue(one).ordinal();
               if (!CardValues[one]) {// it means this card has not been tested
                   CardValues[one] = true;
                    int count = 0;
                    count+=players[currentPlayerIndex].cardsOfValue(Card.CardValue.values()[one]);
                    //This is sufficient because all that was on the table is already in the player's cards
                    if (count==4)  {
                        for (Card.CardSuit suit : Card.CardSuit.values())
                            players[currentPlayerIndex].dropCard(Card.getCardIndex(Card.getCardValue(one), suit));
                    }
               }
           }
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
                //TODO use checkPlayersStates in game process and actualize ignoring player without cards later in a game
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
