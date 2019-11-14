package com.lhf.server;

import com.lhf.interfaces.ConnectionListener;
import com.lhf.interfaces.MessageListener;
import com.lhf.interfaces.ServerInterface;
import com.lhf.interfaces.UserListener;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.in.ExitMessage;
import com.lhf.messages.in.InMessage;
import com.lhf.messages.out.*;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

public class Server extends Thread implements ServerInterface, MessageListener, ConnectionListener {
    private int port;
    private ServerSocket socket;
    private UserManager userManager;
    private ClientManager clientManager;
    private Logger logger;
    ArrayList<UserListener> userListeners;

    public Server(int port, UserManager userManager) throws IOException {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.port = port;
        socket = new ServerSocket(port);
        this.userManager = userManager;
        userListeners = new ArrayList<UserListener>();
        clientManager = new ClientManager();
        this.logger.exiting(this.getClass().toString(), "Constructor");
    }

    public void run() {
        this.logger.info("Server start!");
        while (true) {
            try {
                Socket connection = socket.accept();
                this.logger.finer("Connection made");
                ClientID id = new ClientID();
                ClientHandle handle = new ClientHandle(connection, id, this);
                clientManager.addClient(id, handle);
                handle.registerCallback(this);
                this.logger.fine("Starting handle");
                handle.start();
                //notifyConnectionListeners(id);
                handle.sendMsg(new WelcomeMessage());
            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @Override
    public void registerCallback(UserListener listener) {
        userListeners.add(listener);
    }

    @Override
    public void sendMessageToUser(OutMessage msg, @NotNull UserID id) {
        logger.fine("Sending message\"" + msg + "\" to User " + id);
        sendMessageToClient(msg, userManager.getClient(id));
    }

    public void sendMessageToClient(OutMessage msg, @NotNull ClientID id) {
        logger.finest("Sending message \"" + msg + "\" to Client " + id);
        clientManager.getConnection(id).sendMsg(msg);
    }

    @Override
    public void sendMessageToAll(OutMessage msg) {
        logger.entering(this.getClass().toString(), "sendMessageToAll()", msg);
        for (ClientHandle client: clientManager.getHandles()) {
            client.sendMsg(msg);
        }
    }

    @Override
    public void sendMessageToAllExcept(OutMessage msg, UserID id) {
        logger.entering(this.getClass().toString(), "sendMessageToAllExcept()");
        logger.fine("Message:\"" + msg + "\" Except:" + id);
        for (UserID userId: userManager.getAllUserIds()) {
            if (userId != id) {
                clientManager.getConnection(userManager.getClient(userId));
            }
        }
    }

    @Override
    public void removeUser(UserID id) {
        logger.entering(this.getClass().toString(), "removeUser()", id);
        userManager.removeUser(id);
        removeClient(userManager.getClient(id));
    }

    public void removeClient(ClientID id) {
        try {
            logger.finer("Removing Client " + id);
            clientManager.removeClient(id);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageReceived(ClientID id, InMessage msg) {
        logger.entering(this.getClass().toString(), "messageReceived()");
        Optional<UserID> user = clientManager.getUserForClient(id);
        if (msg instanceof ExitMessage) {
            logger.info("That was an exit message");
            //TODO: some goodbye message here?
            removeClient(id);
        } else {
            // if there is a User associated with the sending Client, tell UserListener (e.g. Game) about it
            user.ifPresent(userID -> {
                for (UserListener listener : userListeners) {
                    listener.messageReceived(userID, msg);
                }
            });
            // if there is no associated User...
            if (user.isEmpty()) {
                if (msg instanceof CreateInMessage) {
                    logger.fine("Creating new user");
                    UserID new_user = userManager.addUser((CreateInMessage) msg, id);
                    if(new_user != null) {
                        clientManager.addUserForClient(id, new_user);
                        sendMessageToUser(new HelpMessage(), new_user);
                    }else{
                        logger.fine("Duplicate user not allowed");
                        sendMessageToClient(new DuplicateUserMessage(), id);
                    }
                } else {
                    // but it was a recognized command
                    logger.fine("Sending NoUserMessage to client");
                    sendMessageToClient(new NoUserMessage(), id);
                }
            }
        }
    }

    private void notifyConnectionListeners(ClientID id) {
        logger.entering(this.getClass().toString(), "notifyConnectionListeners()", id);
        //This will only take action if they have created a user
        clientManager.getUserForClient(id).ifPresent(userID -> {
            logger.info("Notifying that a user has connected! " + userID);
            for (UserListener listener : userListeners) {
                logger.finest("Notifying userListener " + listener);
                listener.userConnected(userID);
            }
        });
    }

    @Override
    public void userConnected(ClientID id) {
        logger.info("User connected");
    }

    /**
     * This will notify other created Users that a User has left.
     * @param id id of the User who has left
     */
    @Override
    public void userLeft(ClientID id) {
        logger.entering(this.getClass().toString(), "userLeft()", id);
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener: userListeners) {
                listener.userLeft(userID);
            }
        });
    }
}
