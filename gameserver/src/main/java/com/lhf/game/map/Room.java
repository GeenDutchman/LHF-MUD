package com.lhf.game.map;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import org.mockito.exceptions.misusing.UnfinishedStubbingException;

import com.lhf.Examinable;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.EntityEffect;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.Usable;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.magic.CubeHolder;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.DropMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.TakeMessage;
import com.lhf.messages.in.UseMessage;
import com.lhf.messages.out.*;
import com.lhf.messages.out.BadMessage.BadMessageType;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;
import com.lhf.messages.out.TakeOutMessage.TakeOutType;
import com.lhf.messages.out.UseOutMessage.UseOutMessageOption;
import com.lhf.server.client.user.UserID;

public class Room implements Area {
    private UUID uuid = UUID.randomUUID();
    private List<Item> items;
    private String description;
    private String name;
    private BattleManager battleManager;
    private Set<Creature> allCreatures;
    private Land dungeon;
    private transient TreeSet<RoomEffect> effects;

    private Map<CommandMessage, String> commands;
    private MessageHandler successor;

    public static class RoomBuilder implements Area.AreaBuilder {
        private Logger logger;
        private String name;
        private String description;
        private List<Item> items;
        private Set<Creature> creatures;
        private Land dungeon;
        private MessageHandler successor;
        private BattleManager.Builder battleManagerBuilder;

        private RoomBuilder() {
            this.logger = Logger.getLogger(this.getClass().getName());
            this.name = "A Room";
            this.description = "An area that Creatures and Items can be in";
            this.items = new ArrayList<>();
            this.creatures = new HashSet<>();
            this.battleManagerBuilder = BattleManager.Builder.getInstance();
        }

        public static RoomBuilder getInstance() {
            return new RoomBuilder();
        }

        public RoomBuilder setName(String name) {
            this.name = name != null ? name : "A Room";
            return this;
        }

        public RoomBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public RoomBuilder addItem(Item item) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            if (item != null) {
                this.items.add(item);
            }
            return this;
        }

        public RoomBuilder addCreature(Creature creature) {
            if (this.creatures == null) {
                this.creatures = new HashSet<>();
            }
            if (creature != null) {
                this.creatures.add(creature);
            }
            return this;
        }

        public RoomBuilder setDungeon(Land dungeon) {
            this.dungeon = dungeon;
            return this;
        }

        public RoomBuilder setSuccessor(MessageHandler successor) {
            this.successor = successor;
            return this;
        }

