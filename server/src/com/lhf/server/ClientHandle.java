package com.lhf.server;

import com.lhf.interfaces.ConnectionListener;
import com.lhf.interfaces.MessageListener;
import com.lhf.user.UserID;
import com.lhf.messages.UserMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientHandle extends Thread {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private UserID id;
    private Consumer onDisconnect;
    private ConnectionListener connectionListener;
    public ClientHandle (Socket client, UserID id, ConnectionListener connectionListener) throws IOException {
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
                UserMessage msg = UserMessage.fromString(value);
                listener.messageReceived(id, msg);
            }
        } catch (IOException e) {
            connectionListener.userLeft(id);
        }
    }

    public synchronized void sendMsg(UserMessage msg) {
        out.println(msg.toString());
        out.flush();
    }

    public void disconnect() throws IOException {
        client.close();
    }
}
