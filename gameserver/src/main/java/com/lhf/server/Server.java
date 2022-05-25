package com.lhf.server;

import com.lhf.game.Game;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.in.ExitMessage;
import com.lhf.messages.out.*;
import com.lhf.server.client.ClientHandle;
import com.lhf.server.client.ClientID;
import com.lhf.server.client.ClientManager;
import com.lhf.server.client.user.UserID;
import com.lhf.server.client.user.UserManager;
import com.lhf.server.interfaces.ConnectionListener;
import com.lhf.server.interfaces.NotNull;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.interfaces.UserListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class Server extends Thread implements ServerInterface, ConnectionListener {
    private int port;
    private ServerSocket socket;
    private Game game;
    private UserManager userManager;
    private ClientManager clientManager;
    private Logger logger;
    private ArrayList<UserListener> userListeners;
    private Map<CommandMessage, String> acceptedCommands;

    public Server(int port) throws IOException {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.port = port;
        this.socket = new ServerSocket(this.port);
        this.userManager = new UserManager();
        this.userListeners = new ArrayList<>();
        this.clientManager = new ClientManager();
        this.acceptedCommands = new TreeMap<>();
        this.acceptedCommands.put(CommandMessage.EXIT, "Disconnect and leave Ibaif!");
        this.game = new Game(this, this.userManager);
        this.logger.exiting(this.getClass().toString(), "Constructor");
    }

    public void run() {
        this.logger.info("Server start!");
        while (true) {
            try {
                Socket connection = this.socket.accept();
                this.logger.finer("Connection made");
                ClientHandle handle = this.clientManager.newClient(connection, this);
                handle.setSuccessor(this);
                this.logger.fine("Starting handle");
                handle.start();
                handle.sendMsg(new WelcomeMessage());
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
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
