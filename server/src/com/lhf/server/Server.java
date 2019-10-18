package com.lhf.server;

import com.lhf.interfaces.ConnectionListener;
import com.lhf.interfaces.MessageListener;
import com.lhf.interfaces.ServerInterface;
import com.lhf.interfaces.UserListener;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.in.ExitMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import com.lhf.messages.in.InMessage;
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
                notifyConnectionListeners(id);
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
        logger.fine("Sending message" + msg + " to " + id);
        sendMessageToClient(msg, userManager.getClient(id));
    }

    public void sendMessageToClient(OutMessage msg, @NotNull ClientID id) {
        logger.finest("Sending message " + msg + " to Client " + id);
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
        logger.fine("Message:" + msg + " Except:" + id);
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
            removeClient(id);
        } else {
            user.ifPresent(userID -> {
                for (UserListener listener : userListeners) {
                    listener.messageReceived(userID, msg);
                }
            });
            if (user.isEmpty()) {
                if (msg instanceof CreateInMessage) {
                    logger.fine("Creating new user");
                    UserID new_user = userManager.addUser((CreateInMessage) msg, id);
                    clientManager.addUserForClient(id, new_user);
                } else {
                    logger.fine("Sending BadMessage to client");
                    sendMessageToClient(new BadMessage(), id);
                }
            }
        }
    }

    private void notifyConnectionListeners(ClientID id) {
        logger.entering(this.getClass().toString(), "notifyConnectionListeners()", id);
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userConnected(userID);
            }
        });
    }

    @Override
    public void userConnected(ClientID id) {
        logger.info("User connected");
    }

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
