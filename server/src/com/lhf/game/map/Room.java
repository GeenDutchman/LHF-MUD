package com.lhf.game.map;

import com.lhf.game.Messenger;
import com.lhf.game.battle.AttackAction;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.Takeable;
import com.lhf.game.map.objects.roomobject.abstractclasses.InteractObject;
import com.lhf.game.map.objects.roomobject.abstractclasses.RoomObject;
import com.lhf.game.map.objects.sharedinterfaces.Examinable;
import com.lhf.messages.out.GameMessage;
import com.lhf.user.UserID;

import java.util.*;


public class Room {

    private Set<Player> players;
    private Map<String, Room> exits;
    private List<Item> items;
    private List<RoomObject> objects;
    private String description;
    private BattleManager battleManager;
    private Map<Creature, Integer> creatures; // how many of what type of monster
    private Messenger messenger;


    public Room(String description) {
        this.description = description;
        players = new HashSet<>();
        exits = new HashMap<>();
        items = new ArrayList<>();
        objects = new ArrayList<>();
        battleManager = new BattleManager(this);
        creatures = new HashMap<>();
    }

    public boolean addPlayer(Player p) {
        return players.add(p);
    }

    public int addCreature(Creature c) {
        if (this.creatures.containsKey(c)) {
            int previous = this.creatures.get(c);
            this.creatures.put(c, previous + 1);
            return previous + 1;
        } else {
            this.creatures.put(c, 1);
            return 1;
        }
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
    }

    public Creature removeCreature(Creature c) {
        if (this.creatures.containsKey(c)) {
            int nextNumber = this.creatures.get(c) - 1;
            if (nextNumber > 0) {
                this.creatures.put(c, nextNumber);
                return c;
            } else {
                this.creatures.remove(c);
                return c;
            }
        }
        return null;
    }

