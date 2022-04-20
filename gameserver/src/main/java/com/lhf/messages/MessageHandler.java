package com.lhf.messages;

import java.util.HashMap;
import java.util.Map;

public abstract class MessageHandler {
    private MessageHandler _successor = null;

    public void setSuccessor(MessageHandler successor) {
        this._successor = successor;
    }

    protected MessageHandler getSuccessor() {
        return this._successor;
    }

    public void intercept(MessageHandler interceptor) {
        interceptor.setSuccessor(this._successor);
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands();

    public Map<CommandMessage, String> gatherHelp() {
        Map<CommandMessage, String> myCommands = this.getCommands();
        if (this._successor == null) {
            return myCommands;
        }
        Map<CommandMessage, String> received = this._successor.gatherHelp();
        if (received == null) {
            received = new HashMap<>();
        }
        received.putAll(myCommands); // override received with mine
        return received;
    }

    public Boolean handleMessage(Command msg) {
        if (this._successor != null) {
            return this._successor.handleMessage(msg);
        }
        return false;
    }

}
