package com.lhf.server;

import com.lhf.interfaces.ConnectionListener;
import com.lhf.interfaces.MessageListener;
import com.lhf.interfaces.ServerInterface;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import com.lhf.messages.UserMessage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread implements ServerInterface, MessageListener, ConnectionListener {
    private int port;
    private ServerSocket socket;
    private UserManager userManager;
    ArrayList<ConnectionListener> connectionListeners;
    ArrayList<MessageListener> messageListeners;

    public Server(int port, UserManager userManager) throws IOException {
        this.port = port;
        socket = new ServerSocket(port);
        this.userManager = userManager;
        connectionListeners = new ArrayList<ConnectionListener>();
        messageListeners = new ArrayList<>();
    }

    public void run() {
        while (true) {
            try {
                Socket connection = socket.accept();
                UserID id = new UserID();
                ClientHandle handle = new ClientHandle(connection, id, this);
                userManager.addUser(id, handle);
                handle.registerCallback(this);
                handle.start();
                notifyConnectionListeners(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void registerCallback(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public void registerCallback(MessageListener listener) {
        messageListeners.add(listener);
    }

    @Override
    public void sendMessageToUser(UserMessage msg, @NotNull UserID id) {
        userManager.getConnection(id).sendMsg(msg);
    }

    @Override
    public void sendMessageToAll(UserMessage msg) {
        for (ClientHandle client: userManager.getHandles()) {
            client.sendMsg(msg);
        }
    }

    @Override
    public void sendMessageToAllExcept(UserMessage msg, UserID id) {
        for (ClientHandle client: userManager.getAllHandlesExcept(id)) {
            client.sendMsg(msg);
        }
    }

    @Override
    public void removeUser(UserID id) {
        try {
            userManager.removeUser(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageReceived(UserID id, UserMessage msg) {
        for (MessageListener listener: messageListeners) {
             listener.messageReceived(id, msg);
        }
    }

    private void notifyConnectionListeners(UserID id) {
        for (ConnectionListener listener: connectionListeners) {
            listener.userConnected(id);
        }
    }

    @Override
    public void userConnected(UserID id) {

    }

    @Override
    public void userLeft(UserID id) {
        for (ConnectionListener listener: connectionListeners) {
            listener.userLeft(id);
        }
    }
}
