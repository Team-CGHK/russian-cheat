package ru.coolgirlhotkey.russiancheat.consolewrapper; /**
 * Created by hotkey on 23.07.13.
 */
import ru.coolgirlhotkey.russiancheat.gamemechanics.Card;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

public class ConsolePlayer extends Player {

    @Override
    public Player.FirstTurnResult firstTurn() {
        return null;
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
