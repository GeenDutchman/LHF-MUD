package com.lhf.server.client;

import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.server.client.Client.ClientID;

public interface Controller extends CommandChainHandler {

    CommandContext.Reply ProcessString(String value);

    ClientID getClientID();

}