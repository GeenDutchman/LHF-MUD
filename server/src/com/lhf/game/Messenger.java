package com.lhf.game;

import com.lhf.game.map.Dungeon;
import com.lhf.interfaces.ServerInterface;
import com.lhf.messages.out.OutMessage;
import com.lhf.user.UserID;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Messenger {
    private ServerInterface server;
    private Dungeon dungeon;

    public Messenger(ServerInterface server, Dungeon dungeon) {
        this.server = server;
        this.dungeon = dungeon;
    }

    public void sendMessageToUser(OutMessage msg, @NotNull UserID id) {
        server.sendMessageToUser(msg, id);
    }

    public void sendMessageToAllInRoom(OutMessage msg, @NotNull UserID id) {
        Set<UserID> ids = dungeon.getPlayersInRoom(id);
        for (UserID playerID : ids) {
            server.sendMessageToUser(msg, playerID);
        }
    }

    public void sendMessageToAllInRoomExceptPlayer(OutMessage msg, @NotNull UserID id) {
        Set<UserID> ids = dungeon.getPlayersInRoom(id);
        for (UserID playerID : ids) {
            if (!id.equals(playerID)) {
                server.sendMessageToUser(msg, playerID);
            }
        }
    }
}
