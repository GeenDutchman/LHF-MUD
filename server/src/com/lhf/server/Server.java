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

public class Server extends Thread implements ServerInterface, MessageListener, ConnectionListener {
    private int port;
    private ServerSocket socket;
    private UserManager userManager;
    private ClientManager clientManager;
    ArrayList<UserListener> userListeners;

    public Server(int port, UserManager userManager) throws IOException {
        this.port = port;
        socket = new ServerSocket(port);
        this.userManager = userManager;
        userListeners = new ArrayList<UserListener>();
        clientManager = new ClientManager();
    }

    public void run() {
        while (true) {
            try {
                Socket connection = socket.accept();
                ClientID id = new ClientID();
                ClientHandle handle = new ClientHandle(connection, id, this);
                clientManager.addClient(id, handle);
                handle.registerCallback(this);
                handle.start();
                notifyConnectionListeners(id);
            } catch (IOException e) {
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
        sendMessageToClient(msg, userManager.getClient(id));
    }

    public void sendMessageToClient(OutMessage msg, @NotNull ClientID id) {
        clientManager.getConnection(id).sendMsg(msg);
    }

    @Override
    public void sendMessageToAll(OutMessage msg) {
        for (ClientHandle client: clientManager.getHandles()) {
            client.sendMsg(msg);
        }
    }

    @Override
    public void sendMessageToAllExcept(OutMessage msg, UserID id) {
        for (UserID userId: userManager.getAllUserIds()) {
            if (userId != id) {
                clientManager.getConnection(userManager.getClient(userId));
            }
        }
    }

    @Override
    public void removeUser(UserID id) {

        userManager.removeUser(id);
        removeClient(userManager.getClient(id));
    }

    public void removeClient(ClientID id) {
        try {
            clientManager.removeClient(id);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageReceived(ClientID id, InMessage msg) {
        Optional<UserID> user = clientManager.getUserForClient(id);
        if (msg instanceof ExitMessage) {
            removeClient(id);
        } else {
            user.ifPresent(userID -> {
                for (UserListener listener : userListeners) {
                    listener.messageReceived(userID, msg);
                }
            });
            if (user.isEmpty()) {
                if (msg instanceof CreateInMessage) {
                    UserID new_user = userManager.addUser((CreateInMessage) msg, id);
                    clientManager.addUserForClient(id, new_user);
                } else {
                    sendMessageToClient(new BadMessage(), id);
                }
            }
        }
    }

    private void notifyConnectionListeners(ClientID id) {
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener : userListeners) {
                listener.userConnected(userID);
            }
        });
    }

    @Override
    public void userConnected(ClientID id) {

    }

    @Override
    public void userLeft(ClientID id) {
        clientManager.getUserForClient(id).ifPresent(userID -> {
            for (UserListener listener: userListeners) {
                listener.userLeft(userID);
            }
        });
    }
}