        @Override
        public Collection<Creature> getCreatures() {
            return this.creatures;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Land getLand() {
            return this.dungeon;
        }

        @Override
        public Collection<Item> getItems() {
            return this.items;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public MessageHandler getSuccessor() {
            return this.successor;
        }

        protected Map<CommandMessage, String> buildCommands() {
            StringJoiner sj = new StringJoiner(" ");
            Map<CommandMessage, String> cmds = new EnumMap<>(CommandMessage.class);
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
            sj = new StringJoiner(" ");
            sj.add("\"use [itemname]\"").add("Uses an item that you have on yourself, if applicable.")
                    .add("Like \"use potion\"").add("\r\n");
            sj.add("\"use [itemname] on [otherthing]\"")
                    .add("Uses an item that you have on something or someone else, if applicable.")
                    .add("Like \"use potion on Bob\"");
            cmds.put(CommandMessage.USE, sj.toString());
            sj = new StringJoiner(" ");
            sj.add("\"cast [invocation]\"").add("Casts the spell that has the matching invocation.").add("\n");
            sj.add("\"cast [invocation] at [target]\"").add("Some spells need you to name a target.").add("\n");
            sj.add("\"cast [invocation] use [level]\"").add(
                    "Sometimes you want to put more power into your spell, so put a higher level number for the level.")
                    .add("\n");
            cmds.put(CommandMessage.CAST, sj.toString());
            return cmds;
        }

        @Override
        public Room build() {
            this.logger.info(() -> String.format("Building room '%s'", this.name));
            return new Room(this);
        }
    }

    Room(RoomBuilder builder) {
        this.name = builder.getName();
        this.description = builder.getDescription() != null ? builder.getDescription() : builder.getName();
        this.items = new ArrayList<>(builder.getItems());
        this.allCreatures = new TreeSet<>(builder.getCreatures());
        for (Creature c : this.allCreatures) {
            c.setSuccessor(this);
        }
        this.dungeon = builder.getLand();
        this.successor = builder.getSuccessor();
        this.battleManager = builder.battleManagerBuilder.Build(this);
        this.commands = builder.buildCommands();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Land getLand() {
        return this.dungeon;
    }

    @Override
    public void setLand(Land land) {
        this.dungeon = land;
    }

    @Override
    public Set<Creature> getCreatures() {
        return Collections.unmodifiableSet(this.allCreatures);
    }

    @Override
    public boolean addCreature(Creature c) {
        c.setSuccessor(this);
        boolean added = this.allCreatures.add(c);
        if (added) {
            c.sendMsg(this.produceMessage());
            this.announce(RoomEnteredOutMessage.getBuilder().setNewbie(c).setBroacast().Build(), c.getName());
            if (this.allCreatures.size() > 1 && !this.commands.containsKey(CommandMessage.ATTACK)) {
                StringJoiner sj = new StringJoiner(" ");
                sj.add("\"attack [name]\"").add("Attacks a creature").add("\r\n");
                sj.add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.");
                sj.add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.");
                this.commands.putIfAbsent(CommandMessage.ATTACK, sj.toString());
            }
        }
        if (this.battleManager.isBattleOngoing() && !CreatureFaction.NPC.equals(c.getFaction())) {
            this.battleManager.addCreature(c);
        }
        return added;
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Optional<Creature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public boolean removeCreature(Creature c) {
        if (this.battleManager.hasCreature(c)) {
            this.battleManager.removeCreature(c);
            c.setInBattle(false);
        }

        if (this.allCreatures.contains(c)) {
            this.allCreatures.remove(c);
            c.tick(TickType.ROOM);
            if (this.allCreatures.size() < 2) {
                this.commands.remove(CommandMessage.ATTACK);
            }
            return true;
        }
        return false;
    }

    @Override
    public Creature removeCreature(Creature c, Directions dir) {
        boolean removed = removeCreature(c);
        if (removed) {
            this.announce(SomeoneLeftRoom.getBuilder().setLeaveTaker(c).setWhichWay(dir).Build(), null,
                    List.of(c.getName()));
        }
        return c;
    }

    @Override
    public boolean addPlayer(Player p) {
        return this.addCreature(p);
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        Optional<Player> found = this.getPlayer(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Optional<Player> toRemove = getPlayer(id);
        if (toRemove.isPresent()) {
            this.removeCreature(toRemove.get());
        }
        return toRemove;
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removeCreature(player);
    }

    @Override
    public boolean onCreatureDeath(Creature creature) {
        boolean removed = this.removeCreature(creature);
        if (removed) {
            Corpse corpse = creature.die();
            this.addItem(corpse);
            for (String i : creature.getInventory().getItemList()) {
                Item drop = creature.removeItem(i).get();
                this.addItem(drop);
            }
        }
        removed = this.dungeon.onCreatureDeath(creature) || removed;

        return removed;
    }

    @Override
    public Collection<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    @Override
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

    /**
     * Checks to see if we have the exact name of the item.
     */
    @Override
    public boolean hasItem(String itemName) {
        return this.items.stream().anyMatch(item -> item != null && item.checkName(itemName));
    }

    @Override
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

    @Override
    public boolean removeItem(Item item) {
        return this.items.remove(item);
    }

    @Override
    public String toString() {
        SeeOutMessage seeOutMessage = this.produceMessage(true, true);
        return seeOutMessage.toString();
    }

    @Override
    public String printDescription() {
        return "<description>" + this.description + "</description>";
    }

    @Override
    public SeeOutMessage produceMessage(boolean seeInvisible, boolean seeDirections) {
        SeeOutMessage.Builder seeOutMessage = (SeeOutMessage.Builder) Area.super.produceMessage(seeInvisible,
                seeDirections).copyBuilder();

        if (this.battleManager.isBattleOngoing()) {
            seeOutMessage.addExtraInfo("There is a battle going on!");
        }
        return seeOutMessage.Build();
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect instanceof RoomEffect;
    }

    @Override
    public RoomAffectedMessage processEffect(EntityEffect effect, boolean reverse) {
        if (!this.isCorrectEffectType(effect)) {
            return null;
        }
        RoomEffect roomEffect = (RoomEffect) effect;
        // TODO: make banishing work!
        if (roomEffect.getCreaturesToBanish().size() > 0 || roomEffect.getCreaturesToBanish().size() > 0) {
            throw new UnfinishedStubbingException("We don't have this yet");
        }

        for (Item item : roomEffect.getItemsToSummon()) {
            this.addItem(item);
        }
        for (Creature creature : roomEffect.getCreaturesToSummon()) {
            this.addCreature(creature);
        }
        return RoomAffectedMessage.getBuilder().setRoom(this).setEffect(roomEffect).Build();
    }

    @Override
    public NavigableSet<RoomEffect> getMutableEffects() {
        return this.effects;
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
        return Collections.unmodifiableMap(this.commands);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getRoom() == null) {
            ctx.setRoom(this);
        }
        return ctx;
    }

    @Override
    public EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        EnumMap<CommandMessage, String> gathered = Area.super.gatherHelp(ctx);
        if (ctx.getCreature() == null) {
            gathered.remove(CommandMessage.ATTACK);
            gathered.remove(CommandMessage.DROP);
            gathered.remove(CommandMessage.INTERACT);
            gathered.remove(CommandMessage.TAKE);
            gathered.remove(CommandMessage.CAST);
            gathered.remove(CommandMessage.USE);
        }
        if (ctx.getCreature() != null && !(ctx.getCreature().getVocation() instanceof CubeHolder)) {
            gathered.remove(CommandMessage.CAST);
        }
        return gathered;
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        Boolean handled = false;
        CommandMessage type = msg.getType();
        ctx = this.addSelfToContext(ctx);
        if (type != null && (this.commands.containsKey(type)
                || (this.battleManager != null && this.battleManager.getCommands().containsKey(type)))) {
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
            } else if (type == CommandMessage.CAST) {
                handled = this.handleCast(ctx, msg);
            } else if (type == CommandMessage.USE) {
                handled = this.handleUse(ctx, msg);
            }
        }
        if (handled) {
            return handled;
        }
        return Area.super.handleMessage(ctx, msg);
    }

    protected Boolean handleAttack(CommandContext ctx, Command msg) {
        if (msg.getType() != CommandMessage.ATTACK) {
            return false;
        }
        ctx = this.addSelfToContext(ctx);
        if (ctx.getCreature() == null) {
            ctx.sendMsg(BadMessage.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(this.gatherHelp(ctx)).setCommand(msg).Build());
            return true;
        }
        return this.battleManager.handleMessage(ctx, msg);
    }

    protected boolean handleCast(CommandContext ctx, Command msg) {
        ctx.setBattleManager(this.battleManager);
        if (ctx.getCreature() == null) {
            ctx.sendMsg(BadMessage.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                    .setHelps(this.gatherHelp(ctx)).setCommand(msg).Build());
            return true;
        }
        return false; // let a successor (ThirdPower) handle it
    }

    protected Boolean handleTake(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.TAKE) {
            if (ctx.getCreature() == null) {
                ctx.sendMsg(BadMessage.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(this.gatherHelp(ctx)).setCommand(msg).Build());
                return true;
            }
            TakeMessage tMessage = (TakeMessage) msg;

            TakeOutMessage.Builder takeOutMessage = TakeOutMessage.getBuilder();

            for (String thing : tMessage.getDirects()) {
                takeOutMessage.setAttemptedName(thing);
                if (thing.length() < 3) {
                    ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.SHORT).Build());
                    continue;
                }
                if (thing.matches("[^ a-zA-Z_-]+") || thing.contains("*")) {
                    ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.INVALID).Build());
                    continue;
                }
                try {
                    Optional<Item> maybeItem = this.items.stream().filter(item -> item.CheckNameRegex(thing, 3))
                            .findAny();
                    if (maybeItem.isEmpty()) {
                        if (thing.equalsIgnoreCase("all") || thing.equalsIgnoreCase("everything")) {
                            ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.GREEDY).Build());
                        } else {
                            ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.NOT_FOUND).Build());
                        }
                        continue;
                    }
                    Item item = maybeItem.get();
                    takeOutMessage.setItem(item);
                    if (item instanceof Takeable) {
                        ctx.getCreature().addItem((Takeable) item);
                        this.items.remove(item);
                        ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.FOUND_TAKEN).Build());
                        continue;
                    }
                    ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.NOT_TAKEABLE).Build());
                } catch (PatternSyntaxException pse) {
                    pse.printStackTrace();
                    ctx.sendMsg(takeOutMessage.setSubType(TakeOutType.UNCLEVER).Build());
                }
            }
            return true;
        }
        return false;
    }

    protected Boolean handleInteract(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.INTERACT) {
            if (ctx.getCreature() == null) {
                ctx.sendMsg(BadMessage.getBuilder().setBadMessageType(BadMessageType.CREATURES_ONLY)
                        .setHelps(this.gatherHelp(ctx)).setCommand(msg).Build());
                return true;
            }
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
                    ctx.sendMsg(InteractOutMessage.getBuilder().setTaggable(ro)
                            .setSubType(InteractOutMessageType.CANNOT).Build());
                }
                return true;
            }
            List<InteractObject> interactables = new ArrayList<>();
            for (Item ro : matches) {
                if (ro.checkVisibility() && ro instanceof InteractObject) {
                    interactables.add((InteractObject) ro);
                }
            }
            ctx.sendMsg(BadTargetSelectedMessage.getBuilder().setBde(BadTargetOption.UNCLEAR).setBadTarget(name)
                    .setPossibleTargets(interactables).Build());
            return true;
        }
        return false;
    }

    protected Boolean handleDrop(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.DROP) {
            if (ctx.getCreature() == null) {
                ctx.sendMsg(new BadMessage(BadMessageType.CREATURES_ONLY, this.gatherHelp(ctx), msg));
                return true;
            }
            DropMessage dMessage = (DropMessage) msg;
            if (dMessage.getDirects().size() == 0) {
                ctx.sendMsg(new BadTargetSelectedMessage(BadTargetSelectedMessage.BadTargetOption.NOTARGET, null));
            }
            for (String itemName : dMessage.getDirects()) {
                Optional<Item> maybeTakeable = ctx.getCreature().removeItem(itemName);
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
    protected Boolean handleSee(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SEE) {
            SeeMessage sMessage = (SeeMessage) msg;
            if (sMessage.getThing() != null && !sMessage.getThing().isBlank()) {
                String name = sMessage.getThing();
                Collection<Creature> found = this.getCreaturesLike(name);
                // we should be able to see people in a fight
                if (found.size() == 1) {
                    ArrayList<Creature> foundList = new ArrayList<Creature>(found);
                    ctx.sendMsg(foundList.get(0).produceMessage().addExtraInfo("They are in the room with you. "));
                    return true;
                }

                if (ctx.getCreature() != null && ctx.getCreature().isInBattle()) {
                    ctx.sendMsg(new SeeOutMessage("You are in a fight right now, you are too busy to examine that!"));
                    return true;
                }

                for (Item ro : items) {
                    if (ro.CheckNameRegex(name, 3)) {
                        ctx.sendMsg(new SeeOutMessage(ro));
                        return true;
                    }
                }

                if (ctx.getCreature() != null) {
                    Creature creature = ctx.getCreature();
                    for (Item thing : creature.getEquipmentSlots().values()) {
                        if (thing.CheckNameRegex(name, 3)) {
                            if (thing instanceof Examinable) {
                                ctx.sendMsg(thing.produceMessage().addExtraInfo("You have it equipped. "));
                                return true;
                            }
                            ctx.sendMsg(new SeeOutMessage(thing, "You have it equipped. "));
                            return true;
                        }
                    }

                    Optional<Item> maybeThing = creature.getInventory().getItem(name);
                    if (maybeThing.isPresent()) {
                        Item thing = maybeThing.get();
                        if (thing instanceof Examinable) {
                            ctx.sendMsg(thing.produceMessage().addExtraInfo("You see it in your inventory. "));
                            return true;
                        }
                        ctx.sendMsg(new SeeOutMessage(thing, "You see it in your inventory. "));
                        return true;
                    }
                }

                ctx.sendMsg(new SeeOutMessage("You couldn't find " + name + " to examine. "));
                return true;
            } else {
                ctx.sendMsg(this.produceMessage());
                return true;
            }
        }
        return false;
    }

    protected Boolean handleSay(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.SAY) {
            SayMessage sMessage = (SayMessage) msg;
            if (sMessage.getTarget() != null) {
                boolean sent = false;
                Optional<Creature> optTarget = this.getCreature(sMessage.getTarget());
                if (optTarget.isPresent()) {
                    ClientMessenger sayer = ctx;
                    if (ctx.getCreature() != null) {
                        sayer = ctx.getCreature();
                    } else if (ctx.getUser() != null) {
                        sayer = ctx.getUser();
                    }
                    Creature target = optTarget.get();
                    target.sendMsg(new SpeakingMessage(sayer, sMessage.getMessage(), target));
                    sent = true;
                }
                if (!sent) {
                    ctx.sendMsg(new CannotSpeakToMessage(sMessage.getTarget(), null));
                }
            } else {
                this.announce(new SpeakingMessage(ctx.getCreature(), sMessage.getMessage()));
            }
            return true;
        }
        return false;
    }

    protected Boolean handleUse(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.USE) {
            if (ctx.getCreature() == null) {
                ctx.sendMsg(new BadMessage(BadMessageType.CREATURES_ONLY, this.gatherHelp(ctx), msg));
                return true;
            }
            UseMessage useMessage = (UseMessage) msg;
            Optional<Item> maybeItem = ctx.getCreature().getItem(useMessage.getUsefulItem());
            if (maybeItem.isEmpty() || !(maybeItem.get() instanceof Usable)) {
                ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES, ctx.getCreature(), null, null));
                return true;
            }
            Usable usable = (Usable) maybeItem.get();
            if (useMessage.getTarget() == null || useMessage.getTarget().isBlank()) {
                usable.doUseAction(ctx, ctx.getCreature());
                return true;
            }
            Collection<Creature> maybeCreature = this.getCreaturesLike(useMessage.getTarget());
            if (maybeCreature.size() == 1) {
                List<Creature> creatureList = new ArrayList<>(maybeCreature);
                usable.doUseAction(ctx, creatureList.get(0));
                return true;
            } else if (maybeCreature.size() > 1) {
                ctx.sendMsg(
                        new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, useMessage.getTarget(), maybeCreature));
                return true;
            }
            Optional<Item> maybeRoomItem = this.getItem(useMessage.getTarget());
            if (maybeRoomItem.isPresent()) {
                usable.doUseAction(ctx, maybeRoomItem.get());
                return true;
            }
            Optional<Item> maybeInventory = ctx.getCreature().getItem(useMessage.getTarget());
            if (maybeInventory.isPresent()) {
                usable.doUseAction(ctx, maybeInventory.get());
                return true;
            }
            ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, useMessage.getTarget(), null));
            return true;
        }
        return false;
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

}
