package ru.coolgirlhotkey.russiancheat.consolewrapper;

/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 30.07.13
 * Time: 12:52
 * To change this template use File | Settings | File Templates.
 */
public class HumanPlayerStatistics {
    private int [] turnToCheck = new int[27];
    private double [] numberOfCardsToPutLieFactor = new double[6];// it's not so important if the number of cards to put
    //is more than five
    private double [] numberOfCardToCheck = new double [53];
    private String  playerName;
    public  void FillTurnToCheck(int turnInLap){
       turnToCheck[turnInLap]++;
    }
    public HumanPlayerStatistics(String name)
    {
        playerName = name;
    }
}
