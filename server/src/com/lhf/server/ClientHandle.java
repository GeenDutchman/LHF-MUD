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

public class ClientHandle extends Thread {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private ClientID id;
    private Consumer onDisconnect;
    private ConnectionListener connectionListener;
    public ClientHandle (Socket client, ClientID id, ConnectionListener connectionListener) throws IOException {
        this.client = client;
        this.id = id;
        this.connectionListener = connectionListener;
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
    }

    public synchronized void registerCallback(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();
        String value = "";
        try {
            while ((value = in.readLine()) != null) {
                Optional<InMessage> opt_msg = InMessage.fromString(value);
                opt_msg.ifPresent(msg -> {
                    listener.messageReceived(id, msg);
                });
                if (opt_msg.isEmpty()) {
                    sendMsg(new BadMessage());
                }
            }
        } catch (IOException e) {
            connectionListener.userLeft(id);
        }
    }

    public synchronized void sendMsg(OutMessage msg) {
        out.println(msg.toString());
        out.flush();
    }

    public void disconnect() throws IOException {
        client.close();
    }
}
