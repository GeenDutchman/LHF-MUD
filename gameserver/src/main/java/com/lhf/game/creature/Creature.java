package com.lhf.game.creature;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainer;
import com.lhf.game.EntityEffect;
import com.lhf.game.ItemContainer;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.DamageFlavorReactions;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.DamageDice.FlavoredRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.messages.events.CreatureStatusRequestedEvent;
import com.lhf.messages.events.ItemEquippedEvent;
import com.lhf.messages.events.ItemEquippedEvent.EquipResultType;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.events.ItemUnequippedEvent;
import com.lhf.messages.events.ItemUnequippedEvent.UnequipResultType;
import com.lhf.messages.in.EquipMessage;
import com.lhf.messages.in.InventoryMessage;
import com.lhf.messages.in.StatusMessage;
import com.lhf.messages.in.UnequipMessage;
import com.lhf.server.client.CommandInvoker;

public abstract class Creature implements ICreature {
    private final String name; // Username for players, description name (e.g., goblin 1) for monsters/NPCs
    private final GameEventProcessorID gameEventProcessorID;
    private CreatureFaction faction; // See shared enum
    private Vocation vocation;
    // private MonsterType monsterType; // I dont know if we'll need this

    private TreeSet<CreatureEffect> effects;
    private Statblock statblock;

    private boolean inBattle; // Boolean to determine if this creature is in combat
    private transient CommandInvoker controller;
    private transient CommandChainHandler successor;
    private Map<CommandMessage, CommandHandler> cmds;
    private transient final Logger logger;

    protected Creature(ICreature.CreatureBuilder<?> builder) {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.cmds = this.buildCommands();
        // Instantiate creature with no name and type Monster
        this.name = builder.getName();
        this.faction = builder.getFaction();
        this.vocation = builder.getVocation();

        this.effects = new TreeSet<>();
        this.statblock = builder.getStatblock();
        this.controller = builder.getController();
        this.successor = builder.getSuccessor();
        ItemContainer.transfer(builder.getCorpse(), this.getInventory(), null);

        // We don't start them in battle
        this.inBattle = false;
        this.logger = Logger
                .getLogger(String.format("%s.%s", this.getClass().getName(), this.name.replaceAll("\\W", "_")));
    }

    private Map<CommandMessage, CommandHandler> buildCommands() {
        StringJoiner sj = new StringJoiner(" ");
        Map<CommandMessage, CommandHandler> cmds = new EnumMap<>(CommandMessage.class);
        sj.add("\"equip [item]\"").add("Equips the item from your inventory to its default slot").add("\r\n");
        sj.add("\"equip [item] to [slot]\"")
                .add("Equips the item from your inventory to the specified slot, if such exists.");
        sj.add("In the unlikely event that either the item or the slot's name contains 'to', enclose the name in quotation marks.");
        cmds.put(CommandMessage.EQUIP, new EquipHandler());
        sj = new StringJoiner(" ");

        cmds.put(CommandMessage.UNEQUIP, new UnequipHandler());
        sj = new StringJoiner(" ");

        cmds.put(CommandMessage.INVENTORY, new InventoryHandler());
        cmds.put(CommandMessage.STATUS, new StatusHandler());
        return cmds;
    }

    private int getHealth() {
        return this.statblock.getStats().getOrDefault(Stats.CURRENTHP, 0);
    }

    @Override
    public HealthBuckets getHealthBucket() {
        return HealthBuckets.calculate(getHealth(), this.statblock.getStats().getOrDefault(Stats.MAXHP, 0));
    }

    @Override
    public boolean isAlive() {
        return getHealth() > 0;
    }

    @Override
    public void updateHitpoints(int value) {
        int current = this.statblock.getStats().getOrDefault(Stats.CURRENTHP, 0);
        int max = this.statblock.getStats().getOrDefault(Stats.MAXHP, 0);
        current = Integer.max(0, Integer.min(max, current + value)); // stick between 0 and max
        this.statblock.getStats().replace(Stats.CURRENTHP, current);
        if (current <= 0) {
            ICreature.announceDeath(this);
        }
    }

    @Override
    public void updateXp(int value) {
        int current = this.statblock.getStats().getOrDefault(Stats.XPEARNED, 0);
        current += value;
        this.statblock.getStats().replace(Stats.XPEARNED, current);
        if (this.vocation != null) {
            this.vocation.addExperience(value);
        }
    }

    private void updateAttribute(Attributes attribute, int value) {
        AttributeBlock retrieved = this.statblock.getAttributes();
        retrieved.setScoreBonus(attribute, retrieved.getScoreBonus(attribute) + value);
    }

