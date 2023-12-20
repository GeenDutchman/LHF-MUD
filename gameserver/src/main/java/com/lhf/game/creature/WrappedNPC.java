package com.lhf.game.creature;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.EntityEffect;
import com.lhf.game.TickType;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
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
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.OutMessage.Builder;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.server.client.ClientID;
import com.lhf.server.interfaces.NotNull;

public abstract class WrappedNPC implements INonPlayerCharacter {
    protected final INonPlayerCharacter innerNPC;

    protected WrappedNPC(@NotNull WrappedNPC npc) {
        this.innerNPC = npc;
    }

    /**
     * Note that this can mask a Monster
     */
    protected WrappedNPC(@NotNull NonPlayerCharacter npc) {
        this.innerNPC = npc;
    }

    public INonPlayerCharacter unwrap() {
        return this.innerNPC;
    }

    @Override
    public SeeOutMessage produceMessage() {
        return innerNPC.produceMessage();
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        return innerNPC.equipItem(itemName, slot);
    }

    @Override
    public void setSuccessor(MessageChainHandler successor) {
        innerNPC.setSuccessor(successor);
    }

    @Override
    public Inventory getInventory() {
        return innerNPC.getInventory();
    }

    @Override
    public void sendMsg(Builder<?> builder) {
        innerNPC.sendMsg(builder);
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        return innerNPC.unequipItem(slot, weapon);
    }

    @Override
    public void setInventory(Inventory inventory) {
        innerNPC.setInventory(inventory);
    }

    @Override
    public MessageChainHandler getSuccessor() {
        return innerNPC.getSuccessor();
    }

    @Override
    public String printInventory() {
        return innerNPC.printInventory();
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return innerNPC.getEquipped(slot);
    }

    @Override
    public OutMessage processEffect(EntityEffect effect, boolean reverse) {
        return innerNPC.processEffect(effect, reverse);
    }

    @Override
    public void intercept(MessageChainHandler interceptor) {
        innerNPC.intercept(interceptor);
    }

    @Override
    public Collection<Item> getItems() {
        return innerNPC.getItems();
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return innerNPC.getEquipmentSlots();
    }

