package ru.coolgirlhotkey.russiancheat.consolewrapper;

import ru.coolgirlhotkey.russiancheat.gamemechanics.GameServer;
import ru.coolgirlhotkey.russiancheat.gamemechanics.Player;

import java.io.BufferedReader;
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
            for (; ; ) {
                System.out.println("Choose players number: [2..4]");
                String userAnswerInput = br.readLine();
                if (userAnswerInput.matches("[2-4]")) {
                    playersCount = Integer.parseInt(userAnswerInput);
                    break;
                }
            }
            Player[] players = new Player[playersCount];
            for (int i = 0; i < playersCount; i++) {
                System.out.printf("Enter a name for player #%d: ", i);
                //TODO when simple AI player will be created, add an option to create AI player instead of ConsolePlayer
                players[i] = new ConsolePlayer(br.readLine());
            }
            GameServer server = new GameServer(players);
            try {
                server.startGame();
            } catch (Player.DeckException ex) {
                System.out.printf(ex.getMessage());
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
