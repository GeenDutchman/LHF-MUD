package com.lhf.game.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.Container;
import com.lhf.game.Examinable;
import com.lhf.game.battle.AttackAction;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.InteractObject;
import com.lhf.game.item.interfaces.Takeable;
import com.lhf.server.client.user.UserID;
import com.lhf.server.messages.Messenger;
import com.lhf.server.messages.out.GameMessage;

public class Room implements Container {

    private Set<Player> players;
    private Map<String, Room> exits;
    private List<Item> items;
    private String description;
    private BattleManager battleManager;
    private Map<Creature, Integer> creatures; // how many of what type of monster
    private Messenger messenger;
    private Dungeon dungeon;

    Room(String description) {
        this.description = description;
        players = new HashSet<>();
        exits = new HashMap<>();
        items = new ArrayList<>();
        battleManager = new BattleManager(this);
        creatures = new HashMap<>();
    }

    boolean addPlayer(Player p) {
        return players.add(p);
    }

    int addCreature(Creature c) {
        if (this.creatures.containsKey(c)) {
            int previous = this.creatures.get(c);
            this.creatures.put(c, previous + 1);
            return previous + 1;
        } else {
            this.creatures.put(c, 1);
            return 1;
        }
    }

    public boolean removePlayer(UserID id) {
        Player toRemove = getPlayerInRoom(id);
        return this.removePlayer(toRemove);
    }