    private void updateStat(Stats stat, int value) {
        Map<Stats, Integer> stats = this.statblock.getStats();
        stats.merge(stat, value, (a, b) -> {
            if (a != null && b != null) {
                return a + b;
            }
            return a != null ? a : b;
        });
        if (Stats.XPEARNED.equals(stat) && this.vocation != null) {
            this.vocation.addExperience(value);
        }
    }

    /* start getters */

    @Override
    public Map<Stats, Integer> getStats() {
        return Collections.unmodifiableMap(this.statblock.getStats());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CreatureFaction getFaction() {
        if (this.faction == null) {
            this.faction = CreatureFaction.RENEGADE;
        }
        return faction;
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        this.faction = faction != null ? faction : CreatureFaction.RENEGADE;
    }

    @Override
    public AttributeBlock getAttributes() {
        return this.statblock.getAttributes();
    }

    @Override
    public Set<EquipmentTypes> getProficiencies() {
        return this.statblock.getProficiencies();
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return this.statblock.getEquipmentSlots();
    }

    @Override
    public boolean isInBattle() {
        return this.inBattle;
    }

    protected MultiRollResult adjustDamageByFlavor(MultiRollResult mrr, boolean reverse) {
        if (mrr == null) {
            return null;
        }
        MultiRollResult.Builder mrrBuilder = new MultiRollResult.Builder();
        DamageFlavorReactions dfr = this.statblock.getDamageFlavorReactions();
        for (RollResult rr : mrr) {
            if (rr instanceof FlavoredRollResult) {
                FlavoredRollResult frr = (FlavoredRollResult) rr;
                if (dfr.getHealing().contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.negative());
                    } else {
                        mrrBuilder.addRollResults(frr);
                    }
                } else if (dfr.getImmunities().contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr.none());
                } else if (dfr.getResistances().contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.half());
                    } else {
                        mrrBuilder.addRollResults(frr.negative().half());
                    }
                } else if (dfr.getWeaknesses().contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.twice());
                    } else {
                        mrrBuilder.addRollResults(frr.negative().twice());
                    }
                } else {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr);
                    } else {
                        mrrBuilder.addRollResults(frr.negative());
                    }
                }
            } else {
                if (dfr.getImmunities().size() > 0) {
                    mrrBuilder.addRollResults(rr.none()); // if they have any immunities, unflavored damge does nothing
                } else if (reverse) {
                    mrrBuilder.addRollResults(rr.negative());
                } else {
                    mrrBuilder.addRollResults(rr);
                }
            }
        }

        if (dfr.getImmunities().size() == 0) { // if they have any immunities, unflavored damge does nothing
            mrrBuilder.addBonuses(mrr.getBonuses());
        }

        return mrrBuilder.Build();
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return effect != null && effect instanceof CreatureEffect;
    }

    @Override
    public boolean shouldAdd(EntityEffect effect, boolean reverse) {
        return this.isAlive() && ICreature.super.shouldAdd(effect, reverse);
    }

    @Override
    public CreatureAffectedEvent processEffect(EntityEffect effect, boolean reverse) {
        if (!this.isCorrectEffectType(effect)) {
            return null;
        }
        CreatureEffect creatureEffect = (CreatureEffect) effect;
        MultiRollResult mrr = this.adjustDamageByFlavor(creatureEffect.getDamageResult(), reverse);
        if (mrr != null) {
            creatureEffect.updateDamageResult(mrr);
            this.updateHitpoints(mrr.getRoll());
        }
        for (Stats delta : creatureEffect.getStatChanges().keySet()) {
            int amount = creatureEffect.getStatChanges().get(delta);
            if (reverse) {
                amount = amount * -1;
            }
            this.updateStat(delta, amount);
        }
        if (this.isAlive()) {
            for (Attributes delta : creatureEffect.getAttributeScoreChanges().keySet()) {
                int amount = creatureEffect.getAttributeScoreChanges().get(delta);
                if (reverse) {
                    amount = amount * -1;
                }
                this.updateAttribute(delta, amount);
            }
            for (Attributes delta : creatureEffect.getAttributeBonusChanges().keySet()) {
                int amount = creatureEffect.getAttributeBonusChanges().get(delta);
                if (reverse) {
                    amount = amount * -1;
                }
                this.updateModifier(delta, amount);
            }
            // for now...cannot curse someone with being a renegade
            if (creatureEffect.isRestoreFaction()) {
                this.restoreFaction();
            }
        } else {
            ICreature.announceDeath(this);
        }

        CreatureAffectedEvent camOut = CreatureAffectedEvent.getBuilder().setAffected(this)
                .setEffect(creatureEffect).setReversed(reverse).setBroacast().Build();
        return camOut;
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        return this.effects;
    }

    @Override
    public String getCreatureRace() {
        return this.statblock.getCreatureRace();
    }

    @Override
    public void setCreatureRace(String creatureRace) {
        this.statblock.setCreatureRace(creatureRace);
    }

    @Override
    public Vocation getVocation() {
        return this.vocation;
    }

    @Override
    public void setVocation(Vocation job) {
        this.vocation = job;
    }

    @Override
    public void setAttributes(AttributeBlock attributes) {
        this.statblock.setAttributes(attributes);
    }

    @Override
    public void setStats(EnumMap<Stats, Integer> stats) {
        this.statblock.setStats(stats);
    }

    @Override
    public void setProficiencies(EnumSet<EquipmentTypes> proficiencies) {
        this.statblock.setProficiencies(proficiencies);
    }

    @Override
    public void setInventory(Inventory inventory) {
        this.statblock.setInventory(inventory);
    }

    @Override
    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        this.statblock.setEquipmentSlots(equipmentSlots);
    }

    @Override
    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ICreature)) {
            return false;
        }
        ICreature c = (ICreature) obj;
        return c.getName().equals(getName());
    }

    @Override
    public Inventory getInventory() {
        return this.statblock.getInventory();
    }

    @Override
    public String printInventory() {
        return this.statblock.getInventory().getInventoryOutMessage(this.getEquipmentSlots()).toString();
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        Optional<Item> maybeItem = this.getInventory().getItem(itemName);
        ItemEquippedEvent.Builder equipMessage = ItemEquippedEvent.getBuilder().setAttemptedItemName(itemName)
                .setNotBroadcast().setAttemptedSlot(slot);
        if (maybeItem.isPresent()) {
            Item fromInventory = maybeItem.get();
            equipMessage.setItem(fromInventory);
            if (fromInventory instanceof Equipable) {
                Equipable equipThing = (Equipable) fromInventory;
                if (slot == null) {
                    slot = equipThing.getWhichSlots().get(0);
                    equipMessage.setAttemptedSlot(slot);
                }
                if (equipThing.getWhichSlots().contains(slot)) {
                    this.unequipItem(slot, "");
                    this.getInventory().removeItem(equipThing);
                    this.getEquipmentSlots().putIfAbsent(slot, equipThing);
                    ICreature.eventAccepter.accept(this, equipMessage.setSubType(EquipResultType.SUCCESS).Build());
                    equipThing.onEquippedBy(this);

                    return true;
                }
                ICreature.eventAccepter.accept(this, equipMessage.setSubType(EquipResultType.BADSLOT).Build());
                return true;
            }
            ICreature.eventAccepter.accept(this, equipMessage.setSubType(EquipResultType.NOTEQUIPBLE).Build());
            return true;
        }
        ICreature.eventAccepter.accept(this,
                ItemNotPossessedEvent.getBuilder().setNotBroadcast().setItemType(Item.class.getSimpleName())
                        .setItemName(itemName).Build());
        return true;
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        ItemUnequippedEvent.Builder unequipMessage = ItemUnequippedEvent.getBuilder().setNotBroadcast().setSlot(slot)
                .setAttemptedName(weapon);
        if (slot == null) {
            // if they specified weapon and not slot
            Optional<Item> optItem = getItem(weapon);
            if (optItem.isPresent()) {
                unequipMessage.setItem(optItem.get());
                Map<EquipmentSlots, Equipable> equipped = this.getEquipmentSlots();
                if (equipped.containsValue(optItem.get())) {
                    Equipable equippedThing = (Equipable) optItem.get();
                    for (EquipmentSlots thingSlot : equippedThing.getWhichSlots()) {
                        if (equippedThing.equals(equipped.get(thingSlot))) {
                            equipped.remove(thingSlot);
                            this.getInventory().addItem(equippedThing);
                            ICreature.eventAccepter.accept(this,
                                    unequipMessage.setSubType(UnequipResultType.SUCCESS).Build());
                            equippedThing.onUnequippedBy(this);
                            return true;
                        }
                    }
                }
                ICreature.eventAccepter.accept(this,
                        unequipMessage.setSubType(UnequipResultType.ITEM_NOT_EQUIPPED).Build());
                return false;
            }

            ICreature.eventAccepter.accept(this,
                    ItemNotPossessedEvent.getBuilder().setNotBroadcast().setItemType(Item.class.getSimpleName())
                            .setItemName(weapon).Build());
            return false;
        }
        Equipable thing = getEquipmentSlots().remove(slot);
        if (thing != null) {
            unequipMessage.setItem(thing).setSubType(UnequipResultType.SUCCESS);
            this.getInventory().addItem(thing);
            ICreature.eventAccepter.accept(this, unequipMessage.Build());
            thing.onUnequippedBy(this);
            return true;
        }
        ICreature.eventAccepter.accept(this,
                unequipMessage.setItem(null).setSubType(UnequipResultType.ITEM_NOT_FOUND).Build()); // thing is
        // null
        return false;
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return (Equipable) this.getEquipmentSlots().get(slot);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        Optional<Item> toRemove = ICreature.super.removeItem(name);
        if (toRemove.isEmpty()) {
            for (Item item : this.getEquipmentSlots().values()) {
                if (item.CheckNameRegex(name, 3)) {
                    toRemove = Optional.of(item);
                    break;
                }
            }
        }
        return toRemove;
    }

    @Override
    public CommandInvoker getController() {
        return this.controller;
    }

    public void setController(CommandInvoker cont) {
        this.controller = cont;
    }

    @Override
    public final GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.cmds);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    protected class EquipHandler implements CreatureCommandHandler {
        private static String helpString;
        private static final Predicate<CommandContext> enabledPredicate = CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> ctx.getCreature().getItems().stream()
                        .anyMatch(item -> item != null && item instanceof Equipable));

        static {
            StringJoiner sj = new StringJoiner(" ");
            sj.add("\"equip [item]\"").add("Equips the item from your inventory to its default slot").add("\r\n");
            sj.add("\"equip [item] to [slot]\"")
                    .add("Equips the item from your inventory to the specified slot, if such exists.");
            sj.add("In the unlikely event that either the item or the slot's name contains 'to', enclose the name in quotation marks.");
            EquipHandler.helpString = sj.toString();
        }

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.EQUIP;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(EquipHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return EquipHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd instanceof EquipMessage equipMessage) {
                Creature.this.equipItem(equipMessage.getItemName(), equipMessage.getEquipSlot());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Creature.this;
        }

    }

    protected class UnequipHandler implements CreatureCommandHandler {
        private static String helpString;
        private static final Predicate<CommandContext> enabledPredicate = CreatureCommandHandler.defaultCreaturePredicate
                .and(ctx -> ctx.getCreature().getEquipmentSlots().values().size() > 0);

        static {
            StringJoiner sj = new StringJoiner(" ");
            sj.add("\"unequip [item]\"").add("Unequips the item (if equipped) and places it in your inventory")
                    .add("\r\n");
            sj.add("\"unequip [slot]\"")
                    .add("Unequips the item that is in the specified slot (if equipped) and places it in your inventory");
            UnequipHandler.helpString = sj.toString();
        }

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.EQUIP;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(UnequipHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return UnequipHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd instanceof UnequipMessage unequipMessage) {
                Creature.this.unequipItem(EquipmentSlots.getEquipmentSlot(unequipMessage.getUnequipWhat()),
                        unequipMessage.getUnequipWhat());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Creature.this;
        }

    }

    protected class StatusHandler implements CreatureCommandHandler {
        private final static String helpString = "\"status\" Show you how much HP you currently have, among other things.";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.STATUS;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(StatusHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return StatusHandler.defaultCreaturePredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd instanceof StatusMessage statusMessage) {
                ctx.receive(
                        CreatureStatusRequestedEvent.getBuilder().setNotBroadcast().setFromCreature(Creature.this, true)
                                .Build());
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Creature.this;
        }

    }

    protected class InventoryHandler implements CreatureCommandHandler {
        private final static String helpString = "\"inventory\" List what you have in your inventory and what you have equipped";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.INVENTORY;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(InventoryHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return InventoryHandler.defaultCreaturePredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd instanceof InventoryMessage inventoryMessage) {
                ctx.receive(Creature.this.getInventory().getInventoryOutMessage(Creature.this.getEquipmentSlots()));
                return ctx.handled();
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Creature.this;
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName()).append(" [name=").append(name)
                .append(", health=").append(this.getHealthBucket())
                .append(", faction=").append(faction).append(", vocation=")
                .append(vocation).append("]");
        return builder.toString();
    }

}
