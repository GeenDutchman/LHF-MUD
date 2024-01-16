package com.lhf.game.creature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder.SummonData;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.AItem;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.messages.events.GameEvent;

public abstract class SummonedINonPlayerCharacter<SummonedType extends INonPlayerCharacter>
        extends WrappedINonPlayerCharacter<SummonedType> {
    protected final EnumSet<SummonData> summonData;
    protected final ICreature summoner;
    protected final Ticker timeLeft;

    protected SummonedINonPlayerCharacter(SummonedType toSummon, EnumSet<SummonData> summonData, ICreature summoner,
            Ticker timeLeft) {
        super(toSummon);
        this.summonData = summonData;
        this.summoner = summoner;
        if (summoner != null) {
            this.setLeaderName(summoner.getName());
        }
        this.timeLeft = timeLeft;
        this.getFaction(); // to set the faction according to the summoner
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        if (this.checkSummonIsAlive()) {
            return super.equipItem(itemName, slot);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'equipItem(itemName, slot)'");
        return false;
    }

    @Override
    public final void restoreFaction() {
        if (this.checkSummonIsAlive()) {
            super.restoreFaction();
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'restoreFaction()'");
    }

    @Override
    public Inventory getInventory() {
        if (this.checkSummonIsAlive()) {
            return super.getInventory();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getInventory()'");
        return new Inventory();
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        if (this.checkSummonIsAlive()) {
            return super.unequipItem(slot, weapon);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'unequipItem(slot, weapon)'");
        return false;
    }

    @Override
    public void setInventory(Inventory inventory) {
        if (this.checkSummonIsAlive()) {
            super.setInventory(inventory);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setInventory(inventory)'");
    }

    @Override
    public String printInventory() {
        if (this.checkSummonIsAlive()) {
            return super.printInventory();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'printInventory()'");
        return "";
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        if (this.checkSummonIsAlive()) {
            return super.getEquipped(slot);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getEquipped(slot)'");
        return null;
    }

    @Override
    public GameEvent processEffect(EntityEffect effect, boolean reverse) {
        if (this.checkSummonIsAlive()) {
            return super.processEffect(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'processEffect(effect, reverse)'");
        return null;
    }

    @Override
    public Collection<IItem> getItems() {
        if (this.checkSummonIsAlive()) {
            return super.getItems();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getItems()'");
        return Set.of();
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        if (this.checkSummonIsAlive()) {
            return super.getEquipmentSlots();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getEquipmentSlots()'");
        return Map.of();
    }

    @Override
    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        if (this.checkSummonIsAlive()) {
            super.setEquipmentSlots(equipmentSlots);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setEquipmentSlots(equipmentSlots)'");
    }

    @Override
    public Optional<IItem> getItem(String name) {
        if (this.checkSummonIsAlive()) {
            return super.getItem(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getItem(name)'");
        return Optional.empty();
    }

    @Override
    public boolean addItem(IItem item) {
        if (this.checkSummonIsAlive()) {
            return super.addItem(item);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'addItem(item)'");
        return false;
    }

    @Override
    public boolean hasItem(String name) {
        if (this.checkSummonIsAlive()) {
            return super.hasItem(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasItem(name)'");
        return false;
    }

    @Override
    public boolean shouldAdd(EntityEffect effect, boolean reverse) {
        if (this.checkSummonIsAlive()) {
            return super.shouldAdd(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'shouldAdd(effect, reverse)'");
        return false;
    }

    @Override
    public Optional<IItem> removeItem(String name) {
        if (this.checkSummonIsAlive()) {
            return super.removeItem(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'removeItem(name)'");
        return Optional.empty();
    }

    @Override
    public boolean removeItem(IItem item) {
        if (this.checkSummonIsAlive()) {
            return super.removeItem(item);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'removeItem(item)'");
        return false;
    }

    @Override
    public Iterator<? extends IItem> itemIterator() {
        if (this.checkSummonIsAlive()) {
            return super.itemIterator();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'itemIterator()'");
        return new ArrayList<IItem>().iterator();
    }

    @Override
    public boolean shouldRemove(EntityEffect effect, boolean reverse) {
        if (this.checkSummonIsAlive()) {
            return super.shouldRemove(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'shouldRemove(effect, reverse)'");
        return false;
    }

    @Override
    public Collection<IItem> filterItems(EnumSet<ItemFilters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends AItem> clazz, Boolean isVisible) {
        if (this.checkSummonIsAlive()) {
            return super.filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible);
        }
        this.log(Level.WARNING,
                "This summon is dead, and cannot perform 'filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible)'");
        return List.of();
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect, boolean reverse) {
        if (this.checkSummonIsAlive()) {
            return super.applyEffect(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'applyEffect(effect, reverse)'");
        return null;
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        super.log(logLevel, logMessage != null ? "Summon: " + logMessage : logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        Supplier<String> nextSupplier = logMessageSupplier != null ? () -> "Summon: " + logMessageSupplier.get()
                : logMessageSupplier;
        super.log(logLevel, nextSupplier);
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect) {
        if (this.checkSummonIsAlive()) {
            return super.applyEffect(effect);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'applyEffect(effect)'");
        return null;
    }

    @Override
    public boolean hasItem(String name, Integer minimumLength) {
        if (this.checkSummonIsAlive()) {
            return super.hasItem(name, minimumLength);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasItem(name, minimumLength)'");
        return false;
    }

    @Override
    public boolean hasItem(IItem item) {
        if (this.checkSummonIsAlive()) {
            return super.hasItem(item);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasItem(item)'");
        return false;
    }

    @Override
    public boolean isEmpty() {
        if (this.checkSummonIsAlive()) {
            return super.isEmpty();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'isEmpty()'");
        return true;
    }

    @Override
    public int size() {
        if (this.checkSummonIsAlive()) {
            return super.size();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'size()'");
        return 0;
    }

    @Override
    public NavigableSet<CreatureEffect> getEffects() {
        if (this.checkSummonIsAlive()) {
            return super.getEffects();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getEffects()'");
        return new TreeSet<>();
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        if (this.checkSummonIsAlive()) {
            return super.getMutableEffects();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getMutableEffects()'");
        return new TreeSet<>();
    }

    @Override
    public void removeEffectByName(String name) {
        if (this.checkSummonIsAlive()) {
            super.removeEffectByName(name);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'removeEffectByName(name)'");
    }

    @Override
    public boolean hasEffect(String name) {
        if (this.checkSummonIsAlive()) {
            return super.hasEffect(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasEffect(name)'");
        return false;
    }

    @Override
    public HealthBuckets getHealthBucket() {
        if (this.checkSummonIsAlive()) {
            return super.getHealthBucket();
        }
        return HealthBuckets.DEAD;
    }

    public final boolean checkSummonIsAlive() {
        if (!this.isAlive()) {
            ICreature.announceDeath(this);
            return false;
        }
        return true;
    }

    public final boolean checkSummonerIsAlive() {
        if (this.summoner == null) {
            return false;
        }
        if (!this.summoner.isAlive()) {
            this.setLeaderName(null);
            return false;
        }
        return true;
    }

    @Override
    public final Corpse generateCorpse(boolean transfer) {
        // returns no corpse for a summon
        return null;
    }

    @Override
    public synchronized boolean isAlive() {
        Consumer<SummonedType> killit = toDie -> {
            EnumMap<Stats, Integer> foundStats = new EnumMap<>(toDie.getStats());
            foundStats.put(Stats.CURRENTHP, 0);
            toDie.setStats(foundStats);
        };

        if (this.timeLeft != null && this.timeLeft.getCountdown() <= 0) {
            this.log(Level.INFO, () -> "Countdown ended");
            killit.accept(this.wrapped);
            return false;
        }
        if (this.checkSummonerIsAlive() && this.summonData != null
                && this.summonData.contains(SummonData.LIFELINE_SUMMON)) {
            this.log(Level.INFO, () -> "Summoner died");
            killit.accept(this.wrapped);
            return false;
        }
        if (!super.isAlive()) {
            this.log(Level.INFO, () -> "Ran out of health");
            return false;
        }
        return true;
    }

    @Override
    public void updateHitpoints(int value) {
        if (this.checkSummonIsAlive()) {
            super.updateHitpoints(value);
            return;
        }
    }

    @Override
    public AttributeBlock getAttributes() {
        if (this.checkSummonIsAlive()) {
            return super.getAttributes();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getAttributes()'");
        Integer newScore = Integer.MIN_VALUE + 20;
        return new AttributeBlock(newScore, newScore, newScore, newScore, newScore, newScore);
    }

    @Override
    @Deprecated
    public void setAttributes(AttributeBlock attributes) {
        if (this.checkSummonIsAlive()) {
            super.setAttributes(attributes);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setAttributes(attributes)'");
    }

    @Override
    public MultiRollResult check(Attributes attribute) {
        if (this.checkSummonIsAlive()) {
            return super.check(attribute);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'check(attribute)'");
        return null;
    }

    @Override
    public void updateModifier(Attributes modifier, int value) {
        if (this.checkSummonIsAlive()) {
            super.updateModifier(modifier, value);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'updateModifier(modifier, value)'");
    }

    @Override
    public Map<Stats, Integer> getStats() {
        if (this.checkSummonIsAlive()) {
            return super.getStats();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getStats()'");
        return Map.of();
    }

    @Override
    public void setStats(EnumMap<Stats, Integer> stats) {
        if (this.checkSummonIsAlive()) {
            super.setStats(stats);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setStats(stats)'");
    }

    @Override
    public CreatureFaction getFaction() {
        if (this.summoner != null && this.summonData != null) {
            boolean summonerAlive = this.checkSummonerIsAlive();
            if (summonerAlive && this.summonData.contains(SummonData.SYMPATHETIC_SUMMON)) {
                super.setFaction(this.summoner.getFaction());
            } else if (!summonerAlive && this.summonData.contains(SummonData.LOYAL_SUMMON)) {
                super.setFaction(this.summoner.getFaction());
            } else {
                this.restoreFaction();
            }
        }
        return super.getFaction();
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        if (this.summoner == null || (!this.checkSummonerIsAlive() && this.summonData != null
                && !this.summonData.contains(SummonData.LOYAL_SUMMON))) {
            super.setFaction(faction);
            return;
        }
        this.log(Level.WARNING, "The summoner is alive, and thus you cannot perform 'setFaction(faction)'");
    }

    @Override
    public Attributes getHighestAttributeBonus(Set<Attributes> attrs) {
        if (this.checkSummonIsAlive()) {
            return super.getHighestAttributeBonus(attrs);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getHighestAttributeBonus(attrs)'");
        return null;
    }

    @Override
    public String printDescription() {
        String summonString = this.summoner != null
                ? " Has been summoned by " + this.summoner.getColorTaggedName() + ". "
                : " Is a summoned creature. ";
        String description = super.printDescription();
        if (description == null) {
            return summonString;
        }
        if (!description.contains(summonString)) {
            return description + summonString;
        }
        return description;
    }

    @Override
    public String getStartTag() {
        return "<summon>";
    }

    @Override
    public String getEndTag() {
        return "</summon>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName()).append(" [wrapped=").append(this.wrapped)
                .append(", summonData=")
                .append(summonData).append(", summoner=")
                .append(summoner).append(", timeLeft=").append(timeLeft).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(summonData, summoner);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof SummonedINonPlayerCharacter))
            return false;
        SummonedINonPlayerCharacter<?> other = (SummonedINonPlayerCharacter<?>) obj;
        return Objects.equals(summonData, other.summonData) && Objects.equals(summoner, other.summoner);
    }

}
