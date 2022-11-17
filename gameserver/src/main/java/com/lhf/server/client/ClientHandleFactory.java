package com.lhf.server.client;

import java.io.IOException;
import java.net.Socket;
import com.lhf.server.interfaces.ConnectionListener;

public class ClientHandleFactory extends ClientFactory {

    public ClientHandleFactory(ClientManager clientManager, ConnectionListener connectionListener) {
        super(clientManager);
        this.connectionListener = connectionListener;
    }

    protected Socket socket;
    protected ConnectionListener connectionListener;

    public ClientHandleFactory SetSocket(Socket socket) {
        this.socket = socket;
        return this;
    }

    @Override
    public Client build() throws CannotBuildClientException {
        if (this.connectionListener == null) {
            throw new CannotBuildClientException("No connection listener");
        }
        if (this.socket == null) {
            throw new CannotBuildClientException("No socket provided!");
        }
        ClientHandle client = null;
        try {
            client = new ClientHandle(this.socket, this.connectionListener);
        } catch (IOException e) {
            throw new CannotBuildClientException(e);
        }
        this.socket = null; // ready for the next build

        return client;
    }

}