    public boolean exitRoom(Player p, String direction) {
        if (p == null) {
            return false;
        }
        if (!exits.containsKey(direction)) {
            return false;
        }

        if (p.isInBattle()) {
            //TODO: stop her!!?
            messenger.sendMessageToUser(new GameMessage("You are fleeing the battle. Flee!  Flee!\r\n"), p.getId());
            messenger.sendMessageToAllInRoomExceptPlayer(new GameMessage(p.getName() + " has fled the battle!\r\n"), p.getId());
            battleManager.removeCreatureFromBattle(p);
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
        return "<description>" + description + "</description>";
    }

    public String getListOfAllVisibleItems() {
        StringJoiner output = new StringJoiner(", ");
        for (Item o : items) {
            if (o.checkVisibility()) {
                output.add(o.getStartTagName() + o.getName() + o.getEndTagName());
            }
        }
        return output.toString();
    }

    public String getListOfAllItems() {
        StringJoiner output = new StringJoiner(", ");
        for (Item o : items) {
            output.add(o.getStartTagName() + o.getName() + o.getEndTagName());
        }
        return output.toString();
    }

    public String getListOfAllVisibleObjects() {
        StringJoiner output = new StringJoiner(", ");
        for (RoomObject o : objects) {
            if (o.checkVisibility()) {
                output.add("<object>" + o.getName() + "</object>");
            }
        }
        return output.toString();
    }

    public String getListOfAllObjects() {
        StringJoiner output = new StringJoiner(", ");
        for (RoomObject o : objects) {
            output.add("<object>" + o.getName() + "</object>");
        }
        return output.toString();
    }

    public String examine(Player p, String name) {
        for (Item ro : items) {
            if (ro.checkName(name)) {
                if (ro instanceof Examinable) {
                    Examinable ex = (Examinable)ro;
                    return "<description>" + ex.getDescription() + "</description>";
                }
                else {
                    return "You cannot examine <item>" + name + "</item>.";
                }
            }
        }

        for (RoomObject ro : objects) {
            if (ro.checkName(name)) {
                if (ro instanceof Examinable) {
                    Examinable ex = (Examinable)ro;
                    return "<description>" + ex.getDescription() + "</description>";
                }
                else {
                    return "You cannot examine <object>" + name + "</object>.";
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
                    return "<interaction>" + ex.doUseAction(p) + "</interaction>";
                }
                else {
                    return "You try to interact with <object>" + name + "</object>, but nothing happens.";
                }
            }
        }
        for (Item item : items) {
            if (item.checkName(name)) {
                return "You poke at it, but it does nothing.";
            }
        }
        return "You couldn't find " + name + " to interact with.";
    }

    public String use(Player p, String usefulObject, String onWhat) {
        Object indirectObject = null; // indirectObject is the receiver of the action
        if (onWhat != null && onWhat.length() > 0) {
            for (RoomObject ro : objects) {
                if (ro.checkName(onWhat) && ro instanceof InteractObject) {
                    indirectObject = (InteractObject) ro;
                }
            }
            if (indirectObject == null) {
                indirectObject = getCreatureInRoom(onWhat);
            }
            if (indirectObject == null) {
                return "You couldn't find " + onWhat + " to use.";// " + usefulObject + " on.";
            }
        }
        return p.useItem(usefulObject, indirectObject);
    }

    public Player getPlayerInRoom(UserID id) {
        for (Player p : players) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public Creature getCreatureInRoom(String creatureName) {
        for (Creature c : this.creatures.keySet()) {
            if (c.getName().equalsIgnoreCase(creatureName)) {
                return c;
            }
        }

        // for PvP
        for (Player p : players) {
            if (p.getName().equals(creatureName)) {
                return p;
            }
        }
        return null;
    }

    public Set<Player> getAllPlayersInRoom() {
        return players;
    }

    public Set<UserID> getAllPlayerIDsInRoom() {
        Set<UserID> ids = new HashSet<>();
        for (Player player : players) {
            ids.add(player.getId());
        }
        return ids;
    }

    public String getDirections() {
        StringJoiner output = new StringJoiner(", ");
        for (String s : exits.keySet()) {
            output.add("<exit>" + s + "</exit>");
        }
        return output.toString();
    }

    private String getListOfPlayers() {
        StringJoiner output = new StringJoiner(", ");
        for (Player p : players) {
            output.add(p.getColorTaggedName());
        }
        return output.toString();
    }

    private String getListOfCreatures() {
        StringJoiner output = new StringJoiner(", ");
        output.setEmptyValue("None.");
        for (Creature c : creatures.keySet()) {
            Integer numOf = this.creatures.get(c);
            if (numOf > 1) {
                output.add(numOf.toString() + ' ' + c.getName() + 's');
            } else {
                output.add(c.getColorTaggedName());
            }
        }
        return output.toString();
    }

    @Override
    public String toString() {
        String output = "\r\n";
        output += getDescription();
        output += "\r\n";
        output += "The possible directions are:\r\n";
        output += getDirections();
        output += "\r\n\r\n";
        output += "Objects you can see:\r\n";
        output += getListOfAllVisibleObjects();
        output += "\r\n\r\n";
        output += "Items you can see:\r\n";
        output += getListOfAllVisibleItems();
        output += "\r\n\r\n";
        output += "Players in room:\r\n";
        output += getListOfPlayers();
        output += "\r\n\r\n";
        output += "Creatures you can see:\r\n";
        output += getListOfCreatures();
        output += "\r\n\r\n";
        if (this.battleManager.isBattleOngoing()) {
            output += "There is a battle going on!\r\n";
        }
        return output;
    }


    public String take(Player player, String name) {
        Optional<Item> maybeItem = this.items.stream().filter(i -> i.getName().equalsIgnoreCase(name)).findAny();
        if (maybeItem.isEmpty()) {
            Optional<RoomObject> maybeRo = this.objects.stream().filter(i -> i.getName().equalsIgnoreCase(name)).findAny();
            if (maybeRo.isEmpty()) {
                return "Could not find that item in this room.";
            }
            return "That's strange--it's stuck in it's place. You can't take it.";
        }
        Item item = maybeItem.get();
        if (item instanceof Takeable) {
            player.takeItem((Takeable) item);
            this.items.remove(item);
            return "Successfully taken";
        }
        return "That's strange--it's stuck in it's place. You can't take it.";
    }

    public String drop(Player player, String itemName) {
        Optional<Takeable> maybeTakeable = player.dropItem(itemName);
        if (maybeTakeable.isEmpty()) {
            return "You don't have a " + itemName + " to drop.";
        }
        Takeable takeable = maybeTakeable.get();
        this.items.add((Item) takeable);
        return "You glance at your empty hand as the " + takeable.getName() + " drops to the floor.";
    }

    public void attack(Player player, String weapon, String target) {
        System.out.println(player.toString() + " attempts attacking " + target + " with " + weapon);
        //if the target does not exist, don't add the player to the combat
        Creature targetCreature = this.getCreatureInRoom(target);
        if (targetCreature == null) {
            messenger.sendMessageToUser(new GameMessage("You cannot attack " + target + " because it does not exist.\r\n"), player.getId());
            return;
        }
        String playerName = player.getId().getUsername();
        if (targetCreature instanceof Player && targetCreature.getName().equals(playerName)) {
            messenger.sendMessageToUser(new GameMessage("You can't attack yourself!\r\n"), player.getId());
            return;
        }
        if (!player.isInBattle()) {
            this.battleManager.addCreatureToBattle(player);
        }
        if (!targetCreature.isInBattle()) {
            this.battleManager.addCreatureToBattle(targetCreature);
        }

        if (!this.battleManager.isBattleOngoing()) {
            this.battleManager.startBattle(player);
        }
        AttackAction attackAction = new AttackAction(targetCreature, weapon);
        this.battleManager.playerAction(player, attackAction);

        return;
    }

    public void setMessenger(Messenger messenger) {
        this.messenger = messenger;
        battleManager.setMessenger(messenger);
    }
}
