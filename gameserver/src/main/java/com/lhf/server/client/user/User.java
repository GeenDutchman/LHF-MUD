package com.lhf.server.client.user;

import java.util.Map;
import java.util.TreeMap;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public class User implements MessageHandler, ClientMessenger { // TODO: what is the difference between MessageHandler
                                                               // and ClientMessenger
    private UserID id;
    private String username;
    private MessageHandler successor;

    // private String password;
    private ClientMessenger client;

    public User(CreateInMessage msg, ClientMessenger client) {
        id = new UserID(msg);
        username = msg.getUsername();
        // password = msg.getPassword();
        this.client = client;
    }

    public UserID getUserID() {
        return this.id;
    }

    public ClientMessenger getClient() {
        return this.client;
    }

    public String getUsername() {
        return username;
    }

    public String getColorTaggedUsername() {
        return "<player>" + getUsername() + "</player>";
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return new TreeMap<>();
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        ctx.setUserID(id);
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    @Override
    public void sendMsg(OutMessage msg) {
        this.client.sendMsg(msg);
    }

    @Override
    public ClientID getClientID() {
        return this.client.getClientID();
    }
}
