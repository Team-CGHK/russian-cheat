public abstract class Player {
    private boolean[] cards;

    private String name;

    public String getName() {
        return name;
    }

    abstract public FirstTurnResult firstTurn();

    abstract public DependentTurnResult dependentTurn(int declaredCard, int cardsOnBoardCount, int actualCardsCount);

    public class DeckException extends Exception {
        //TODO decide, which additional info should be stored. May be, card value and/or entire player object?
        DeckException(String msg) {
            super(msg);
        }
    }

    class FirstTurnResult {
        int declaredCardValue;
        int[] cards;
    }

    class DependentTurnResult {
        boolean isChecking;
        int cardToCheck;
        int[] cards;
    }

    public void takeCard(int card) throws DeckException {
        if (!cards[card])
            cards[card] = true;
        else {
            throw new DeckException("The deck already has this card");
        }
    }

    public boolean hasCards() {
        boolean result = false;
        for (int i = 0; i < cards.length && !result; i++)
            result |= cards[i];
        return result;
    }
}