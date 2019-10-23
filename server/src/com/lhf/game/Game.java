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

import java.util.logging.Logger;

public class Game implements UserListener {
    ServerInterface server;
    UserManager userManager;
    private Logger logger;

    public Game(ServerInterface server, UserManager userManager) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.server = server;
        this.userManager = userManager;
        server.registerCallback((UserListener) this);
        this.logger.info("Created Game");
        server.start();
    }

    @Override
    public void userConnected(UserID id) {
        this.logger.entering(this.getClass().toString(), "userConnected()", id);
        server.sendMessageToUser(new WelcomeMessage(), id);
        server.sendMessageToAllExcept(new NewInMessage(), id);
    }

    @Override
    public void userLeft(UserID id) {
        this.logger.entering(this.getClass().toString(), "userLeft()", id);
        server.sendMessageToAll(new UserLeftMessage());
    }

    @Override
    public void messageReceived(UserID id, @NotNull InMessage msg) {
        this.logger.entering(this.getClass().toString(), "messageReceived()");
        this.logger.fine("Message:" + msg + " for:" + id);
        User user = userManager.getUser(id);
        if (msg instanceof SayMessage) {
            this.logger.finer("Saying");
            server.sendMessageToAll(new com.lhf.messages.out.SayMessage(((SayMessage)msg).getMessage(), user));
        }
        if (msg instanceof TellMessage) {
            this.logger.finer("Telling");
            TellMessage tellMsg = (TellMessage) msg;
            server.sendMessageToUser(new com.lhf.messages.out.TellMessage(id, tellMsg.getMessage()), tellMsg.getTarget());
        }
        if (msg instanceof ExitMessage) {
            this.logger.finer("Exiting");
            server.removeUser(id);
        }
    }
}
