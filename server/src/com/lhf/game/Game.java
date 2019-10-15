package com.lhf.game;

import com.lhf.interfaces.ServerInterface;
import com.lhf.interfaces.UserListener;
import com.lhf.messages.in.*;
import com.lhf.messages.out.BadMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.messages.out.NewInMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.user.User;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import org.jetbrains.annotations.NotNull;

public class Game implements UserListener {
    ServerInterface server;
    UserManager userManager;
    public Game(ServerInterface server, UserManager userManager) {
        this.server = server;
        this.userManager = userManager;
        server.registerCallback((UserListener) this);
        server.start();
    }

    @Override
    public void userConnected(UserID id) {
        server.sendMessageToUser(new WelcomeMessage(), id);
        server.sendMessageToAllExcept(new NewInMessage(), id);
    }

    @Override
    public void userLeft(UserID id) {
        server.sendMessageToAll(new UserLeftMessage());
    }

    @Override
    public void messageReceived(UserID id, @NotNull InMessage msg) {
        User user = userManager.getUser(id);
        if (msg instanceof SayMessage) {
            server.sendMessageToAll(new com.lhf.messages.out.SayMessage(((SayMessage)msg).getMessage(), user));
        }
        if (msg instanceof TellMessage) {
            TellMessage tellMsg = (TellMessage) msg;
            server.sendMessageToUser(new com.lhf.messages.out.TellMessage(id, tellMsg.getMessage()), tellMsg.getTarget());
        }
        if (msg instanceof ExitMessage) {
            server.removeUser(id);
        }
    }
}
