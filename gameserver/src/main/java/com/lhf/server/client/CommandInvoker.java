package com.lhf.server.client;

import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.Client.ClientID;

public interface CommandInvoker extends CommandChainHandler {
    CommandInvoker getInnerCommandInvoker();

    default ClientID getClientID() {
        return this.getInnerCommandInvoker().getClientID();
    }

}