    @Override
    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        innerNPC.setEquipmentSlots(equipmentSlots);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return innerNPC.addSelfToContext(ctx);
    }

    @Override
    public Optional<Item> getItem(String name) {
        return innerNPC.getItem(name);
    }

    @Override
    public boolean isCorrectEffectType(EntityEffect effect) {
        return innerNPC.isCorrectEffectType(effect);
    }

    @Override
    public boolean addItem(Item item) {
        return innerNPC.addItem(item);
    }

    @Override
    public boolean hasItem(String name) {
        return innerNPC.hasItem(name);
    }

    @Override
    public boolean shouldAdd(EntityEffect effect, boolean reverse) {
        return innerNPC.shouldAdd(effect, reverse);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        return innerNPC.removeItem(name);
    }

    @Override
    public boolean removeItem(Item item) {
        return innerNPC.removeItem(item);
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        return innerNPC.itemIterator();
    }

    @Override
    public boolean shouldRemove(EntityEffect effect, boolean reverse) {
        return innerNPC.shouldRemove(effect, reverse);
    }

    @Override
    public Collection<Item> filterItems(EnumSet<Filters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends Item> clazz, Boolean isVisible) {
        return innerNPC.filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible);
    }

    @Override
    public OutMessage applyEffect(CreatureEffect effect, boolean reverse) {
        return innerNPC.applyEffect(effect, reverse);
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return innerNPC.getCommands(ctx);
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        innerNPC.log(logLevel, logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        innerNPC.log(logLevel, logMessageSupplier);
    }

    @Override
    public Reply handle(CommandContext ctx, Command cmd) {
        return innerNPC.handle(ctx, cmd);
    }

    @Override
    public OutMessage applyEffect(CreatureEffect effect) {
        return innerNPC.applyEffect(effect);
    }

    @Override
    public boolean hasItem(String name, Integer minimumLength) {
        return innerNPC.hasItem(name, minimumLength);
    }

    @Override
    public void tick(TickType type) {
        innerNPC.tick(type);
    }

    @Override
    public boolean hasItem(Item item) {
        return innerNPC.hasItem(item);
    }

    @Override
    public boolean isEmpty() {
        return innerNPC.isEmpty();
    }

    @Override
    public int size() {
        return innerNPC.size();
    }

    @Override
    public NavigableSet<CreatureEffect> getEffects() {
        return innerNPC.getEffects();
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        return innerNPC.getMutableEffects();
    }

    @Override
    public Reply handleChain(CommandContext ctx, Command cmd) {
        return innerNPC.handleChain(ctx, cmd);
    }

    @Override
    public void removeEffectByName(String name) {
        innerNPC.removeEffectByName(name);
    }

    @Override
    public boolean hasEffect(String name) {
        return innerNPC.hasEffect(name);
    }

    @Override
    public ClientMessenger getController() {
        return innerNPC.getController();
    }

    @Override
    public ClientID getClientID() {
        return innerNPC.getClientID();
    }

    @Override
    public void sendMsg(OutMessage msg) {
        innerNPC.sendMsg(msg);
    }

    @Override
    public HealthBuckets getHealthBucket() {
        return innerNPC.getHealthBucket();
    }

    @Override
    public boolean isAlive() {
        return innerNPC.isAlive();
    }

    @Override
    public void updateHitpoints(int value) {
        innerNPC.updateHitpoints(value);
    }

    @Override
    public void updateXp(int value) {
        innerNPC.updateXp(value);
    }

    @Override
    public AttributeBlock getAttributes() {
        return innerNPC.getAttributes();
    }

    @Override
    public void setAttributes(AttributeBlock attributes) {
        innerNPC.setAttributes(attributes);
    }

    @Override
    public MultiRollResult check(Attributes attribute) {
        return innerNPC.check(attribute);
    }

    @Override
    public void updateModifier(Attributes modifier, int value) {
        innerNPC.updateModifier(modifier, value);
    }

    @Override
    public Map<Stats, Integer> getStats() {
        return innerNPC.getStats();
    }

    @Override
    public void setStats(EnumMap<Stats, Integer> stats) {
        innerNPC.setStats(stats);
    }

    @Override
    public String getName() {
        return innerNPC.getName();
    }

    @Override
    public Weapon defaultWeapon() {
        return innerNPC.defaultWeapon();
    }

    @Override
    public boolean checkName(String otherName) {
        return innerNPC.checkName(otherName);
    }

    @Override
    public boolean CheckNameRegex(String possName, Integer minimumLength) {
        return innerNPC.CheckNameRegex(possName, minimumLength);
    }

    @Override
    public void setConvoTree(ConversationTree tree) {
        innerNPC.setConvoTree(tree);
    }

    @Override
    public void setConvoTree(ConversationManager manager, String name) {
        innerNPC.setConvoTree(manager, name);
    }

    @Override
    public CreatureFaction getFaction() {
        return innerNPC.getFaction();
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        innerNPC.setFaction(faction);
    }

    @Override
    public Attributes getHighestAttributeBonus(EnumSet<Attributes> attrs) {
        return innerNPC.getHighestAttributeBonus(attrs);
    }

    @Override
    public ConversationTree getConvoTree() {
        return innerNPC.getConvoTree();
    }

    @Override
    public void restoreFaction() {
        innerNPC.restoreFaction();
    }

    @Override
    public HarmMemories getHarmMemories() {
        return innerNPC.getHarmMemories();
    }

    @Override
    public Set<EquipmentTypes> getProficiencies() {
        return innerNPC.getProficiencies();
    }

    @Override
    public void setController(ClientMessenger cont) {
        innerNPC.setController(cont);
    }

    @Override
    public void setProficiencies(EnumSet<EquipmentTypes> proficiences) {
        innerNPC.setProficiencies(proficiences);
    }

    @Override
    public boolean isInBattle() {
        return innerNPC.isInBattle();
    }

    @Override
    public void setInBattle(boolean inBattle) {
        innerNPC.setInBattle(inBattle);
    }

    @Override
    public Attack attack(Weapon weapon) {
        return innerNPC.attack(weapon);
    }

    @Override
    public Attack attack(String itemName, String target) {
        return innerNPC.attack(itemName, target);
    }

    @Override
    public String getCreatureRace() {
        return innerNPC.getCreatureRace();
    }

    @Override
    public void setCreatureRace(String creatureRace) {
        innerNPC.setCreatureRace(creatureRace);
    }

    @Override
    public Vocation getVocation() {
        return innerNPC.getVocation();
    }

    @Override
    public void setVocation(Vocation job) {
        innerNPC.setVocation(job);
    }

    @Override
    public String printDescription() {
        return innerNPC.printDescription();
    }

    @Override
    public SeeOutMessage produceMessage(com.lhf.messages.out.SeeOutMessage.Builder seeOutMessage) {
        return innerNPC.produceMessage(seeOutMessage);
    }

    @Override
    public String getStartTag() {
        return innerNPC.getStartTag();
    }

    @Override
    public String getEndTag() {
        return innerNPC.getEndTag();
    }

    @Override
    public String getColorTaggedName() {
        return innerNPC.getColorTaggedName();
    }

    @Override
    public int compareTo(ICreature other) {
        return innerNPC.compareTo(other);
    }

}