package com.lhf.game;

import com.lhf.game.map.Dungeon;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.game.map.Player;
import com.lhf.interfaces.ServerInterface;
import com.lhf.interfaces.UserListener;
import com.lhf.messages.in.*;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.TellMessage;
import com.lhf.messages.out.*;
import com.lhf.user.User;
import com.lhf.user.UserID;
import com.lhf.user.UserManager;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Game implements UserListener {
    ServerInterface server;
    UserManager userManager;
    Dungeon dungeon;
    public Game(ServerInterface server, UserManager userManager) {
        dungeon = DungeonBuilder.buildStaticDungeon();
        this.server = server;
        this.userManager = userManager;
        this.userManager.setGame(this);
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

        if (msg instanceof GoMessage) {
            server.sendMessageToUser(
                    new GameMessage(
                        dungeon.goCommand(id, ((GoMessage) msg).getDirection())
                    ),
                    id
            );
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
