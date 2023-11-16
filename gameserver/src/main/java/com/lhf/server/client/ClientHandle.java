package com.lhf.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;

import com.lhf.messages.out.FatalMessage;
import com.lhf.server.interfaces.ConnectionListener;

public class ClientHandle extends Client implements Runnable {
    private Socket socket;

    private boolean connected;
    private boolean killIt;

    private ConnectionListener connectionListener;
    private BufferedReader in;

    protected ClientHandle(Socket socket, ConnectionListener cl) throws IOException {
        super();
        this.socket = socket;
        this.out = new PrintWriterSendStrategy(socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;
        killIt = false;
        this.connectionListener = cl;
        this.logger.log(Level.FINEST, "ClientHandle created");
    }

    @Override
    public void run() {
        this.logger.log(Level.FINER, "Running ClientHandle");
        String value;
        try {
            while (!this.killIt && ((value = in.readLine()) != null)) {
                this.ProcessString(value);
            }
        } catch (IOException e) {
            sendMsg(new FatalMessage());
            e.printStackTrace();
        } catch (Exception e) {
            sendMsg(new FatalMessage());
            e.printStackTrace();
            throw e;
        } finally {
            connectionListener.clientConnectionTerminated(id); // let connectionListener know that it is over
            this.kill();
        }
    }

    public void kill() {
        this.logger.log(Level.INFO, "Disconnecting ClientHandler");
        this.killIt = true;
        if (connected && socket.isConnected()) {
            try {
                socket.close();
                connected = false;
            } catch (IOException e) {
                this.logger.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    void disconnect() {
        this.logger.log(Level.INFO, "Requesting ClientHandler to stop");
        this.killIt = true;
    }

}
