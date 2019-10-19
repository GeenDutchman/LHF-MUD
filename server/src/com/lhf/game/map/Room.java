package com.lhf.game.map;

import com.lhf.game.map.objects.RoomObject;
import com.lhf.game.map.objects.interfaces.Examinable;
import com.lhf.game.map.objects.interfaces.Obtainable;
import com.lhf.game.map.objects.interfaces.Usable;
import com.lhf.user.UserID;

import java.util.*;

public class Room {

    private Set<Player> players;
    private Map<String, Room> exits;
    private List<RoomObject> objects;
    private String description;

    public Room(String description) {
        this.description = description;
        players = new HashSet<>();
        exits = new HashMap<>();
        objects = new ArrayList<>();
    }

    public boolean addPlayer(Player p) {
        return players.add(p);
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
    }

    public boolean exitRoom(UserID id, String direction) {
        Player p = getPlayerInRoom(id);
        if (p == null) {
            return false;
        }
        if (!exits.containsKey(direction)) {
            return false;
        }

        Room room = exits.get(direction);
        removePlayer(p);
        room.addPlayer(p);
        return true;
    }

    public boolean addExit(String direction, Room room) {
        if (exits.containsKey(direction))
        {
            return false;
        }
        exits.put(direction, room);
        return true;
    }

    public boolean addObject(RoomObject obj) {
        if (objects.contains(obj)) {
            return false;
        }
        objects.add(obj);
        return true;
    }

    public String getDescription() {
        return description;
    }

    public String getListOfAllVisibleObjects() {
        StringJoiner output = new StringJoiner(",");
        for (RoomObject o : objects) {
            if (o.checkVisibility()) {
                output.add(o.getName());
            }
        }
        return output.toString();
    }

    public String getListOfAllObjects() {
        StringJoiner output = new StringJoiner(",");
        for (RoomObject o : objects) {
            output.add(o.getName());
        }
        return output.toString();
    }

    public String examine(UserID id, String name) {
        for (RoomObject ro : objects) {
            if (ro.checkName(name)) {
                if (ro instanceof Examinable) {
                    Examinable ex = (Examinable)ro;
                    return ex.getDescription();
                }
                else {
                    return "You cannot examine " + name + ".";
                }
            }
        }

        return "You couldn't find " + name + " to examine.";
    }

    public String use(UserID id, String name) {
        for (RoomObject ro : objects) {
            if (ro.checkName(name)) {
                if (ro instanceof Usable) {
                    Examinable ex = (Examinable)ro;
                    return "You used " + name;
                }
                else {
                    return "You cannot use " + name + ".";
                }
            }
        }

        return "You couldn't find " + name + " to use.";
    }

    public String obtain(UserID id, String name) {
        for (RoomObject ro : objects) {
            if (ro.checkName(name)) {
                if (ro instanceof Obtainable) {
                    Examinable ex = (Examinable)ro;
                    return "You tried to obtain " + name + ", but alas, this feature has not been implemented yet.";
                }
                else {
                    return "You cannot obtain " + name + ".";
                }
            }
        }

        return "You couldn't find " + name + " to obtain.";
    }

    public Player getPlayerInRoom(UserID id) {
        for (Player p : players) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public Set<Player> getAllPlayersInRoom() {
        return players;
    }

    public String getDirections() {
        StringJoiner output = new StringJoiner(",");
        for (String s : exits.keySet()) {
            output.add(s);
        }
        return output.toString();
    }

    private String getListOfPlayers() {
        StringJoiner output = new StringJoiner(",");
        for (Player p : players) {
            output.add(p.getId().getUsername());
        }
        return output.toString();
    }

    @Override
    public String toString() {

        String output = getDescription() +
                "\r\n\n" +
                "The possible directions are:\r\n";
        output += getDirections();
        output += "\r\n";
        output += "Objects you can see:\r\n";
        output += getListOfAllVisibleObjects();
        output += "\r\n";
        output += "Players in room:\r\n";
        output += getListOfPlayers();
        return output;
    }


}
