public abstract class Player
{
    private boolean[] cards = new boolean[52];

    private String name;
    public String getName {return name;}

    abstract public FirstTurnResult FirstTurn();

    abstract public DependentTurnResult DependentTurn(int declaredCard, boolean[] boardCards, String[] actualCards);

    class FirstTurnResult
    {
        int declaredCardValue;
        String[] cards;
    }

    class DependentTurnResult
    {
        boolean isChecking;
        int cardToCheck;
        String[] cards;
    }
}