package com.lhf.game.creature;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.EntityEffect;
import com.lhf.game.ItemContainer;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.DamgeFlavorReaction;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.dice.DamageDice.FlavoredRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemNameSearchVisitor;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.messages.events.ItemEquippedEvent;
import com.lhf.messages.events.ItemEquippedEvent.EquipResultType;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.events.ItemUnequippedEvent;
import com.lhf.messages.events.ItemUnequippedEvent.UnequipResultType;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public abstract class Creature implements ICreature {
    private final String name; // Username for players, description name (e.g., goblin 1) for monsters/NPCs
    private final ICreatureID creatureID;
    private final GameEventProcessorID gameEventProcessorID;
    private CreatureFaction faction; // See shared enum
    private Vocation vocation;
    // private MonsterType monsterType; // I dont know if we'll need this

    private final TreeSet<CreatureEffect> effects;
    private final Statblock statblock;

    private EnumSet<SubAreaSort> subAreaSorts; // what sub area engagements is the creature in?
    private transient CommandInvoker controller;
    private transient CommandChainHandler successor;
    private Map<AMessageType, CommandHandler> cmds;
    private transient final Logger logger;

    protected Creature(ICreature.CreatureBuilder<?, ? extends ICreature> builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock) {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.creatureID = new ICreatureID();
        this.name = builder.getName();
        if (statblock == null) {
            throw new IllegalArgumentException("Creature cannot have a null statblock!");
        }
        if (controller == null) {
            throw new IllegalArgumentException("Creature cannot have a null controller!");
        }
        this.cmds = this.buildCommands();
        this.faction = builder.getFaction();
        this.vocation = VocationFactory.getVocation(builder.getVocation(), builder.getVocationLevel());

        this.effects = new TreeSet<>();
        this.statblock = new Statblock(statblock);
        this.controller = controller;
        this.controller.setSuccessor(this);
        this.successor = successor;
        ItemContainer.transfer(builder.getCorpse(), this.getInventory(), null, false);

        // We don't start them in battle
        this.subAreaSorts = EnumSet.noneOf(SubAreaSort.class);
        this.logger = Logger
                .getLogger(String.format("%s.%s", this.getClass().getName(), this.name.replaceAll("\\W", "_")));
    }

    @Override
    public ICreatureID getCreatureID() {
        return this.creatureID;
    }

    protected Map<AMessageType, CommandHandler> buildCommands() {
        Map<AMessageType, CommandHandler> cmds = new EnumMap<>(
                ICreature.CreatureCommandHandler.creatureCommandHandlers);
        return cmds;
    }

    private int getHealth() {
        return this.statblock.getStats().getOrDefault(Stats.CURRENTHP, 0);
    }

    private int getMaxHealth() {
        return this.statblock.getStats().getOrDefault(Stats.MAXHP, 0);
    }

    @Override
    public HealthBuckets getHealthBucket() {
        return HealthBuckets.calculate(this.getHealth(), this.getMaxHealth());
    }

    @Override
    public boolean isAlive() {
        return this.getHealth() > 0 && this.getMaxHealth() > 0;
    }

    @Override
    public void updateHitpoints(int value) {
        int current = this.getHealth();
        int max = this.getMaxHealth();
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
        if (stat == null) {
            return;
        }
        final BiFunction<Integer, Integer, Integer> merger = (a, b) -> {
            if (a != null && b != null) {
                return a + b;
            }
            return a != null ? a : b;
        };
        switch (stat) {
            case MAXHP:
                this.statblock.getStats().merge(stat, value, merger);
                // fallthrough
            case CURRENTHP:
                int current = this.getHealth();
                int max = this.getMaxHealth();
                current = Integer.max(0, Integer.min(max, current + value)); // stick between 0 and max
                this.statblock.getStats().replace(Stats.CURRENTHP, current);
                break;
            case XPEARNED:
                if (this.vocation != null) {
                    this.vocation.addExperience(value);
                }
                // fallthrough
            case AC:
                // fallthrough
            case PROFICIENCYBONUS:
                // fallthrough
            case XPWORTH:
                // fallthrough
            default:
                this.statblock.getStats().merge(stat, value, merger);
                break;
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
    public final EnumSet<SubAreaSort> getSubAreaSorts() {
        return this.subAreaSorts;
    }

    @Override
    public final boolean addSubArea(SubAreaSort subAreaSort) {
        if (subAreaSort == null) {
            return false;
        }
        return this.subAreaSorts.add(subAreaSort);
    }

    @Override
    public final boolean removeSubArea(SubAreaSort subAreaSort) {
        if (subAreaSort == null) {
            return false;
        }
        return this.subAreaSorts.remove(subAreaSort);
    }

    @Override
    public final boolean isInBattle() {
        return this.getSubAreaSorts().contains(SubAreaSort.BATTLE);
    }

    protected MultiRollResult adjustDamageByFlavor(MultiRollResult mrr, boolean reverse) {
        if (mrr == null) {
            return null;
        }
        MultiRollResult.Builder mrrBuilder = new MultiRollResult.Builder();
        EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> dfr = this.statblock.getDamageFlavorReactions();
        for (RollResult rr : mrr) {
            if (rr instanceof FlavoredRollResult) {
                FlavoredRollResult frr = (FlavoredRollResult) rr;
                if (dfr.get(DamgeFlavorReaction.CURATIVES).contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.negative());
                    } else {
                        mrrBuilder.addRollResults(frr);
                    }
                } else if (dfr.get(DamgeFlavorReaction.IMMUNITIES).contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr.none());
                } else if (dfr.get(DamgeFlavorReaction.RESISTANCES).contains(frr.getDamageFlavor())) {
                    if (reverse) {
                        mrrBuilder.addRollResults(frr.half());
                    } else {
                        mrrBuilder.addRollResults(frr.negative().half());
                    }
                } else if (dfr.get(DamgeFlavorReaction.WEAKNESSES).contains(frr.getDamageFlavor())) {
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
                if (dfr.get(DamgeFlavorReaction.IMMUNITIES).size() > 0) {
                    mrrBuilder.addRollResults(rr.none()); // if they have any immunities, unflavored damge does nothing
                } else if (reverse) {
                    mrrBuilder.addRollResults(rr.negative());
                } else {
                    mrrBuilder.addRollResults(rr);
                }
            }
        }

        if (dfr.get(DamgeFlavorReaction.IMMUNITIES).size() == 0) { // if they have any immunities, unflavored damge
                                                                   // does nothing
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
        ItemNameSearchVisitor visitor = new ItemNameSearchVisitor(itemName);
        this.getInventory().acceptItemVisitor(visitor);
        Optional<Equipable> maybeItem = visitor.getEquipable();
        ItemEquippedEvent.Builder equipMessage = ItemEquippedEvent.getBuilder().setAttemptedItemName(itemName)
                .setNotBroadcast().setAttemptedSlot(slot);
        if (maybeItem.isPresent()) {
            Equipable equipThing = maybeItem.get();
            equipMessage.setItem(equipThing);
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
        ICreature.eventAccepter.accept(this,
                ItemNotPossessedEvent.getBuilder().setNotBroadcast().setItemType(Item.class.getSimpleName())
                        .setItemName(itemName).Build());
        return true;
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        ItemUnequippedEvent.Builder unequipMessage = ItemUnequippedEvent.getBuilder().setNotBroadcast()
                .setAttemptedName(weapon);
        if (slot == null) {
            // if they specified weapon and not slot
            for (final Entry<EquipmentSlots, Equipable> entry : this.getEquipmentSlots().entrySet()) {
                final Equipable equipable = entry.getValue();
                if (equipable == null) {
                    continue;
                }
                if (equipable.checkName(weapon)) {
                    slot = entry.getKey();
                    break;
                }
            }
        }
        unequipMessage.setSlot(slot);
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

    protected void setController(CommandInvoker cont) {
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
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
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
