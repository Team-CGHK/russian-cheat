package ru.coolgirlhotkey.russiancheat.consolewrapper;

import ru.coolgirlhotkey.russiancheat.gamemechanics.AIPlayer;
import ru.coolgirlhotkey.russiancheat.gamemechanics.GameServer;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by hotkey on 27.07.13.
 */
public class ConsoleGameTester {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            int playersCount;
            if (new File("humanstats.hst").exists())
                AIPlayer.loadStatsFromFile("humanstats.hst");
            for (; ; ) {
                System.out.println("Choose players number: [2..4]");
                String userAnswerInput = br.readLine();
                if (userAnswerInput.matches("[2-4]")) {
                    playersCount = Integer.parseInt(userAnswerInput);
                    break;
                }
            }
            String[] playerNames = new String[playersCount];
            boolean[] isPlayerAI = new boolean[playersCount];
            for (int i = 0; i < playersCount; i++) {
                System.out.printf("Enter a name for player #%d or enter \"AI\" to create an AI player: ", i);
                String userAnswerInput = br.readLine();
                playerNames[i] = userAnswerInput;
                isPlayerAI[i] = userAnswerInput.equals("AI");
            }
            GameServer server = new GameServer(playersCount, isPlayerAI, playerNames);
            try {
                server.startGame();
            } catch (Player.DeckException ex) {
                System.out.printf(ex.getMessage());
            }
            AIPlayer.saveStatsToFile("humanstats.hst");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
