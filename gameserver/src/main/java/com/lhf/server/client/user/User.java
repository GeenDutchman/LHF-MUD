package com.lhf.server.client.user;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;

public class User implements CommandInvoker, Comparable<User> {
    private final GameEventProcessorID gameEventProcessorID;
    private UserID id;
    private String username;
    private transient CommandChainHandler successor;
    private transient final Logger logger;

    // private String password;
    private Client client;

    public User(CreateInMessage msg, Client client) {
        this.gameEventProcessorID = new GameEventProcessorID();
        id = new UserID(msg);
        username = msg.getUsername();
        // password = msg.getPassword();
        this.client = client;
        this.logger = Logger.getLogger(String.format("User.%s", this.username));
    }

    public UserID getUserID() {
        return this.id;
    }

    public Client getClient() {
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
    public void setSuccessor(CommandChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        return new EnumMap<>(AMessageType.class);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    @Override
    public synchronized void log(Level level, String msg, Throwable thrown) {
        this.logger.log(level, msg, thrown);
    }

    @Override
    public synchronized void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        this.logger.log(level, thrown, msgSupplier);
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
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public ClientID getClientID() {
        return this.client.getClientID();
    }

    @Override
    public CommandInvoker getInnerCommandInvoker() {
        return this.client;
    }

    @Override
    public int compareTo(User o) {
        if (this == o) {
            return 0;
        }
        return this.username.compareTo(o.getUsername());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("User [id=").append(id).append(", username=").append(username).append("]");
        return builder.toString();
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return Set.of(this.client);
    }

}
