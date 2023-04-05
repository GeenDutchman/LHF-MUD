package com.lhf;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.lhf.server.Server;
import com.lhf.server.SocketServer;

public class Main {

    public static void main(String[] args) {
        setLogLevel(Level.ALL);
        Logger logger = Logger.getLogger(Main.class.getPackageName());
        logger.log(Level.FINER, "Starting main");
        try {
            logger.entering("com.lhf.Main", "in try catch");
            logger.log(Level.INFO, "Creating Server on port 3001...");
            Server server = new SocketServer(3001);
            server.start();
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
