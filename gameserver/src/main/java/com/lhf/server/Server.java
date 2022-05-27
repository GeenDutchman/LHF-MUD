package com.lhf.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.lhf.game.Game;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.GameMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.ClientHandle;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.ClientManager;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ConnectionListener;
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
        this.acceptedCommands = new TreeMap<>();
        this.acceptedCommands.put(CommandMessage.EXIT, "Disconnect and leave Ibaif!");
        this.game = new Game(this, this.userManager);
        this.logger.exiting(this.getClass().toString(), "Constructor");
    }

    public Client startClient(Client client) {
        this.logger.finer("Sending welcome");
        client.setSuccessor(this);
        client.sendMsg(new WelcomeMessage());
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
            logger.finer("Removing Client " + id);
            clientManager.removeClient(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUser(UserID id) {
        logger.finer("Removing User " + id);
        userManager.removeUser(id);
    }

    @Override
    public void userConnected(ClientID id) {
        logger.info("User connected");
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
    public void userLeft(ClientID id) {
        logger.entering(this.getClass().toString(), "userLeft()", id);
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userLeft(userID);
            }
            userManager.removeUser(userID);
        });
    }

    @Override
    public void connectionTerminated(ClientID id) {
        logger.entering(this.getClass().toString(), "connectionTerminated()", id);
        userLeft(id);
        removeClient(id);
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        // Server is IT, the buck stops here
        logger.warning("Attempted to put a successor on the Server");
    }

    @Override
    public MessageHandler getSuccessor() {
        // Server is IT, the buck stops here
        return null;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return this.acceptedCommands;
    }

    @Override
    public Map<CommandMessage, String> gatherHelp() {
        return ServerInterface.super.gatherHelp();
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.EXIT) {
            this.logger.info("client " + ctx.getClientID().toString() + " is exiting");
            this.userManager.removeUser(ctx.getUserID());
            this.game.userLeft(ctx.getUserID());
            ClientHandle ch = this.clientManager.getConnection(ctx.getClientID());
            if (ch != null) {
                ch.sendMsg(new GameMessage("Goodbye, we hope to see you again soon!"));
            }
            try {
                this.clientManager.removeClient(ctx.getClientID()); // ch is killed in here
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return ServerInterface.super.handleMessage(ctx, msg);
    }

}
