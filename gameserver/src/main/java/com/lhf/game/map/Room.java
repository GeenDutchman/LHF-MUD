package com.lhf.game.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.PatternSyntaxException;

import com.lhf.Examinable;
import com.lhf.game.Container;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.InteractObject;
import com.lhf.game.item.interfaces.Takeable;
import com.lhf.game.magic.ISpell;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.DamageSpell;
import com.lhf.game.magic.interfaces.RoomAffector;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.DropMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.TakeMessage;
import com.lhf.messages.out.GameMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SayOutMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.server.client.user.UserID;

public class Room implements Container, MessageHandler {

    private Map<String, Room> exits;
    private List<Item> items;
    private String description;
    private BattleManager battleManager;
    private Set<Creature> allCreatures;
    private Dungeon dungeon;

    private Map<CommandMessage, String> commands;
    private MessageHandler successor;

    Room(String description) {
        this.description = description;
        exits = new HashMap<>();
        items = new ArrayList<>();
        battleManager = new BattleManager(this);
        allCreatures = new HashSet<>();
        this.commands = this.buildCommands();
    }

    private Map<CommandMessage, String> buildCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, String> cmds = new HashMap<>();
        sj.add("\"say [message]\"").add("Tells everyone in your current room your message").add("\r\n");
        sj.add("\"say [message] to [name]\"")
                .add("Will tell a specific person somewhere in your current room your message.");
        sj.add("If your message contains the word 'to', put your message in quotes like")
                .add("\"say 'They are taking the hobbits to Isengard' to Aragorn\"")
                .add("\r\n");
        cmds.put(CommandMessage.SAY, sj.toString());
        sj = new StringJoiner(" "); // clear
        sj.add("\"see\"").add("Will give you some information about your surroundings.\r\n");
        sj.add("\"see [name]\"").add("May tell you more about the object with that name.");
        cmds.put(CommandMessage.SEE, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"go [direction]\"").add("Move in the desired direction, if that direction exists.  Like \"go east\"");
        cmds.put(CommandMessage.GO, sj.toString());
        sj = new StringJoiner(" ");
        sj.add("\"drop [itemname]\"").add("Drop an item that you have.").add("Like \"drop longsword\"");
        cmds.put(CommandMessage.DROP, sj.toString());
        return cmds;
    }

    boolean addPlayer(Player p) {
        return this.addCreature(p);
    }

    boolean addCreature(Creature c) {
        c.setSuccessor(this);
        boolean added = this.allCreatures.add(c);
        if (added) {
            c.sendMsg(new SeeOutMessage(this));
            this.sendMessageToAllExcept(new GameMessage(c.getColorTaggedName() + " has entered the room."),
                    c.getName());
            if (this.allCreatures.size() > 1 && !this.commands.containsKey(CommandMessage.ATTACK)) {
                StringJoiner sj = new StringJoiner(" ");
                sj.add("\"attack [name]\"").add("Attacks a creature").add("\r\n");
                sj.add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.");
                sj.add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.");
                this.commands.putIfAbsent(CommandMessage.ATTACK, sj.toString());
            }
        }
        if (this.battleManager.isBattleOngoing() && !CreatureFaction.NPC.equals(c.getFaction())) {
            this.battleManager.addCreatureToBattle(c);
        }
        return added;
    }

    public boolean containsCreature(Creature c) {
        return this.allCreatures.contains(c);
    }

    public boolean removePlayer(UserID id) {
        Player toRemove = getPlayerInRoom(id);
        return this.removePlayer(toRemove);
    }

    public boolean removePlayer(Player p) {
        Creature c = this.removeCreature(p);
        return c != null;
    }

    public Creature removeCreature(Creature c) {
        if (this.battleManager.isCreatureInBattle(c)) {
            this.battleManager.removeCreatureFromBattle(c);
            c.setInBattle(false);
        }
        if (this.allCreatures.contains(c)) {
            this.allCreatures.remove(c);
            if (this.allCreatures.size() < 2) {
                this.commands.remove(CommandMessage.ATTACK);
            }
            return c;
        }
        return null;
    }

    public void killPlayer(Player p) {
        this.allCreatures.remove(p);
        dungeon.reincarnate(p);
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
        StringJoiner sj;
        if (obj instanceof InteractObject) {
            sj = new StringJoiner(" ");
            sj.add("\"interact [item]\"").add("Certain items in the room may be interactable.")
                    .add("Like \"interact lever\"");
            this.commands.put(CommandMessage.INTERACT, sj.toString());
        }
        if (obj instanceof Takeable) {
            sj = new StringJoiner(" ");
            sj.add("\"take [item]\"").add("Take an item from the room and add it to your inventory.");
            this.commands.put(CommandMessage.TAKE, sj.toString());
        }
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
        Item firstfound = null;
        int takeables = 0;
        for (Item item : items) {
            if (firstfound == null && item.checkName(name)) {
                items.remove(item);
                firstfound = item;
                continue;
            }
            if (item instanceof Takeable) {
                takeables++;
            }
        }
        if (takeables == 0 && this.commands.containsKey(CommandMessage.TAKE)) {
            this.commands.remove(CommandMessage.TAKE);
        }
        if (firstfound != null) {
            return Optional.of(firstfound);
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

    public Player getPlayerInRoom(UserID id) {
        for (Creature creature : this.allCreatures) {
            if (creature instanceof Player) {
                Player p = (Player) creature;
                if (p.getId().equals(id)) {
                    return p;
                }
            }
        }
        return null;
    }

    public ArrayList<Creature> getCreaturesInRoom() {
        ArrayList<Creature> creatures = new ArrayList<>();
        for (Creature c : this.allCreatures) {
            creatures.add(c);
        }
        return creatures;
    }

    public ArrayList<Creature> getCreaturesInRoom(String creatureName) {
        ArrayList<Creature> match = new ArrayList<>();
        for (Creature c : this.allCreatures) {
            if (c.CheckNameRegex(creatureName, 3)) {
                match.add(c);
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
        Set<Player> players = new HashSet<>();
        for (Creature c : this.allCreatures) {
            if (c instanceof Player) {
                players.add((Player) c);
            }
        }
        return players;
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
        for (Player p : this.getAllPlayersInRoom()) {
            output.add(p.getColorTaggedName());
        }
        return output.toString();
    }

    private String getListOfCreaturesNotPlayers() {
        StringJoiner output = new StringJoiner(", ");
        output.setEmptyValue("None.");
        for (Creature c : this.allCreatures) {
            if (!(c instanceof Player)) {
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
        output += getListOfCreaturesNotPlayers();
        output += "\r\n\r\n";
        if (this.battleManager.isBattleOngoing()) {
            output += "There is a battle going on!\r\n";
        }
        return output;
    }

    @Override
    public String printDescription() {
        return this.toString();
    }

    public String cast(Player player, ISpell spell) {
        if (spell instanceof RoomAffector) {
            this.sendMessageToAll(new GameMessage(spell.Cast()));
            return spell.Cast();
        }
        if (spell instanceof CreatureAffector) {
            CreatureAffector creatureSpell = (CreatureAffector) spell;
            if (creatureSpell instanceof DamageSpell) {
                if (!player.isInBattle()) {
                    this.battleManager.addCreatureToBattle(player);
                    if (this.battleManager.isBattleOngoing()) {
                        this.sendMessageToAllExcept(
                                new GameMessage(player.getColorTaggedName() + " has joined the ongoing battle!"),
                                player.getName());
                    }
                }
                for (Creature target : creatureSpell.getTargets()) {
                    if (!target.isInBattle()) {
                        this.battleManager.addCreatureToBattle(target);
                    }
                }
                if (!this.battleManager.isBattleOngoing()) {
                    this.battleManager.startBattle(player);
                }
                this.battleManager.takeAction(player, creatureSpell);
                return "You attempted an attack spell";
            }
            this.sendMessageToAll(new GameMessage(spell.Cast()));
            StringBuilder sb = new StringBuilder();
            for (Creature target : creatureSpell.getTargets()) {
                sb.append(target.applySpell(creatureSpell)).append("\n");
            }
            this.sendMessageToAll(new GameMessage(sb.toString()));
            return sb.toString();
        }
        return "You attempted a spell!";
    }

    public void sendMessageToAll(OutMessage message) {
        for (Creature c : this.allCreatures) {
            c.sendMsg(message);
        }
    }

    public void sendMessageToAllExcept(OutMessage message, String... exactNames) {
        Set<String> preciseNames = new HashSet<>(Arrays.asList(exactNames));
        for (Creature c : this.allCreatures) {
            if (preciseNames.contains(c.getName())) {
                continue;
            }
            c.sendMsg(message);
        }
    }

    void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    public String getBattleInfo() {
        return battleManager.toString();
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return this.commands;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        Boolean handled = false;
        CommandMessage type = msg.getType();
        if (type != null && this.commands.containsKey(type)) {
            if (type == CommandMessage.ATTACK) {
                handled = this.handleAttack(ctx, msg);
            } else if (type == CommandMessage.SAY) {
                handled = this.handleSay(ctx, msg);
            } else if (type == CommandMessage.SEE) {
                handled = this.handleSee(ctx, msg);
            } else if (type == CommandMessage.GO) {
                handled = this.handleGo(ctx, msg);
            } else if (type == CommandMessage.DROP) {
                handled = this.handleDrop(ctx, msg);
            } else if (type == CommandMessage.INTERACT) {
                handled = this.handleInteract(ctx, msg);
            } else if (type == CommandMessage.TAKE) {
                handled = this.handleTake(ctx, msg);
            }
        }
        if (handled) {
            return handled;
        }
        ctx.setRoom(this);
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    private Boolean handleAttack(CommandContext ctx, Command msg) {
        if (msg.getType() != CommandMessage.ATTACK) {
            return false;
        }
        return this.battleManager.handleMessage(ctx, msg);
    }

    private Boolean handleTake(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.TAKE) {
            TakeMessage tMessage = (TakeMessage) msg;
            StringBuilder sb = new StringBuilder();

            for (String thing : tMessage.getDirects()) {
                if (thing.length() < 3) {
                    sb.append("You'll need to be more specific than '").append(thing).append("'!\n");
                    continue;
                }
                if (thing.matches("[^ a-zA-Z_-]+") || thing.contains("*")) {
                    sb.append("I don't think '").append(thing).append("' is a valid name\n");
                    continue;
                }
                try {
                    Optional<Item> maybeItem = this.items.stream().filter(item -> item.CheckNameRegex(thing, 3))
                            .findAny();
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
                        ctx.getCreature().takeItem((Takeable) item);
                        this.items.remove(item);
                        sb.append(item.getColorTaggedName() + " successfully taken\n");
                        continue;
                    }
                    sb.append(
                            "That's strange--it's stuck in its place. You can't take the " + item.getColorTaggedName())
                            .append("\n");
                } catch (PatternSyntaxException pse) {
                    pse.printStackTrace();
                    sb.append("Are you trying to be too clever with '").append(thing).append("'?\n");
                }
            }
            ctx.sendMsg(new GameMessage(sb.toString()));
            return true;
        }
        return false;
    }

    private Boolean handleInteract(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.INTERACT) {
            InteractMessage intMessage = (InteractMessage) msg;
            String name = intMessage.getObject();
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
                    ctx.sendMsg(new GameMessage(
                            "<interaction>" + ex.doUseAction((Player) ctx.getCreature()) + "</interaction>"));
                } else {
                    ctx.sendMsg(new GameMessage("You try to interact with " + ro.getColorTaggedName()
                            + ", but nothing happens."));
                }
                return true;
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
            ctx.sendMsg(new GameMessage(sb.toString()));
            return true;
        }
        return false;
    }

    private Boolean handleDrop(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.DROP) {
            DropMessage dMessage = (DropMessage) msg;
            StringJoiner sj = new StringJoiner(" ");
            sj.setEmptyValue("You didn't select anything to drop!");
            for (String itemName : dMessage.getDirects()) {
                Optional<Item> maybeTakeable = ctx.getCreature().dropItem(itemName);
                if (maybeTakeable.isEmpty()) {
                    sj.add("You don't have a " + itemName + " to drop.");
                    continue;
                }
                Item takeable = maybeTakeable.get();
                this.addItem(takeable);
                sj.add("You glance at your empty hand as the " + takeable.getColorTaggedName()
                        + " drops to the floor.");
            }
            ctx.sendMsg(new GameMessage(sj.toString()));
            return true;
        }
        return false;
    }

    private Boolean handleGo(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.GO) {
            GoMessage goMessage = (GoMessage) msg;
            if (exits.containsKey(goMessage.getDirection().toString().toLowerCase())) {
                Room otherRoom = exits.get(goMessage.getDirection().toString().toLowerCase());
                ctx.getCreature().setSuccessor(otherRoom);
                this.removePlayer((Player) ctx.getCreature());
                otherRoom.addPlayer((Player) ctx.getCreature());
            } else {
                ctx.sendMsg(new GameMessage(goMessage.getDirection().toString()
                        + " is not a valid choice here, try one of " + this.getDirections()));
            }
            return true;
        }
        return false;
    }

    private Boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage sMessage = (SeeMessage) msg;
            if (sMessage.getThing() != null) {
                ctx.sendMsg(this.examine(ctx.getCreature(), sMessage.getThing()));
            } else {
                ctx.sendMsg(new SeeOutMessage(this));
            }
            return true;
        }
        return false;
    }

    private Boolean handleSay(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SAY) {
            SayMessage sMessage = (SayMessage) msg;
            if (sMessage.getTarget() != null) {
                for (Creature p : this.allCreatures) {
                    if (p.checkName(sMessage.getTarget())) {
                        p.sendMsg(new SayOutMessage(ctx.getCreature(), sMessage.getMessage(), p));
                        break;
                    }
                }
            } else {
                this.sendMessageToAll(new SayOutMessage(ctx.getCreature(), sMessage.getMessage()));
            }
            return true;
        }
        return false;
    }

    private SeeOutMessage examine(Creature creature, String name) {
        if (creature.isInBattle()) {
            return new SeeOutMessage("You are in a fight right now, you are too busy to examine that!");
        }

        for (Item ro : items) {
            if (ro.CheckNameRegex(name, 3)) {
                return new SeeOutMessage(ro);
            }
        }

        for (Item thing : creature.getEquipmentSlots().values()) {
            if (thing.CheckNameRegex(name, 3)) {
                return new SeeOutMessage(thing, "You have it equipped. ");
            }
        }

        Optional<Item> maybeThing = creature.getInventory().getItem(name);
        if (maybeThing.isPresent()) {
            Item thing = maybeThing.get();
            return new SeeOutMessage(thing, "You see it in your inventory.");
        }

        // TODO: make creatures examinable

        return new SeeOutMessage("You couldn't find " + name + " to examine.");
    }
}
