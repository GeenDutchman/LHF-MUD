package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.EntityEffect;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.Weapon;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEvent.Builder;
import com.lhf.messages.events.SeeEvent;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public abstract class WrappedMonster implements IMonster {
    protected final IMonster innerMonster;

    protected WrappedMonster(@NotNull WrappedMonster monster) {
        this.innerMonster = monster;
    }

    protected WrappedMonster(@NotNull Monster monster) {
        this.innerMonster = monster;
    }

    protected WrappedMonster(@NotNull MonsterBuilder builder, AIRunner aiRunner, CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        this.innerMonster = builder.build(aiRunner, successor, statblockManager, conversationManager);
    }

    public IMonster unwrap() {
        return this.innerMonster;
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        return innerMonster.equipItem(itemName, slot);
    }

    @Override
    public void restoreFaction() {
        innerMonster.restoreFaction();
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        innerMonster.setSuccessor(successor);
    }

    @Override
    public Inventory getInventory() {
        return innerMonster.getInventory();
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        return innerMonster.unequipItem(slot, weapon);
    }

    @Override
    public void setInventory(Inventory inventory) {
        innerMonster.setInventory(inventory);
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return innerMonster.getSuccessor();
    }

    @Override
    public long getMonsterNumber() {
        return innerMonster.getMonsterNumber();
    }

    @Override
    public String printInventory() {
        return innerMonster.printInventory();
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return innerMonster.getEquipped(slot);
    }

    @Override
    public GameEvent processEffect(EntityEffect effect, boolean reverse) {
        return innerMonster.processEffect(effect, reverse);
    }

    @Override
    public Collection<Item> getItems() {
        return innerMonster.getItems();
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return innerMonster.getEquipmentSlots();
    }

    @Override
    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        innerMonster.setEquipmentSlots(equipmentSlots);
    }

    @Override
    public Optional<Item> getItem(String name) {
        return innerMonster.getItem(name);
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return innerMonster.isCorrectEffectType(effect);
    }

    @Override
    public boolean addItem(Item item) {
        return innerMonster.addItem(item);
    }

    @Override
    public boolean hasItem(String name) {
        return innerMonster.hasItem(name);
    }

    @Override
    public boolean shouldAdd(EntityEffect effect, boolean reverse) {
        return innerMonster.shouldAdd(effect, reverse);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        return innerMonster.removeItem(name);
    }

    @Override
    public boolean removeItem(Item item) {
        return innerMonster.removeItem(item);
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        return innerMonster.itemIterator();
    }

    @Override
    public boolean shouldRemove(EntityEffect effect, boolean reverse) {
        return innerMonster.shouldRemove(effect, reverse);
    }

    @Override
    public Collection<Item> filterItems(EnumSet<Filters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends Item> clazz, Boolean isVisible) {
        return innerMonster.filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible);
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect, boolean reverse) {
        return innerMonster.applyEffect(effect, reverse);
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return innerMonster.getCommands(ctx);
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        innerMonster.log(logLevel, logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        innerMonster.log(logLevel, logMessageSupplier);
    }

    @Override
    public Reply handle(CommandContext ctx, Command cmd) {
        return innerMonster.handle(ctx, cmd);
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect) {
        return innerMonster.applyEffect(effect);
    }

    @Override
    public boolean hasItem(String name, Integer minimumLength) {
        return innerMonster.hasItem(name, minimumLength);
    }

    @Override
    public boolean hasItem(Item item) {
        return innerMonster.hasItem(item);
    }

    @Override
    public boolean isEmpty() {
        return innerMonster.isEmpty();
    }

    @Override
    public int size() {
        return innerMonster.size();
    }

    @Override
    public NavigableSet<CreatureEffect> getEffects() {
        return innerMonster.getEffects();
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        return innerMonster.getMutableEffects();
    }

    @Override
    public Reply handleChain(CommandContext ctx, Command cmd) {
        return innerMonster.handleChain(ctx, cmd);
    }

    @Override
    public void removeEffectByName(String name) {
        innerMonster.removeEffectByName(name);
    }

    @Override
    public boolean hasEffect(String name) {
        return innerMonster.hasEffect(name);
    }

    @Override
    public CommandInvoker getController() {
        return innerMonster.getController();
    }

    @Override
    public ClientID getClientID() {
        return innerMonster.getClientID();
    }

    @Override
    public HealthBuckets getHealthBucket() {
        return innerMonster.getHealthBucket();
    }

    @Override
    public boolean isAlive() {
        return innerMonster.isAlive();
    }

    @Override
    public void updateHitpoints(int value) {
        innerMonster.updateHitpoints(value);
    }

    @Override
    public void updateXp(int value) {
        innerMonster.updateXp(value);
    }

    @Override
    public AttributeBlock getAttributes() {
        return innerMonster.getAttributes();
    }

    @Override
    @Deprecated
    public void setAttributes(AttributeBlock attributes) {
        innerMonster.setAttributes(attributes);
    }

    @Override
    public MultiRollResult check(Attributes attribute) {
        return innerMonster.check(attribute);
    }

    @Override
    public void updateModifier(Attributes modifier, int value) {
        innerMonster.updateModifier(modifier, value);
    }

    @Override
    public Map<Stats, Integer> getStats() {
        return innerMonster.getStats();
    }

    @Override
    public void setStats(EnumMap<Stats, Integer> stats) {
        innerMonster.setStats(stats);
    }

    @Override
    public String getName() {
        return innerMonster.getName();
    }

    @Override
    public Weapon defaultWeapon() {
        return innerMonster.defaultWeapon();
    }

    @Override
    public boolean checkName(String otherName) {
        return innerMonster.checkName(otherName);
    }

    @Override
    public boolean CheckNameRegex(String possName, Integer minimumLength) {
        return innerMonster.CheckNameRegex(possName, minimumLength);
    }

    @Override
    public void setConvoTree(ConversationTree tree) {
        innerMonster.setConvoTree(tree);
    }

    @Override
    public void setConvoTree(ConversationManager manager, String name) {
        innerMonster.setConvoTree(manager, name);
    }

    @Override
    public CreatureFaction getFaction() {
        return innerMonster.getFaction();
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        innerMonster.setFaction(faction);
    }

    @Override
    public Attributes getHighestAttributeBonus(EnumSet<Attributes> attrs) {
        return innerMonster.getHighestAttributeBonus(attrs);
    }

    @Override
    public ConversationTree getConvoTree() {
        return innerMonster.getConvoTree();
    }

    @Override
    public HarmMemories getHarmMemories() {
        return innerMonster.getHarmMemories();
    }

    @Override
    public Set<EquipmentTypes> getProficiencies() {
        return innerMonster.getProficiencies();
    }

    @Override
    public void setController(CommandInvoker cont) {
        innerMonster.setController(cont);
    }

    @Override
    public void setProficiencies(EnumSet<EquipmentTypes> proficiences) {
        innerMonster.setProficiencies(proficiences);
    }

    @Override
    public boolean isInBattle() {
        return innerMonster.isInBattle();
    }

    @Override
    public void setInBattle(boolean inBattle) {
        innerMonster.setInBattle(inBattle);
    }

    @Override
    public Attack attack(Weapon weapon) {
        return innerMonster.attack(weapon);
    }

    @Override
    public Attack attack(String itemName, String target) {
        return innerMonster.attack(itemName, target);
    }

    @Override
    public String getCreatureRace() {
        return innerMonster.getCreatureRace();
    }

    @Override
    public void setCreatureRace(String creatureRace) {
        innerMonster.setCreatureRace(creatureRace);
    }

    @Override
    public Vocation getVocation() {
        return innerMonster.getVocation();
    }

    @Override
    public void setVocation(Vocation job) {
        innerMonster.setVocation(job);
    }

    @Override
    public String printDescription() {
        return innerMonster.printDescription();
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.Builder seeOutMessage) {
        return innerMonster.produceMessage(seeOutMessage);
    }

    @Override
    public String getStartTag() {
        return innerMonster.getStartTag();
    }

    @Override
    public String getEndTag() {
        return innerMonster.getEndTag();
    }

    @Override
    public String getColorTaggedName() {
        return innerMonster.getColorTaggedName();
    }

    @Override
    public int compareTo(ICreature other) {
        return innerMonster.compareTo(other);
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return innerMonster.getEventProcessorID();
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return innerMonster.getGameEventProcessors();
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return innerMonster.getAcceptHook();
    }

    @Override
    public void tick(ITickEvent tickEvent) {
        innerMonster.tick(tickEvent);
    }

    @Override
    public SeeEvent produceMessage() {
        return innerMonster.produceMessage();
    }

    @Override
    public void intercept(CommandChainHandler interceptor) {
        innerMonster.intercept(interceptor);
    }

    @Override
    public boolean announceDirect(GameEvent gameEvent, Collection<? extends GameEventProcessor> recipients) {
        return innerMonster.announceDirect(gameEvent, recipients);
    }

    @Override
    public boolean announceDirect(GameEvent gameEvent, GameEventProcessor... recipients) {
        return innerMonster.announceDirect(gameEvent, recipients);
    }

    @Override
    public boolean announce(GameEvent gameEvent, Set<? extends GameEventProcessor> deafened) {
        return innerMonster.announce(gameEvent, deafened);
    }

    @Override
    public boolean announce(Builder<?> builder, Set<? extends GameEventProcessor> deafened) {
        return innerMonster.announce(builder, deafened);
    }

    @Override
    public boolean announce(GameEvent gameEvent, GameEventProcessor... deafened) {
        return innerMonster.announce(gameEvent, deafened);
    }

    @Override
    public boolean announce(Builder<?> builder, GameEventProcessor... deafened) {
        return innerMonster.announce(builder, deafened);
    }

    @Override
    public boolean announce(GameEvent gameEvent) {
        return innerMonster.announce(gameEvent);
    }

    @Override
    public boolean announce(Builder<?> builder) {
        return innerMonster.announce(builder);
    }

    @Override
    public CommandInvoker getInnerCommandInvoker() {
        return innerMonster;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getCreature() == null) {
            ctx.setCreature(this);
        }
        return ctx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerMonster);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof WrappedMonster))
            return false;
        WrappedMonster other = (WrappedMonster) obj;
        return Objects.equals(innerMonster, other.innerMonster);
    }

}