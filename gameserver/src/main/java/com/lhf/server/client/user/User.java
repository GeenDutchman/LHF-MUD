package com.lhf.server.client.user;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public class User implements MessageHandler, ClientMessenger, Comparable<User> {
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

    @Override
    public String getStartTag() {
        return "<user>";
    }

    @Override
    public String getEndTag() {
        return "</user>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + getUsername() + this.getEndTag();
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
        return new EnumMap<>(CommandMessage.class);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (ctx.getUser() == null) {
            ctx.setUser(this);
        }
        return ctx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        return Objects.equals(id, other.id) && Objects.equals(username, other.username);
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        ctx = this.addSelfToContext(ctx);
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

    @Override
    public int compareTo(User o) {
        if (this == o) {
            return 0;
        }
        return this.username.compareTo(o.getUsername());
    }

}
