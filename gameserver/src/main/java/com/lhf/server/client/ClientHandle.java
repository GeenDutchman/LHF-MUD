package com.lhf.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

import com.lhf.messages.out.FatalMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.interfaces.ConnectionListener;

public class ClientHandle extends Client implements Runnable {
    private Socket socket;

    private boolean connected;
    private boolean killIt;

    private ConnectionListener connectionListener;
    private BufferedReader in;

    public ClientHandle(Socket socket, ClientID id, ConnectionListener cl) throws IOException {
        super(id);
        this.logger = Logger.getLogger(this.getClass().getName());
        this.logger.finest("Creating ClientHandle");
        this.socket = socket;
        this.out = new PrintWriterSendStrategy(socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;
        killIt = false;
        this.connectionListener = cl;
        this.logger.finest("ClientHandle created");
    }

    @Override
    public void run() {
        this.logger.finer("Running ClientHandle");
        String value;
        try {
            while (!killIt && ((value = in.readLine()) != null)) {
                this.ProcessString(value);
            }
            disconnect(); // clean up after itself
        } catch (IOException e) {
            sendMsg(new FatalMessage());
            e.printStackTrace();
        } catch (Exception e) {
            sendMsg(new FatalMessage());
            e.printStackTrace();
            throw e;
        } finally {
            connectionListener.connectionTerminated(id); // let connectionListener know that it is over
            this.kill();
        }
    }

    public void kill() {
        this.killIt = true;
    }

    void disconnect() throws IOException {
        this.logger.info("Disconnecting ClientHandler");
        if (connected && socket.isConnected()) {
            socket.close();
            connected = false;
        }
    }

}
