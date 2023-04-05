package com.lhf.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.server.client.ClientHandle;

public class SocketServer extends Server implements Runnable {
    private int port;
    private ServerSocket socket;

    public SocketServer(int port) throws IOException {
        super();
        this.logger = Logger.getLogger(this.getClass().getName());
        this.port = port;
        this.socket = new ServerSocket(this.port);
    }

    @Override
    public void start() {
        Thread serverThread = new Thread(this);
        serverThread.start();
    }

    @Override
    public void run() {
        this.logger.log(Level.INFO, "Server Thread start");
        while (true) {
            try {
                Socket connection = this.socket.accept();
                this.logger.log(Level.FINER, "Connection made");
                ClientHandle handle = this.clientManager.newClientHandle(connection, this);
                this.startClient(handle);
                this.logger.log(Level.FINE, "Starting handle");
                Thread clientThread = new Thread(handle);
                clientThread.start();
            } catch (IOException e) {
                logger.log(Level.INFO, e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
