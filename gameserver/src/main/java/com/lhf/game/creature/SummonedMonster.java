package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.events.GameEvent;

public class SummonedMonster extends WrappedMonster {
    protected final ICreature summoner;
    protected final Ticker timeLeft;

    public SummonedMonster(Monster monster, ICreature summoner, Ticker timeLeft) {
        super(monster);
        this.summoner = summoner;
        this.timeLeft = timeLeft;
    }

    public SummonedMonster(WrappedMonster monster, ICreature summoner, Ticker timeLeft) {
        super(monster);
        this.summoner = summoner;
        this.timeLeft = timeLeft;
    }

    public SummonedMonster(MonsterBuilder builder, ICreature summoner, Ticker timeLeft, AIRunner aiRunner,
            CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        super(builder, aiRunner, successor, statblockManager, conversationManager);
        this.summoner = summoner;
        this.timeLeft = timeLeft;
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        if (this.isAlive()) {
            return innerMonster.equipItem(itemName, slot);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'equipItem(itemName, slot)'");
        return false;
    }

    @Override
    public void restoreFaction() {
        if (this.isAlive()) {
            innerMonster.restoreFaction();
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'restoreFaction()'");
    }

    @Override
    public Inventory getInventory() {
        if (this.isAlive()) {
            return innerMonster.getInventory();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getInventory()'");
        return new Inventory();
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        if (this.isAlive()) {
            return innerMonster.unequipItem(slot, weapon);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'unequipItem(slot, weapon)'");
        return false;
    }

    @Override
    public void setInventory(Inventory inventory) {
        if (this.isAlive()) {
            innerMonster.setInventory(inventory);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setInventory(inventory)'");
    }

    @Override
    public String printInventory() {
        if (this.isAlive()) {
            return innerMonster.printInventory();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'printInventory()'");
        return "";
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        if (this.isAlive()) {
            return innerMonster.getEquipped(slot);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getEquipped(slot)'");
        return null;
    }

    @Override
    public GameEvent processEffect(EntityEffect effect, boolean reverse) {
        if (this.isAlive()) {
            return innerMonster.processEffect(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'processEffect(effect, reverse)'");
        return null;
    }

    @Override
    public Collection<Item> getItems() {
        if (this.isAlive()) {
            return innerMonster.getItems();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getItems()'");
        return Set.of();
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        if (this.isAlive()) {
            return innerMonster.getEquipmentSlots();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getEquipmentSlots()'");
        return Map.of();
    }

    @Override
    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        if (this.isAlive()) {
            innerMonster.setEquipmentSlots(equipmentSlots);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setEquipmentSlots(equipmentSlots)'");
    }

    @Override
    public Optional<Item> getItem(String name) {
        if (this.isAlive()) {
            return innerMonster.getItem(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getItem(name)'");
        return Optional.empty();
    }

    @Override
    public boolean addItem(Item item) {
        if (this.isAlive()) {
            return innerMonster.addItem(item);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'addItem(item)'");
        return false;
    }

    @Override
    public boolean hasItem(String name) {
        if (this.isAlive()) {
            return innerMonster.hasItem(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasItem(name)'");
        return false;
    }

    @Override
    public boolean shouldAdd(EntityEffect effect, boolean reverse) {
        if (this.isAlive()) {
            return innerMonster.shouldAdd(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'shouldAdd(effect, reverse)'");
        return false;
    }

    @Override
    public Optional<Item> removeItem(String name) {
        if (this.isAlive()) {
            return innerMonster.removeItem(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'removeItem(name)'");
        return Optional.empty();
    }

    @Override
    public boolean removeItem(Item item) {
        if (this.isAlive()) {
            return innerMonster.removeItem(item);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'removeItem(item)'");
        return false;
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        if (this.isAlive()) {
            return innerMonster.itemIterator();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'itemIterator()'");
        return new ArrayList<Item>().iterator();
    }

    @Override
    public boolean shouldRemove(EntityEffect effect, boolean reverse) {
        if (this.isAlive()) {
            return innerMonster.shouldRemove(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'shouldRemove(effect, reverse)'");
        return false;
    }

    @Override
    public Collection<Item> filterItems(EnumSet<Filters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends Item> clazz, Boolean isVisible) {
        if (this.isAlive()) {
            return innerMonster.filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible);
        }
        this.log(Level.WARNING,
                "This summon is dead, and cannot perform 'filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible)'");
        return List.of();
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect, boolean reverse) {
        if (this.isAlive()) {
            return innerMonster.applyEffect(effect, reverse);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'applyEffect(effect, reverse)'");
        return null;
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        innerMonster.log(logLevel, logMessage != null ? "Summon: " + logMessage : logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        Supplier<String> nextSupplier = logMessageSupplier != null ? () -> "Summon: " + logMessageSupplier.get()
                : logMessageSupplier;
        innerMonster.log(logLevel, nextSupplier);
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect) {
        if (this.isAlive()) {
            return innerMonster.applyEffect(effect);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'applyEffect(effect)'");
        return null;
    }

    @Override
    public boolean hasItem(String name, Integer minimumLength) {
        if (this.isAlive()) {
            return innerMonster.hasItem(name, minimumLength);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasItem(name, minimumLength)'");
        return false;
    }

    @Override
    public boolean hasItem(Item item) {
        if (this.isAlive()) {
            return innerMonster.hasItem(item);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasItem(item)'");
        return false;
    }

    @Override
    public boolean isEmpty() {
        if (this.isAlive()) {
            return innerMonster.isEmpty();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'isEmpty()'");
        return true;
    }

    @Override
    public int size() {
        if (this.isAlive()) {
            return innerMonster.size();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'size()'");
        return 0;
    }

    @Override
    public NavigableSet<CreatureEffect> getEffects() {
        if (this.isAlive()) {
            return innerMonster.getEffects();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getEffects()'");
        return new TreeSet<>();
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        if (this.isAlive()) {
            return innerMonster.getMutableEffects();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getMutableEffects()'");
        return new TreeSet<>();
    }

    @Override
    public void removeEffectByName(String name) {
        if (this.isAlive()) {
            innerMonster.removeEffectByName(name);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'removeEffectByName(name)'");
    }

    @Override
    public boolean hasEffect(String name) {
        if (this.isAlive()) {
            return innerMonster.hasEffect(name);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'hasEffect(name)'");
        return false;
    }

    @Override
    public HealthBuckets getHealthBucket() {
        if (this.isAlive()) {
            return innerMonster.getHealthBucket();
        }
        return HealthBuckets.DEAD;
    }

    @Override
    public synchronized boolean isAlive() {
        if (this.timeLeft != null && this.timeLeft.getCountdown() <= 0) {
            ICreature.announceDeath(this);
            return false;
        }
        if (this.summoner != null && !this.summoner.isAlive()) {
            ICreature.announceDeath(this);
            return false;
        }
        if (!innerMonster.isAlive()) {
            ICreature.announceDeath(this);
            return false;
        }
        return true;
    }

    @Override
    public void updateHitpoints(int value) {
        if (this.isAlive()) {
            innerMonster.updateHitpoints(value);
            return;
        }
    }

    @Override
    public AttributeBlock getAttributes() {
        if (this.isAlive()) {
            return innerMonster.getAttributes();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getAttributes()'");
        Integer newScore = Integer.MIN_VALUE + 20;
        return new AttributeBlock(newScore, newScore, newScore, newScore, newScore, newScore);
    }

    @Override
    @Deprecated
    public void setAttributes(AttributeBlock attributes) {
        if (this.isAlive()) {
            innerMonster.setAttributes(attributes);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setAttributes(attributes)'");
    }

    @Override
    public MultiRollResult check(Attributes attribute) {
        if (this.isAlive()) {
            return innerMonster.check(attribute);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'check(attribute)'");
        return null;
    }

    @Override
    public void updateModifier(Attributes modifier, int value) {
        if (this.isAlive()) {
            innerMonster.updateModifier(modifier, value);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'updateModifier(modifier, value)'");
    }

    @Override
    public Map<Stats, Integer> getStats() {
        if (this.isAlive()) {
            return innerMonster.getStats();
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getStats()'");
        return Map.of();
    }

    @Override
    public void setStats(EnumMap<Stats, Integer> stats) {
        if (this.isAlive()) {
            innerMonster.setStats(stats);
            return;
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'setStats(stats)'");
    }

    @Override
    public CreatureFaction getFaction() {
        if (this.summoner != null && this.summoner.isAlive()) {
            return this.summoner.getFaction();
        }
        return innerMonster.getFaction();
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        if (this.summoner == null || !this.summoner.isAlive()) {
            innerMonster.setFaction(faction);
            return;
        }
        this.log(Level.WARNING, "The summoner is alive, and thus you cannot perform 'setFaction(faction)'");
    }

    @Override
    public Attributes getHighestAttributeBonus(EnumSet<Attributes> attrs) {
        if (this.isAlive()) {
            return innerMonster.getHighestAttributeBonus(attrs);
        }
        this.log(Level.WARNING, "This summon is dead, and cannot perform 'getHighestAttributeBonus(attrs)'");
        return null;
    }

    @Override
    public String printDescription() {
        String summonString = this.summoner != null
                ? " Has been summoned by " + this.summoner.getColorTaggedName() + ". "
                : " Is a summoned creature. ";
        String description = innerMonster.printDescription();
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

}
