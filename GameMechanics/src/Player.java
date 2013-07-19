import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;


public abstract class Player
{
    private boolean[] cards;

    private String name;
    public String getName() {return name;}

    abstract public FirstTurnResult firstTurn();

    abstract public DependentTurnResult dependentTurn(int declaredCard, int cardsOnBoardCount, int actualCardsCount);

    public class DeckException extends Exception{
        String Message;
        DeckException(String msg){
         Message = msg;
        }
    }
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

    public void takeCard(int card) throws DeckException
    {
        if (!cards[card])
            cards[card] = true;
        else
        {
            throw new DeckException("Error! The deck has only one copy of this card!");
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