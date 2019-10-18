package com.lhf;

import com.lhf.game.Game;
import com.lhf.server.Server;
import com.lhf.user.UserManager;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Logger mainLogger = Logger.getLogger("com.lhf.Main.main()");
        mainLogger.finer("Creating userManager");
        UserManager userManager = new UserManager();
        try {
            mainLogger.entering("com.lhf.Main", "in try catch");
            mainLogger.info("Creating Server on port 3001...");
            Server server = new Server(3001, userManager);
            mainLogger.finer("Creating Game...");
            Game game = new Game(server, userManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
