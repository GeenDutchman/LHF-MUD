package com.lhf.server.client;

import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.Client.ClientID;

public interface CommandInvoker extends CommandChainHandler {
    CommandInvoker getInnerCommandInvoker();

    default ClientID getClientID() {
        return this.getInnerCommandInvoker().getClientID();
    }

    public void log(Level level, String msg, Throwable thrown);

    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier);

}