package com.lhf;

import com.lhf.game.Game;
import com.lhf.server.Server;
import com.lhf.user.UserManager;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        setLogLevel(Level.ALL);
        Logger logger = Logger.getLogger(Main.class.getPackageName());
        logger.finer("Creating userManager");
        UserManager userManager = new UserManager();
        try {
            logger.entering("com.lhf.Main", "in try catch");
            logger.info("Creating Server on port 3001...");
            Server server = new Server(3001, userManager);
            logger.finer("Creating Game...");
            Game game = new Game(server, userManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setLogLevel(Level theLevel) {
        Logger head = LogManager.getLogManager().getLogger("");
        head.setLevel(theLevel);
        for (Handler h : head.getHandlers()) {
            h.setLevel(theLevel);
        }
        System.out.println("The LogLevel is set to " + theLevel.getName());
    }
}
