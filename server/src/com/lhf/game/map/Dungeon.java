package com.lhf.game.map;

import com.lhf.game.creature.Player;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.server.client.user.UserID;
import com.lhf.server.messages.Messenger;
import com.lhf.server.messages.out.GameMessage;
import com.lhf.server.messages.out.SpawnMessage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dungeon {
    private Room startingRoom = null;
    private Set<Room> rooms;
    private Messenger messenger;

    Dungeon() {
        rooms = new HashSet<>();
    }

    public boolean addNewPlayer(Player p) {
        return startingRoom.addPlayer(p);
    }

    public boolean removePlayer(UserID id) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return true;
        }
        return room.removePlayer(id);
    }

    public boolean removePlayer(Player p) {
        return this.removePlayer(p.getId());
    }

    void reincarnate(Player p) {
        Player p2 = new Player(p.getId(), p.getName());
        addNewPlayer(p2);
        messenger.sendMessageToUser(new GameMessage(
                "*******************************X_X*********************************************\nYou have died. Out of mercy you have been reborn back where you began."),
                p2.getId());
        messenger.sendMessageToUser(new GameMessage(startingRoom.toString()), p2.getId());
    }

    void setStartingRoom(Room r) {
        startingRoom = r;
    }

    boolean addRoom(Room r) {
        r.setMessenger(messenger);
        r.setDungeon(this);
        return rooms.add(r);
    }

    public Room getPlayerRoom(UserID id) {
        for (Room r : rooms) {
            Player p = r.getPlayerInRoom(id);
            if (p != null) {
                return r;
            }
        }
        return null;
    }

    public Player getPlayerById(UserID id) {
        for (Room r : rooms) {
            Player p = r.getPlayerInRoom(id);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    public String goCommand(UserID id, String direction, AtomicBoolean didMove) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon.";
        }

        if (direction.equals("n")) {
            direction = "north";
        } else if (direction.equals("e")) {
            direction = "east";
        } else if (direction.equals("s")) {
            direction = "south";
        } else if (direction.equals("w")) {
            direction = "west";
        }

        if (room.exitRoom(getPlayerById(id), direction)) {
            didMove.set(true);
            return "You went " + direction + ". \r\n" + Objects.requireNonNull(getPlayerRoom(id)).toString();
        } else if (isValidDirection(direction)) {
            return "There's only a wall there.";
        } else {
            return "Couldn't understand that command.";
        }
    }

    private boolean isValidDirection(String direction) {
        if (!direction.equalsIgnoreCase(Directions.NORTH.toString())) {
            if (!direction.equalsIgnoreCase(Directions.SOUTH.toString())) {
                if (!direction.equalsIgnoreCase(Directions.WEST.toString())) {
                    return direction.equalsIgnoreCase(Directions.EAST.toString());
                }
            }
        }
        return true;
    }

    public String examineCommand(UserID id, String name) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon.";
        }
        return room.examine(getPlayerById(id), name);
    }

    public String interactCommand(UserID id, String name) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon.";
        }
        return room.interact(getPlayerById(id), name);
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

    public String takeCommand(UserID id, String name) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon";
        }
        return room.take(getPlayerById(id), name);
    }

    public String dropCommand(UserID id, String name) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon";
        }
        return room.drop(Objects.requireNonNull(getPlayerById(id)), name);
    }

    public void attackCommand(UserID id, String weapon, String target) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            messenger.sendMessageToUser(new GameMessage("You are not in this dungeon"), id);
            return;
        }
        room.attack(Objects.requireNonNull(getPlayerById(id)), weapon, target);
    }

    public String inventory(UserID id) {
        Player player = getPlayerById(id);
        assert player != null;
        return player.printInventory();
    }

    public String equip(UserID id, String itemName, EquipmentSlots slot) {
        Player player = getPlayerById(id);
        assert player != null;
        return player.equipItem(itemName, slot);
    }

    public String unequip(UserID id, EquipmentSlots slot, String weapon) {
        Player player = getPlayerById(id);
        assert player != null;
        return player.unequipItem(slot, weapon);
    }

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
        for (Room r : rooms) {
            r.setMessenger(messenger);
        }
    }

    public String useCommand(UserID id, String usefulItem, String target) {
        Room room = getPlayerRoom(id);
        if (room == null) {
            return "You are not in this dungeon";
        }
        return room.use(getPlayerById(id), usefulItem, target);
    }

    public String statusCommand(UserID id) {
        String ret = Objects.requireNonNull(getPlayerById(id)).getStatus();
        Player p = getPlayerById(id);
        if (p != null && p.isInBattle()) {
            ret += Objects.requireNonNull(getPlayerRoom(id)).getBattleInfo();
        }
        return ret;
    }

    public void notifyAllInRoomOfNewPlayer(UserID id, String name) {
        messenger.sendMessageToAllInRoomExceptPlayer(new SpawnMessage(name), id);
    }
}
