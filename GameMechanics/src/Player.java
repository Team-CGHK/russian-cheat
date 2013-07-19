import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;


public abstract class Player
{
    private boolean[] cards;

    private String name;
    public String getName() {return name;}

    abstract public FirstTurnResult firstTurn();

    abstract public DependentTurnResult dependentTurn(int declaredCard, int cardsOnBoardCount, int actualCardsCount);

    class FirstTurnResult
    {
        int declaredCardValue;
        int[] cards;
    }

    class DependentTurnResult
    {
        boolean isChecking;
        int cardToCheck;
        int[] cards;
    }

    public void takeCard(int card) throws Exception
    {
        if (!cards[card])
            cards[card] = true;
        else
        {
            throw new Exception("Error! The deck has only one copy of this card!");
            //TODO create a child class extending Exception to provide an additional error info in it's fields - DeckException
            //(low priopity)
        }
    }

    public boolean hasCards()
    {
        boolean result = false;
        for (int i = 0; i<cards.length && !result; i++)
            result |= cards[i];
        return result;
    }
}