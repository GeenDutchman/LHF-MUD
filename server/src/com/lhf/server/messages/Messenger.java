package com.lhf.server.messages;

import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Room;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.ServerInterface;
import com.lhf.server.messages.out.OutMessage;
import com.lhf.server.interfaces.NotNull;

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

    public void sendMessageToAllInRoom(OutMessage msg, @NotNull Room room) {
        Set<UserID> ids = room.getAllPlayerIDsInRoom();
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
