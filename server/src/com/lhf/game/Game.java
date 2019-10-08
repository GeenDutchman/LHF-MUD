package com.lhf.game;

import com.lhf.interfaces.MessageListener;
import com.lhf.interfaces.ServerInterface;
import com.lhf.messages.*;
import com.lhf.interfaces.ConnectionListener;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import org.jetbrains.annotations.NotNull;

public class Game implements ConnectionListener, MessageListener {
    ServerInterface server;
    UserManager userManager;
    public Game(ServerInterface server, UserManager userManager) {
        this.server = server;
        this.userManager = userManager;
        server.registerCallback((ConnectionListener) this);
        server.registerCallback((MessageListener) this);
        server.start();
    }

    @Override
    public void userConnected(UserID id) {
        server.sendMessageToUser(new WelcomeMessage(), id);
        server.sendMessageToAllExcept(new NewUserMessage(), id);
    }

    @Override
    public void userLeft(UserID id) {
        server.sendMessageToAll(new UserLeftMessage());
    }

    @Override
    public void messageReceived(UserID id, @NotNull UserMessage msg) {
        if (msg instanceof SayMessage) {
            server.sendMessageToAll(msg);
        }
        if (msg instanceof BadMessage) {
            server.sendMessageToUser(msg, id);
        }
        if (msg instanceof ExitMessage) {
            server.sendMessageToUser(msg, id);
            server.removeUser(id);
        }
    }
}
