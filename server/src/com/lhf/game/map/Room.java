package com.lhf.game.map;

import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.sharedinterfaces.Examinable;
import com.lhf.game.map.objects.roomobject.abstractclasses.InteractObject;
import com.lhf.game.map.objects.roomobject.abstractclasses.RoomObject;
import com.lhf.user.UserID;

import java.util.*;

public class Room {

    private Set<Player> players;
    private Map<String, Room> exits;
    private List<Item> items;
    private List<RoomObject> objects;
    private String description;

    public Room(String description) {
        this.description = description;
        players = new HashSet<>();
        exits = new HashMap<>();
        items = new ArrayList<>();
        objects = new ArrayList<>();
    }

    public boolean addPlayer(Player p) {
        return players.add(p);
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
    }

    public boolean exitRoom(Player p, String direction) {
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

    public boolean addItem(Item obj) {
        if (items.contains(obj)) {
            return false;
        }
        items.add(obj);
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

    public String getListOfAllVisibleItems() {
        StringJoiner output = new StringJoiner(",");
        for (Item o : items) {
            if (o.checkVisibility()) {
                output.add(o.getName());
            }
        }
        return output.toString();
    }

    public String getListOfAllItems() {
        StringJoiner output = new StringJoiner(",");
        for (Item o : items) {
            output.add(o.getName());
        }
        return output.toString();
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

    public String examine(Player p, String name) {
        for (Item ro : items) {
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

    public String interact(Player p, String name) {
        for (RoomObject ro : objects) {
            if (ro.checkName(name)) {
                if (ro instanceof InteractObject) {
                    InteractObject ex = (InteractObject)ro;
                    return ex.doUseAction(p);
                }
                else {
                    return "You cannot interact with " + name + ".";
                }
            }
        }

        return "You couldn't find " + name + " to interact with.";
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
                "\r\n" +
                "The possible directions are:\r\n";
        output += getDirections();
        output += "\r\n";
        output += "Objects you can see:\r\n";
        output += getListOfAllVisibleObjects();
        output += "\r\n";
        output += "Items you can see:\r\n";
        output += getListOfAllVisibleItems();
        output += "\r\n";
        output += "Players in room:\r\n";
        output += getListOfPlayers();
        return output;
    }


}
