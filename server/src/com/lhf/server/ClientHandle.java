package com.lhf.server;

import com.lhf.interfaces.ConnectionListener;
import com.lhf.interfaces.MessageListener;
import com.lhf.messages.in.InMessage;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.OutMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ClientHandle extends Thread {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private ClientID id;
    private Consumer onDisconnect;
    private ConnectionListener connectionListener;
    private Logger logger;

    public ClientHandle (Socket client, ClientID id, ConnectionListener connectionListener) throws IOException {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.logger.finest("Creating ClientHandle");
        this.client = client;
        this.id = id;
        this.connectionListener = connectionListener;
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.logger.finest("ClientHandle created");
    }

    public synchronized void registerCallback(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();
        this.logger.finer("Running ClientHandle");
        String value = "";
        try {
            while ((value = in.readLine()) != null) {
                this.logger.fine("message received: " + value);
                Optional<InMessage> opt_msg = InMessage.fromString(value);
                opt_msg.ifPresent(msg -> {
                    this.logger.finer("Post Processing:" + msg);
                    listener.messageReceived(id, msg);
                });
                if (opt_msg.isEmpty()) {
                    this.logger.fine("Message was bad");
                    sendMsg(new BadMessage());
                }
            }
        } catch (IOException e) {
            connectionListener.userLeft(id);
        }
    }

    public synchronized void sendMsg(OutMessage msg) {
        this.logger.entering(this.getClass().toString(), "sendMsg()", msg);
        out.println(msg.toString());
        out.flush();
    }

    public void disconnect() throws IOException {
        this.logger.info("Disconnecting ClientHandler");
        client.close();
    }
}
