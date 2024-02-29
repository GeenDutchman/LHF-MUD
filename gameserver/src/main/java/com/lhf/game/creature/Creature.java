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

import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.VocationFactory;
import com.lhf.game.dice.DamageDice.FlavoredRollResult;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamgeFlavorReaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.ItemNameSearchVisitor;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.CreatureAffectedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemEquippedEvent;
import com.lhf.messages.events.ItemEquippedEvent.EquipResultType;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.events.ItemUnequippedEvent;
import com.lhf.messages.events.ItemUnequippedEvent.UnequipResultType;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public abstract class Creature implements ICreature {
    private final String creatureRace;
    private final AttributeBlock attributeBlock;
    private final EnumMap<Stats, Integer> stats;
    private final EnumSet<EquipmentTypes> proficiencies;
    private final Inventory inventory;
    private final EnumMap<EquipmentSlots, Equipable> equipment;
    private final EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> damageFlavorReactions;
    private final String name; // Username for players, description name (e.g., goblin 1) for monsters/NPCs
    private final ICreatureID creatureID;
    private final GameEventProcessorID gameEventProcessorID;
    private CreatureFaction faction; // See shared enum
    private Vocation vocation;
    private final TreeSet<CreatureEffect> effects;

    private EnumSet<SubAreaSort> subAreaSorts; // what sub area engagements is the creature in?
    private transient CommandInvoker controller;
    private transient CommandChainHandler successor;
    private transient Map<AMessageType, CommandHandler> cmds;
    private transient final Logger logger;

    protected Creature(ICreatureBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor) {
        this.gameEventProcessorID = new GameEventProcessorID();
        this.creatureID = new ICreatureID();
        this.name = builder.getName();
        if (controller == null) {
            throw new IllegalArgumentException("Creature cannot have a null controller!");
        }
        this.creatureRace = builder.getCreatureRace();
        this.attributeBlock = builder.getAttributeBlock();
        this.stats = builder.getStats();
        this.proficiencies = builder.getProficiencies();
        this.inventory = builder.getInventory();
        this.equipment = builder.getEquipmentSlots();
        this.damageFlavorReactions = builder.getDamageFlavorReactions();
        this.cmds = this.buildCommands();
        this.faction = builder.getFaction();
        this.vocation = VocationFactory.getVocation(builder.getVocation(), builder.getVocationLevel());

        this.effects = new TreeSet<>();
        this.controller = controller;
        this.controller.setSuccessor(this);
        this.successor = successor;

        // We don't start them in battle
        this.subAreaSorts = EnumSet.noneOf(SubAreaSort.class);
        this.logger = Logger
                .getLogger(String.format("%s.%s", this.getClass().getName(), this.name.replaceAll("\\W", "_")));

        // re-equip them
        if (equipment != null && !equipment.isEmpty()) {
            for (final Equipable thing : equipment.values()) {
                if (thing == null) {
                    continue;
                }
                thing.onEquippedBy(this);
            }
        }
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
        return this.getStats().getOrDefault(Stats.CURRENTHP, 0);
    }

    private int getMaxHealth() {
        return this.getStats().getOrDefault(Stats.MAXHP, 0);
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
        this.stats.replace(Stats.CURRENTHP, current);
        if (current <= 0) {
            ICreature.announceDeath(this);
        }
    }

    @Override
    public void updateXp(int value) {
        int current = this.getStats().getOrDefault(Stats.XPEARNED, 0);
        current += value;
        this.stats.replace(Stats.XPEARNED, current);
        if (this.vocation != null) {
            this.vocation.addExperience(value);
        }
    }

    private void updateAttribute(Attributes attribute, int value) {
        this.attributeBlock.setScoreBonus(attribute, attributeBlock.getScoreBonus(attribute) + value);
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
                this.stats.merge(stat, value, merger);
                // fallthrough
            case CURRENTHP:
                int current = this.getHealth();
                int max = this.getMaxHealth();
                current = Integer.max(0, Integer.min(max, current + value)); // stick between 0 and max
                this.stats.replace(Stats.CURRENTHP, current);
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
                this.stats.merge(stat, value, merger);
                break;
        }

    }

    /* start getters */

    @Override
    public Map<Stats, Integer> getStats() {
        return Collections.unmodifiableMap(this.stats);
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
        return this.attributeBlock;
    }

    @Override
    public Set<EquipmentTypes> getProficiencies() {
        return this.proficiencies;
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return this.equipment;
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

    private MultiRollResult adjustDamageByFlavor(MultiRollResult mrr) {
        if (mrr == null) {
            return null;
        }
        MultiRollResult.Builder mrrBuilder = new MultiRollResult.Builder();
        final EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> dfr = this.damageFlavorReactions;
        for (RollResult rr : mrr) {
            if (rr instanceof FlavoredRollResult) {
                FlavoredRollResult frr = (FlavoredRollResult) rr;
                if (dfr.get(DamgeFlavorReaction.CURATIVES).contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr);
                } else if (dfr.get(DamgeFlavorReaction.IMMUNITIES).contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr.none());
                } else if (dfr.get(DamgeFlavorReaction.RESISTANCES).contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr.negative().half());
                } else if (dfr.get(DamgeFlavorReaction.WEAKNESSES).contains(frr.getDamageFlavor())) {
                    mrrBuilder.addRollResults(frr.negative().twice());
                } else {
                    mrrBuilder.addRollResults(frr.negative());
                }
            } else {
                if (dfr.get(DamgeFlavorReaction.IMMUNITIES).size() > 0) {
                    mrrBuilder.addRollResults(rr.none()); // if they have any immunities, unflavored damge does nothing
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

    private CreatureAffectedEvent.Builder processEffectDelta(CreatureEffect creatureEffect, Deltas deltas,
            MultiRollResult preAdjustedDamages) {
        CreatureAffectedEvent.Builder builder = CreatureAffectedEvent.getBuilder().setAffected(this)
                .setHighlightedDelta(deltas).fromCreatureEffect(creatureEffect);
        if (deltas == null) {
            return builder;
        }
        if (preAdjustedDamages != null && !preAdjustedDamages.isEmpty()) {
            builder.setDamages(preAdjustedDamages);
            this.updateHitpoints(preAdjustedDamages.getRoll());
        }
        for (Stats delta : deltas.getStatChanges().keySet()) {
            int amount = deltas.getStatChanges().getOrDefault(delta, 0);
            this.updateStat(delta, amount);
        }
        if (this.isAlive()) {
            for (Attributes delta : deltas.getAttributeScoreChanges().keySet()) {
                int amount = deltas.getAttributeScoreChanges().getOrDefault(delta, 0);
                this.updateAttribute(delta, amount);
            }
            for (Attributes delta : deltas.getAttributeBonusChanges().keySet()) {
                int amount = deltas.getAttributeBonusChanges().getOrDefault(delta, 0);
                this.updateModifier(delta, amount);
            }
            // for now...cannot curse someone with being a renegade
            if (deltas.isRestoreFaction()) {
                this.restoreFaction();
            }
        } else {
            ICreature.announceDeath(this);
        }
        return builder;
    }

    @Override
    public GameEvent processEffectEvent(final CreatureEffect effect, final GameEvent event) {
        if (effect == null) {
            this.log(Level.WARNING, "Cannot process null effect for any event");
            return null;
        }
        final Deltas deltas = effect.getDeltasForEvent(event);
        if (deltas == null) {
            this.log(Level.FINE,
                    () -> String.format("Effect %s does nothing on event %s", effect.getName(), event.getEventType()));
            return null;
        }
        final MultiRollResult damages = effect.getEventDamageResult(event,
                (mrr) -> this.adjustDamageByFlavor(mrr));
        CreatureAffectedEvent changeEvent = this.processEffectDelta(effect, deltas, damages).Build();
        this.announce(changeEvent);
        return changeEvent;
    }

    @Override
    public CreatureAffectedEvent processEffectApplication(CreatureEffect effect) {
        if (effect == null) {
            this.log(Level.WARNING, "Cannot process application of null effect");
            return null;
        }
        final Deltas deltas = effect.getApplicationDeltas();
        if (deltas == null) {
            this.log(Level.FINE,
                    () -> String.format("Effect %s does nothing on application", effect.getName()));
            return null;
        }
        final MultiRollResult damages = effect
                .getApplicationDamageResult((mrr) -> this.adjustDamageByFlavor(mrr));
        CreatureAffectedEvent camOut = this.processEffectDelta(effect, deltas, damages).Build();
        this.announce(camOut);
        return camOut;
    }

    @Override
    public GameEvent processEffectRemoval(CreatureEffect effect) {
        if (effect == null) {
            this.log(Level.WARNING, "Cannot process removal of null effect");
            return null;
        }
        final Deltas deltas = effect.getOnRemovalDeltas();
        if (deltas == null) {
            this.log(Level.FINE,
                    () -> String.format("Effect %s does nothing on removal", effect.getName()));
            return null;
        }
        final MultiRollResult damages = effect
                .getApplicationDamageResult((mrr) -> this.adjustDamageByFlavor(mrr));
        CreatureAffectedEvent camOut = this.processEffectDelta(effect, deltas, damages).Build();
        this.announce(camOut);
        return camOut;
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        return this.effects;
    }

    @Override
    public String getCreatureRace() {
        return this.creatureRace;
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
    public boolean equals(Object obj) {
        if (!(obj instanceof ICreature)) {
            return false;
        }
        ICreature c = (ICreature) obj;
        return c.getName().equals(getName());
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public String printInventory() {
        return this.getInventory().getInventoryOutMessage(this.getEquipmentSlots()).toString();
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
                ItemNotPossessedEvent.getBuilder().setNotBroadcast().setItemType(IItem.class.getSimpleName())
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
    public Optional<IItem> removeItem(String name) {
        Optional<IItem> toRemove = ICreature.super.removeItem(name);
        if (toRemove.isEmpty()) {
            for (IItem item : this.getEquipmentSlots().values()) {
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