    public boolean removePlayer(Player p) {
        if (this.battleManager.isPlayerInBattle(p)) {
            this.battleManager.removeCreatureFromBattle(p);
            p.setInBattle(false);
        }
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

    public void killPlayer(Player p) {
        players.remove(p);
        dungeon.reincarnate(p);
    }

    boolean exitRoom(Player p, String direction) {
        if (p == null) {
            return false;
        }
        if (!exits.containsKey(direction)) {
            return false;
        }

        if (p.isInBattle()) {
            RollResult dexCheck = p.check(Attributes.DEX);
            if (dexCheck.getTotal() > 10) { // arbitrary boundary
                messenger.sendMessageToUser(
                        new GameMessage(
                                "You are " + dexCheck.getColorTaggedName() + " fleeing the battle. Flee!  Flee!\r\n"),
                        p.getId());
                messenger.sendMessageToAllInRoomExceptPlayer(new GameMessage(p.getName() + " has "
                        + dexCheck.getColorTaggedName()
                        + " fled the battle!\r\n"),
                        p.getId());
                battleManager.removeCreatureFromBattle(p);
            } else {
                messenger.sendMessageToUser(
                        new GameMessage(
                                "You didn't dodge past the enemy successfully" + dexCheck.getColorTaggedName() + "!"),
                        p.getId());
                return false;
            }
        }

        Room room = exits.get(direction);
        removePlayer(p);
        room.addPlayer(p);
        return true;
    }

    boolean addExit(String direction, Room room) {
        if (exits.containsKey(direction)) {
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

    public boolean hasItem(String itemName) {
        for (Item item : items) {
            if (item.checkName(itemName)) {
                return true;
            }
        }
        return false;
    }

    public Optional<Item> getItem(String itemName) {
        for (Item item : items) {
            if (item.checkName(itemName)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public Optional<Item> removeItem(String name) {
        for (Item item : items) {
            if (item.checkName(name)) {
                items.remove(item);
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public String getDescription() {
        return "<description>" + description + "</description>";
    }

    private String printListOfAllVisibleImovables() {
        StringJoiner output = new StringJoiner(", ");
        for (Item item : items) {
            if (item.checkVisibility() && !(item instanceof Takeable)) {
                output.add(item.getColorTaggedName());
            }
        }
        return output.toString();
    }

    private String printListOfAllVisibleTakeables() {
        StringJoiner output = new StringJoiner(", ");
        for (Item item : items) {
            if (item.checkVisibility() && item instanceof Takeable) {
                output.add(item.getColorTaggedName());
            }
        }
        return output.toString();
    }

    public String printListOfAllItems() {
        StringJoiner output = new StringJoiner(", ");
        for (Item o : items) {
            output.add(o.getColorTaggedName());
        }
        return output.toString();
    }

    String examine(Player p, String name) {
        if (p.isInBattle()) {
            return "You are in a fight right now, you are too busy to examine that!";
        }

        for (Item ro : items) {
            if (ro.CheckNameRegex(name, 3)) {
                return "<description>" + ro.getDescription() + "</description>";
            }
        }

        for (Item thing : p.getEquipmentSlots().values()) {
            if (thing.CheckNameRegex(name, 3)) {
                return "You have it equipped.  <description>" + thing.getDescription() + "</description>";
            }
        }

        Optional<Item> maybeThing = p.getInventory().getItem(name);
        if (maybeThing.isPresent()) {
            Item thing = maybeThing.get();
            if (thing instanceof Examinable) {
                return "You see it in your inventory.  <description>" + thing.getDescription()
                        + "</description>";
            }
            return "It seems to resist examination...weird.";
        }

        return "You couldn't find " + name + " to examine.";
    }

    String interact(Player p, String name) {
        if (p.isInBattle()) {
            return "You are in a fight right now, you are too busy to interact with that!";
        }

        ArrayList<Item> matches = new ArrayList<>();
        for (Item ro : items) {
            if (ro.CheckNameRegex(name, 3)) {
                matches.add(ro);
            }
        }
        if (matches.size() == 1) {
            Item ro = matches.get(0);
            if (ro instanceof InteractObject) {
                InteractObject ex = (InteractObject) ro;
                return "<interaction>" + ex.doUseAction(p) + "</interaction>";
            } else {
                return "You try to interact with " + ro.getColorTaggedName()
                        + ", but nothing happens.";
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("You couldn't find '").append(name).append("' to interact with.\n");
        StringJoiner sj = new StringJoiner(", ");
        for (Item ro : matches) {
            if (ro.checkVisibility() && ro instanceof InteractObject) {
                sj.add(ro.getColorTaggedName());
            }
        }
        if (sj.toString().length() > 0) {
            sb.append("Did you mean one of these: ").append(sj.toString()).append("?\n");
        }
        return sb.toString();
    }

    public String use(Player p, String usefulObject, String onWhat) {
        Object indirectObject = null; // indirectObject is the receiver of the action
        if (onWhat != null && onWhat.length() > 0) {
            List<Item> roomObjectThings = new ArrayList<>();
            for (Item ro : items) {
                if (ro.CheckNameRegex(onWhat, 3) && ro instanceof InteractObject) {
                    roomObjectThings.add(ro);
                }
            }

            if (roomObjectThings.size() == 1) {
                indirectObject = roomObjectThings.get(0);
            }
            List<Creature> targets = new ArrayList<>();
            if (indirectObject == null) {
                targets = getCreaturesInRoom(onWhat);
                if (targets.size() == 1) {
                    indirectObject = targets.get(0);
                }
            }
            if (indirectObject == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("You couldn't find '").append(onWhat).append("' to target.\n");
                StringJoiner sj = new StringJoiner(", ");
                if (roomObjectThings.size() > 0 || targets.size() > 0) {
                    for (Item ro : roomObjectThings) {
                        if (ro.checkVisibility()) {
                            sj.add(ro.getColorTaggedName());
                        }
                    }
                    for (Creature target : targets) {
                        sj.add(target.getColorTaggedName());
                    }
                }
                if (sj.toString().length() > 0) {
                    sb.append("Did you mean one of: ").append(sj.toString()).append("\n");
                }
                return sb.toString();
            }
        }
        return p.useItem(usefulObject, indirectObject);
    }

    Player getPlayerInRoom(UserID id) {
        for (Player p : players) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    private ArrayList<Creature> getCreaturesInRoom(String creatureName) {
        ArrayList<Creature> match = new ArrayList<>();
        for (Creature c : this.creatures.keySet()) {
            if (c.CheckNameRegex(creatureName, 3)) {
                match.add(c);
            }
        }

        // for PvP
        for (Player p : players) {
            if (p.CheckNameRegex(creatureName, 3)) {
                match.add(p);
            }
        }

        ArrayList<Creature> closeMatch = new ArrayList<>();
        for (Creature c : match) {
            if (c.checkName(creatureName)) {
                closeMatch.add(c);
                return closeMatch;
            }
        }

        return match;
    }

    Set<Player> getAllPlayersInRoom() {
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
        output += printListOfAllVisibleImovables();
        output += "\r\n\r\n";
        output += "Items you can see:\r\n";
        output += printListOfAllVisibleTakeables();
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

    String take(Player player, String name) {
        if (player.isInBattle()) {
            return "You are in a fight right now, you are too busy to take that!";
        }

        StringBuilder sb = new StringBuilder();
        for (String splitThing : name.split(",")) {
            String thing = splitThing.trim();
            if (thing.length() < 3) {
                sb.append("You'll need to be more specific than '").append(thing).append("'!\n");
                continue;
            }
            if (thing.matches("[^ a-zA-Z_-]+") || thing.contains("*")) {
                sb.append("I don't think '").append(thing).append("' is a valid name\n");
                continue;
            }
            try {
                Optional<Item> maybeItem = this.items.stream().filter(i -> i.CheckNameRegex(thing, 3)).findAny();
                if (maybeItem.isEmpty()) {
                    if (thing.equalsIgnoreCase("all") || thing.equalsIgnoreCase("everything")) {
                        sb.append("Aren't you being a bit greedy there by trying to grab '").append(thing)
                                .append("'?\n");
                    } else {
                        sb.append("Could not find that item '").append(thing).append("' in this room.\n");
                    }
                    continue;
                }
                Item item = maybeItem.get();
                if (item instanceof Takeable) {
                    player.takeItem((Takeable) item);
                    this.items.remove(item);
                    sb.append(item.getColorTaggedName() + " successfully taken\n");
                    continue;
                }
                sb.append("That's strange--it's stuck in its place. You can't take the " + item.getColorTaggedName())
                        .append("\n");
            } catch (PatternSyntaxException pse) {
                pse.printStackTrace();
                sb.append("Are you trying to be too clever with '").append(thing).append("'?\n");
            }
        }
        return sb.toString();
    }

    String drop(Player player, String itemName) {
        Optional<Item> maybeTakeable = player.dropItem(itemName);
        if (maybeTakeable.isEmpty()) {
            return "You don't have a " + itemName + " to drop.";
        }
        Item takeable = maybeTakeable.get();
        this.items.add(takeable);
        return "You glance at your empty hand as the " + takeable.getColorTaggedName() + " drops to the floor.";
    }

    public void attack(Player player, String weapon, String target) {
        System.out.println(player.toString() + " attempts attacking " + target + " with " + weapon);
        // if the target does not exist, don't add the player to the combat
        Creature targetCreature = null;
        List<Creature> possTargets = this.getCreaturesInRoom(target);
        if (possTargets.size() == 1) {
            targetCreature = possTargets.get(0);
        }
        if (targetCreature == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("You cannot attack '").append(target).append("' ");
            if (possTargets.size() == 0) {
                sb.append("because it does not exist.");
            } else {
                sb.append("because it could be any of these:\n");
                for (Creature c : possTargets) {
                    sb.append(c.getColorTaggedName()).append(" ");
                }
            }
            sb.append("\r\n");
            messenger.sendMessageToUser(new GameMessage(sb.toString()), player.getId());
            return;
        }
        String playerName = player.getId().getUsername();
        if (targetCreature instanceof Player && targetCreature.getName().equals(playerName)) {
            messenger.sendMessageToUser(new GameMessage("You can't attack yourself!\r\n"), player.getId());
            return;
        }
        if (!player.isInBattle()) {
            this.battleManager.addCreatureToBattle(player);
            if (this.battleManager.isBattleOngoing()) {
                messenger.sendMessageToAllInRoomExceptPlayer(
                        new GameMessage(player.getColorTaggedName() + " has joined the ongoing battle!"),
                        player.getId());
            }
        }

        if (!targetCreature.isInBattle()) {
            this.battleManager.addCreatureToBattle(targetCreature);
        }

        if (!this.battleManager.isBattleOngoing()) {
            this.battleManager.startBattle(player);
        }
        AttackAction attackAction = new AttackAction(targetCreature, weapon);
        this.battleManager.playerAction(player, attackAction);

    }

    void setMessenger(Messenger messenger) {
        this.messenger = messenger;
        battleManager.setMessenger(messenger);
    }

    void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    public String getBattleInfo() {
        return battleManager.toString();
    }
}
