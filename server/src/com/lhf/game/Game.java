package com.lhf.game;

import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.game.map.Player;
import com.lhf.interfaces.ServerInterface;
import com.lhf.interfaces.UserListener;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.messages.in.TellMessage;
import com.lhf.messages.in.*;
import com.lhf.messages.out.*;
import com.lhf.user.User;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Game implements UserListener {
    ServerInterface server;
    UserManager userManager;
    Dungeon dungeon;
    private Logger logger;

    public Game(ServerInterface server, UserManager userManager) {
        this.logger = Logger.getLogger(this.getClass().getName());
        dungeon = DungeonBuilder.buildStaticDungeon();
        this.server = server;
        this.userManager = userManager;
        this.userManager.setGame(this);
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
        if (msg instanceof ShoutMessage) {
            this.logger.finer("Shouting");
            server.sendMessageToAll(new com.lhf.messages.out.ShoutMessage(((ShoutMessage) msg).getMessage(), user));
        }
        if (msg instanceof SayMessage) {
            this.logger.finer("Saying");
            this.sendMessageToAllInRoom(new com.lhf.messages.out.SayMessage(((SayMessage) msg).getMessage(), user), id);
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

        if (msg instanceof GoMessage) {
            AtomicBoolean didMove = new AtomicBoolean(false);

            server.sendMessageToUser(
                    new GameMessage(
                        dungeon.goCommand(id, ((GoMessage) msg).getDirection(), didMove)
                    ),
                    id
            );
            if (didMove.get()) {
                sendMessageToAllInRoomExceptPlayer(
                        new GameMessage(
                                id.getUsername() + " has entered the room."
                        ),
                        id
                );
            }
        }

        if (msg instanceof ExamineMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.examineCommand(id, ((ExamineMessage) msg).getThing())
                    ),
                    id
            );
        }
        if (msg instanceof LookMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.lookCommand(id)
                    ),
                    id
            );
        }
        if (msg instanceof InteractMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                            dungeon.interactCommand(id, ((InteractMessage) msg).getObject())
                    ),
                    id
            );
        }
    }

    private void sendMessageToAllInRoom(OutMessage msg, @NotNull UserID id) {
        Set<UserID> ids = dungeon.getPlayersInRoom(id);
        for (UserID playerID : ids) {
            server.sendMessageToUser(msg, playerID);
        }
    }

    private void sendMessageToAllInRoomExceptPlayer(OutMessage msg, @NotNull UserID id) {
        Set<UserID> ids = dungeon.getPlayersInRoom(id);
        for (UserID playerID : ids) {
            if (!id.equals(playerID)) {
                server.sendMessageToUser(msg, playerID);
            }
        }
    }

    public void addNewPlayerToGame(UserID id) {
        Player newPlayer = new Player(id);
        dungeon.addNewPlayer(newPlayer);
    }
}
