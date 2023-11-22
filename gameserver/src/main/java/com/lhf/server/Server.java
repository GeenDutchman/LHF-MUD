package com.lhf.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.Game;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandContext;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.events.messages.MessageHandler;
import com.lhf.game.events.messages.in.CreateInMessage;
import com.lhf.game.events.messages.out.DuplicateUserMessage;
import com.lhf.game.events.messages.out.UserLeftMessage;
import com.lhf.game.events.messages.out.WelcomeMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.ClientManager;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ConnectionListener;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

public class Server implements ServerInterface, ConnectionListener {
    protected Game game;
    protected UserManager userManager;
    protected ClientManager clientManager;
    protected Logger logger;
    protected ArrayList<UserListener> userListeners;
    protected Map<CommandMessage, String> acceptedCommands;

    public Server() throws IOException {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.userManager = new UserManager();
        this.userListeners = new ArrayList<>();
        this.clientManager = new ClientManager();
        this.acceptedCommands = new EnumMap<>(CommandMessage.class);
        this.acceptedCommands.put(CommandMessage.EXIT, "Disconnect and leave Ibaif!");
        this.acceptedCommands.put(CommandMessage.CREATE, "Create a character in Ibaif!");
        this.acceptedCommands = Collections.unmodifiableMap(this.acceptedCommands);
        this.game = new Game(this, this.userManager);
        this.logger.exiting(this.getClass().getName(), "NoArgConstructor");
    }

    public Server(@NotNull UserManager userManager, @NotNull ClientManager clientManager, @NotNull Game game) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.userManager = userManager;
        this.userListeners = new ArrayList<>();
        this.clientManager = clientManager;
        this.acceptedCommands = new EnumMap<>(CommandMessage.class);
        this.acceptedCommands.put(CommandMessage.EXIT, "Disconnect and leave Ibaif!");
        this.acceptedCommands.put(CommandMessage.CREATE, "Create a character in Ibaif!");
        this.acceptedCommands = Collections.unmodifiableMap(this.acceptedCommands);
        this.game = game;
        if (game != null) {
            game.setServer(this);
        }
        this.logger.exiting(this.getClass().getName(), "ArgConstructor");
    }

    public Client startClient(Client client) {
        this.logger.log(Level.FINER, "Sending welcome");
        client.setSuccessor(this);
        client.sendMsg(WelcomeMessage.getWelcomeBuilder().Build());
        return client;
    }

    public void start() {
    }

    @Override
    public void registerCallback(UserListener listener) {
        this.userListeners.add(listener);
    }

    private void removeClient(ClientID id) {
        try {
            logger.log(Level.FINER, "Removing Client " + id);
            clientManager.removeClient(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUser(UserID id) {
        logger.log(Level.FINER, "Removing User " + id);
        userManager.removeUser(id);
    }

    @Override
    public void clientConnected(ClientID id) {
        logger.log(Level.INFO, "User connected");
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userConnected(userID);
            }
        });
    }

    /**
     * This will notify other created Users that a User has left.
     * 
     * @param id id of the User who has left
     */
    @Override
    public void clientLeft(ClientID id) {
        logger.entering(this.getClass().getName(), "userLeft()", id);
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userLeft(userID);
            }
            userManager.removeUser(userID);
        });
    }

    @Override
    public void clientConnectionTerminated(ClientID id) {
        logger.entering(this.getClass().getName(), "connectionTerminated()", id);
        clientLeft(id);
        removeClient(id);
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        // Server is IT, the buck stops here
        logger.log(Level.WARNING, "Attempted to put a successor on the Server");
    }

    @Override
    public MessageHandler getSuccessor() {
        // Server is IT, the buck stops here
        return null;
    }

    @Override
    public Map<CommandMessage, String> getCommands(CommandContext ctx) {
        Map<CommandMessage, String> pruned = new EnumMap<>(this.acceptedCommands);
        if (ctx.getUser() != null) {
            pruned.remove(CommandMessage.CREATE);
        }
        return ctx.addHelps(pruned);
    }

    private CommandContext.Reply handleCreateMessage(CommandContext ctx, CreateInMessage msg) {
        if (this.userManager.getAllUsernames().contains(msg.getUsername())) {
            ctx.sendMsg(DuplicateUserMessage.getBuilder().Build());
            return ctx.handled();
        }
        User user = this.userManager.addUser(msg, ctx.getClientMessenger());
        if (user == null) {
            ctx.sendMsg(DuplicateUserMessage.getBuilder().Build());
            return ctx.handled();
        }
        user.setSuccessor(this);
        Client client = this.clientManager.getConnection(ctx.getClientID());
        this.clientManager.addUserForClient(client.getClientID(), user.getUserID());
        client.setSuccessor(user);
        this.game.addNewPlayerToGame(user, msg.vocationRequest());
        return ctx.handled();
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        ctx = this.addSelfToContext(ctx);
        if (this.getCommands(ctx).containsKey(msg.getType())) {
            if (msg.getType() == CommandMessage.EXIT) {
                this.logger.log(Level.INFO, "client " + ctx.getClientID().toString() + " is exiting");
                Client ch = this.clientManager.getConnection(ctx.getClientID());

                if (ctx.getUserID() != null) {
                    this.game.userLeft(ctx.getUserID());
                    User leaving = this.userManager.getUser(ctx.getUserID());
                    this.userManager.removeUser(ctx.getUserID());
                    leaving.sendMsg(UserLeftMessage.getBuilder().setUser(leaving).setNotBroadcast().Build());
                } else {
                    if (ch != null) {
                        ch.sendMsg(UserLeftMessage.getBuilder().setNotBroadcast().Build());
                    }
                }

                try {
                    this.clientManager.removeClient(ctx.getClientID()); // ch is killed in here
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return ctx.handled();
            }
            if (ctx.getUserID() == null && msg.getType() == CommandMessage.CREATE) {
                CreateInMessage createMessage = (CreateInMessage) msg;
                return this.handleCreateMessage(ctx, createMessage);
            }
        }
        return ServerInterface.super.handleMessage(ctx, msg);
    }

}
