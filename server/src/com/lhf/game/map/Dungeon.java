package com.lhf.game.map;

import com.lhf.user.UserID;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Dungeon {
    private Room startingRoom = null;
    private Set<Room> rooms;

    public Dungeon() {
        rooms = new HashSet<>();
    }

    public boolean addNewPlayer(Player p) {
        return startingRoom.addPlayer(p);
    }

    public void setStartingRoom(Room r) {
        startingRoom = r;
    }

    public boolean addRoom(Room r) {
        return rooms.add(r);
    }

    private Room getPlayerRoom(UserID id) {
        for (Room r : rooms) {
            Player p = r.getPlayerInRoom(id);
            if (p != null) {
                return r;
            }
        }
        return null;
    }

    public String goCommand(UserID id, String direction) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon.";
        }
        if(room.exitRoom(id, direction)) {
            return "You went " + direction + ". \r\n\n" + getPlayerRoom(id).toString();
        }
        return "That isn't a valid direction to go.";
    }

    public String examineCommand(UserID id, String name) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon.";
        }
        return room.examine(id, name);
    }

    public String lookCommand(UserID id) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon.";
        }
        return room.toString();
    }

    public Set<UserID> getPlayersInRoom(UserID id) {
        Set<Player> players = Objects.requireNonNull(getPlayerRoom(id)).getAllPlayersInRoom();
        Set<UserID> ids = new HashSet<>();
        for (Player p : players) {
            ids.add(p.getId());
        }
        return ids;
    }
}
