package com.lhf.game.map;

import com.lhf.game.map.objects.RoomObject;
import com.lhf.user.UserID;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Room {

    private Set<Player> players;
    private Map<String, Room> exits;
    private List<RoomObject> objects;
    private String description;

    public Room(String description) {
        this.description = description;
    }

    public boolean addPlayer(Player p) {
        players.add(p);
        return true;
    }

    public boolean exitRoom(UserID id, String direction) {
        return false;
    }

    public boolean addExit(String direction, Room room) {
        return false;
    }

    public boolean addObject(RoomObject obj) {
        return false;
    }

    public String getDescription() {
        return description;
    }

    public String getListOfAllVisibleObjects() {
        StringBuilder output = new StringBuilder();
        for (RoomObject o : objects) {
            if (o.checkVisibility()) {
                output.append(o.getName());
                output.append(", ");
            }
        }
        return output.toString().substring(0, output.length() - 2);
    }

    public String getListOfAllObjects() {
        StringBuilder output = new StringBuilder();
        for (RoomObject o : objects) {
            output.append(o.getName());
            output.append(", ");
        }
        return output.toString().substring(0, output.length() - 2);
    }

    public boolean examine(UserID id, String name) {
        return false;
    }

    public boolean use(UserID id, String name) {
        return false;
    }

    public boolean obtain(UserID id, String name) {
        return false;
    }
}
