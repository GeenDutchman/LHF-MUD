package com.lhf.server.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.out.GameEvent;

public class LoggerSendStrategy implements SendStrategy {
    protected Logger logInstance;
    protected Level sendLevel;

    public LoggerSendStrategy(Logger logInstance, Level sendLevel) {
        this.logInstance = logInstance;
        this.sendLevel = sendLevel != null ? sendLevel : Level.FINE;
    }

    public Level getSendLevel() {
        return sendLevel;
    }

    public void setSendLevel(Level sendLevel) {
        this.sendLevel = sendLevel != null ? sendLevel : Level.FINE;
    }

    @Override
    public void send(GameEvent toSend) {
        if (this.logInstance != null) {
            this.logInstance.log(this.sendLevel, toSend::print);
        } else {
            System.out.println(toSend.print());
            System.out.flush();
        }
    }

}
