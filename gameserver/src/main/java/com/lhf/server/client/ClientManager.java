package com.lhf.server.client;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.events.GameEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.ConnectionListener;
import com.lhf.server.interfaces.NotNull;

public class ClientManager {
    private HashMap<ClientID, Client> clientMap;
    private HashMap<ClientID, UserID> userMap;
    private Logger logger;
    private HashMap<AMessageType, String> helps;

    public ClientManager() {
        clientMap = new HashMap<>();
        userMap = new HashMap<>();
        logger = Logger.getLogger(this.getClass().getName());
        helps = new HashMap<>();
        helps.put(AMessageType.EXIT, "This will have you disconnect and leave Ibaif!");
    }

    public ClientHandle newClientHandle(Socket socket, ConnectionListener cl) throws IOException {
        ClientHandle ch = new ClientHandle(socket, cl);
        this.addClient(ch);
        return ch;
    }

    public Client newClient(ConnectionListener cl) throws IOException {
        Client ch = new Client();
        this.addClient(ch);
        return ch;
    }

    public Client getConnection(ClientID id) {
        return clientMap.get(id);
    }

    private void addClient(Client connection) {
        clientMap.put(connection.getClientID(), connection);
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
        logger.log(Level.FINER, "Pairing client " + clientID + " with user " + userId);
        userMap.put(clientID, userId);
    }

    public Optional<UserID> getUserForClient(ClientID id) {
        logger.log(Level.FINER, "Checking if client " + id + " is here.");
        Optional<UserID> result = Optional.ofNullable((userMap.get(id)));
        logger.log(Level.FINER, "isHere:" + result.isPresent());
        return result;
    }

    public void sendMessageToAll(GameEvent event) {
        logger.entering(this.getClass().getName(), "sendMessageToAll()", event);
        for (Client client : this.clientMap.values()) {
            Client.eventAccepter.accept(client, event);
        }
    }

    public void sendMessageToAllExcept(GameEvent event, ClientID id) {
        logger.entering(this.getClass().getName(), "sendMessageToAllExcept()");
        for (ClientID iterId : this.clientMap.keySet()) {
            if (iterId != id) {
                Client.eventAccepter.accept(this.clientMap.get(iterId), event);
            }
        }
    }

}
