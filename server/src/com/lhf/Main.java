package com.lhf;

import com.lhf.game.Game;
import com.lhf.server.Server;
import com.lhf.user.UserManager;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        try {
            Server server = new Server(3001, userManager);
            Game game = new Game(server, userManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
