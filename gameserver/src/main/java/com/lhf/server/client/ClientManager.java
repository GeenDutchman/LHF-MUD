package com.lhf.server.client;

import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.ConnectionListener;
import com.lhf.server.interfaces.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ClientManager {
    private HashMap<ClientID, ClientHandle> clientMap;
    private HashMap<ClientID, UserID> userMap;
    private Logger logger;
    private HashMap<CommandMessage, String> helps;

    public ClientManager() {
        clientMap = new HashMap<>();
        userMap = new HashMap<>();
        logger = Logger.getLogger(this.getClass().getName());
        helps = new HashMap<>();
        helps.put(CommandMessage.EXIT, "This will have you disconnect and leave Ibaif!");
    }

    public ClientHandle newClient(Socket socket, ConnectionListener cl) throws IOException {
        ClientID id = new ClientID();
        ClientHandle ch = new ClientHandle(socket, id, cl);
        this.addClient(id, ch);
        return ch;
    }

    public ClientHandle getConnection(ClientID id) {
        return clientMap.get(id);
    }

    private void addClient(@NotNull ClientID clientID, ClientHandle connection) {
        clientMap.put(clientID, connection);
    }

    public void killClient(ClientID id) {
        if (clientMap.containsKey(id)) {
            clientMap.get(id).kill();
        }
    }

    public void removeClient(ClientID id) throws IOException {
        // disconnect and remove from all
        if (clientMap.containsKey(id)) {
            clientMap.get(id).disconnect();
        }
        clientMap.remove(id);
        userMap.remove(id);
    }

    public void addUserForClient(@NotNull ClientID clientID, @NotNull UserID userId) {
        logger.finer("Pairing client " + clientID + " with user " + userId);
        userMap.put(clientID, userId);
    }

    public Optional<UserID> getUserForClient(ClientID id) {
        logger.finer("Checking if client " + id + " is here.");
        Optional<UserID> result = Optional.ofNullable((userMap.get(id)));
        logger.finer("isHere:" + result.isPresent());
        return result;
    }

    public void sendMessageToAll(OutMessage msg) {
        logger.entering(this.getClass().toString(), "sendMessageToAll()", msg);
        for (ClientHandle client : this.clientMap.values()) {
            client.sendMsg(msg);
        }
    }

    public void sendMessageToAllExcept(OutMessage msg, ClientID id) {
        logger.entering(this.getClass().toString(), "sendMessageToAllExcept()");
        for (ClientID iterId : this.clientMap.keySet()) {
            if (iterId != id) {
                this.clientMap.get(iterId).sendMsg(msg);
            }
        }
    }

}
