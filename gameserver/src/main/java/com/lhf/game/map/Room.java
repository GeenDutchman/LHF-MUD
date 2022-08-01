package com.lhf.game.map;

import java.util.*;
import java.util.regex.PatternSyntaxException;

import com.lhf.game.Container;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
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
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.TakeMessage;
import com.lhf.messages.out.*;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;
import com.lhf.messages.out.SpellFizzleMessage.SpellFizzleType;
import com.lhf.messages.out.TakeOutMessage.TakeOutType;
import com.lhf.server.client.user.UserID;

public class Room implements Container, MessageHandler, Comparable<Room> {
    private UUID uuid = UUID.randomUUID();
    private List<Item> items;
    private String description;
    private String name;
    private BattleManager battleManager;
    private Set<Creature> allCreatures;
    private Dungeon dungeon;

    private Map<CommandMessage, String> commands;
    private MessageHandler successor;

    Room(String name) {
        this.name = name;
        this.description = name;
        this.init();
    }

    Room(String name, String description) {
        this.name = name;
        this.description = description;
        this.init();
    }

    private Room init() {
        this.items = new ArrayList<>();
        this.battleManager = new BattleManager(this);
        this.allCreatures = new HashSet<>();
        this.commands = this.buildCommands();
        return this;
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
        sj.add("\"drop [itemname]\"").add("Drop an item that you have.").add("Like \"drop longsword\"");
        cmds.put(CommandMessage.DROP, sj.toString());
        return cmds;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public UUID getUuid() {
        return uuid;
    }

    boolean addPlayer(Player p) {
        return this.addCreature(p);
    }

    // TODO: AUDIT public access
    public boolean addCreature(Creature c) {
        c.setSuccessor(this);
        boolean added = this.allCreatures.add(c);
        if (added) {
            c.sendMsg(this.dungeon.seeRoomExits(this));
            this.sendMessageToAllExcept(new RoomEnteredOutMessage(c), c.getName());
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
        return this.removeCreature(toRemove) != null;
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
        ArrayList<Creature> closeMatch = new ArrayList<>();

        for (Creature c : this.allCreatures) {
            if (c.CheckNameRegex(creatureName, 3)) {
                match.add(c);
            }
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

    @Override
    public String toString() {
        SeeOutMessage seeOutMessage = this.produceMessage();
        for (Item item : this.items) {
            if (!item.checkVisibility()) {
                if (item instanceof Takeable) {
                    seeOutMessage.addSeen("Invisible Takeables", item);
                } else {
                    seeOutMessage.addSeen("Other invisible items", item);
                }
            }
        }
        return seeOutMessage.toString();
    }

    @Override
    public String printDescription() {
        return "<description>" + this.description + "</description>";
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        for (Creature c : this.allCreatures) {
            if (c instanceof Player) {
                seeOutMessage.addSeen(SeeCategory.PLAYER, c);
            } else if (c instanceof Monster) {
                seeOutMessage.addSeen(SeeCategory.MONSTER, c);
            } else if (c instanceof NonPlayerCharacter) {
                seeOutMessage.addSeen(SeeCategory.NPC, c);
            } else {
                seeOutMessage.addSeen(SeeCategory.CREATURE, c);
            }
        }
        for (Item item : this.items) {
            if (!item.checkVisibility()) {
                continue;
            }
            if (item instanceof Takeable) {
                seeOutMessage.addSeen(SeeCategory.TAKEABLE, item);
            } else {
                seeOutMessage.addSeen(SeeCategory.ROOM_ITEM, item);
            }
        }
        if (this.battleManager.isBattleOngoing()) {
            seeOutMessage.addExtraInfo("There is a battle going on!");
        }
        return seeOutMessage;
    }

    public void cast(Creature caster, ISpell spell) {
        if (spell instanceof RoomAffector) {
            this.sendMessageToAll(spell.Cast());
            return;
        }
        if (spell instanceof CreatureAffector) {
            CreatureAffector creatureSpell = (CreatureAffector) spell;
            if (creatureSpell instanceof DamageSpell) {
                if (!caster.isInBattle()) {
                    this.battleManager.addCreatureToBattle(caster);
                }
                for (Creature target : creatureSpell.getTargets()) {
                    if (!target.isInBattle()) {
                        this.battleManager.addCreatureToBattle(target);
                    }
                }
                if (!this.battleManager.isBattleOngoing()) {
                    this.battleManager.startBattle(caster);
                }
                this.battleManager.takeAction(caster, creatureSpell);
                return;
            }
            this.sendMessageToAll(spell.Cast());
            for (Creature target : creatureSpell.getTargets()) {
                this.sendMessageToAll(target.applySpell(creatureSpell));
            }
            return;
        }
        caster.sendMsg(new SpellFizzleMessage(SpellFizzleType.OTHER, caster, true));
        return;
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

            for (String thing : tMessage.getDirects()) {
                if (thing.length() < 3) {
                    ctx.sendMsg(new TakeOutMessage(thing, TakeOutType.SHORT));
                    continue;
                }
                if (thing.matches("[^ a-zA-Z_-]+") || thing.contains("*")) {
                    ctx.sendMsg(new TakeOutMessage(thing, TakeOutType.INVALID));
                    continue;
                }
                try {
                    Optional<Item> maybeItem = this.items.stream().filter(item -> item.CheckNameRegex(thing, 3))
                            .findAny();
                    if (maybeItem.isEmpty()) {
                        if (thing.equalsIgnoreCase("all") || thing.equalsIgnoreCase("everything")) {
                            ctx.sendMsg(new TakeOutMessage(thing, TakeOutType.GREEDY));
                        } else {
                            ctx.sendMsg(new TakeOutMessage(thing, TakeOutType.NOT_FOUND));
                        }
                        continue;
                    }
                    Item item = maybeItem.get();
                    if (item instanceof Takeable) {
                        ctx.getCreature().takeItem((Takeable) item);
                        this.items.remove(item);
                        ctx.sendMsg(new TakeOutMessage(thing, item, TakeOutType.FOUND_TAKEN));
                        continue;
                    }
                    ctx.sendMsg(new TakeOutMessage(thing, item, TakeOutType.NOT_TAKEABLE));
                } catch (PatternSyntaxException pse) {
                    pse.printStackTrace();
                    ctx.sendMsg(new TakeOutMessage(thing, TakeOutType.UNCLEVER));
                }
            }
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
                    ctx.sendMsg(ex.doUseAction(ctx.getCreature()));
                } else {
                    ctx.sendMsg(new InteractOutMessage(ro, InteractOutMessageType.CANNOT));
                }
                return true;
            }
            List<InteractObject> interactables = new ArrayList<>();
            for (Item ro : matches) {
                if (ro.checkVisibility() && ro instanceof InteractObject) {
                    interactables.add((InteractObject) ro);
                }
            }
            ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, name, interactables));
            return true;
        }
        return false;
    }

    private Boolean handleDrop(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.DROP) {
            DropMessage dMessage = (DropMessage) msg;
            if (dMessage.getDirects().size() == 0) {
                ctx.sendMsg(new BadTargetSelectedMessage(BadTargetSelectedMessage.BadTargetOption.NOTARGET, null));
            }
            for (String itemName : dMessage.getDirects()) {
                Optional<Item> maybeTakeable = ctx.getCreature().dropItem(itemName);
                if (maybeTakeable.isEmpty()) {
                    ctx.sendMsg(new NotPossessedMessage(Item.class.getSimpleName(), itemName));
                    continue;
                }
                Item takeable = maybeTakeable.get();
                this.addItem(takeable);
                ctx.sendMsg(new DropOutMessage(takeable));
            }
            return true;
        }
        return false;
    }

    // only used to examine items and creatures in this room
    private Boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage sMessage = (SeeMessage) msg;
            if (sMessage.getThing() != null) {
                ctx.sendMsg(this.examine(ctx.getCreature(), sMessage.getThing()));
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    private Boolean handleSay(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SAY) {
            SayMessage sMessage = (SayMessage) msg;
            if (sMessage.getTarget() != null) {
                boolean sent = false;
                for (Creature p : this.allCreatures) {
                    if (p.checkName(sMessage.getTarget())) {
                        p.sendMsg(new SpeakingMessage(ctx.getCreature(), sMessage.getMessage(), p));
                        sent = true;
                        break;
                    }
                }
                if (!sent) {
                    ctx.sendMsg(new CannotSpeakToMessage(sMessage.getTarget(), null));
                }
            } else {
                this.sendMessageToAll(new SpeakingMessage(ctx.getCreature(), sMessage.getMessage()));
            }
            return true;
        }
        return false;
    }

    private SeeOutMessage examine(Creature creature, String name) {
        ArrayList<Creature> found = this.getCreaturesInRoom(name);
        // we should be able to see people in a fight
        if (found.size() == 1) {
            return new SeeOutMessage(found.get(0), "They are in the room with you.");
        }

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

        return new SeeOutMessage("You couldn't find " + name + " to examine.");
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Room)) {
            return false;
        }
        Room other = (Room) obj;
        return Objects.equals(name, other.name) && Objects.equals(uuid, other.uuid);
    }

    @Override
    public int compareTo(Room o) {
        if (this.equals(o)) {
            return 0;
        }
        return this.uuid.compareTo(o.getUuid());
    }

}
