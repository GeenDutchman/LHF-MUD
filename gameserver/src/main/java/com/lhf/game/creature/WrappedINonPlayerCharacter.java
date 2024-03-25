package com.lhf.game.creature;

import java.util.Collection;
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

import com.lhf.game.CreatureContainer;
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
import com.lhf.game.item.AItem;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Weapon;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.events.CreatureStatusRequestedEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEvent.Builder;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.Client.ClientID;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public abstract class WrappedINonPlayerCharacter<WrappedType extends INonPlayerCharacter>
        implements INonPlayerCharacter, CreatureContainer {
    protected final WrappedType wrapped;
    protected CommandChainHandler successor;

    protected WrappedINonPlayerCharacter(@NotNull WrappedType toWrap) {
        this.wrapped = toWrap;
        if (toWrap != null) {
            toWrap.setSuccessor(this);
        }
    }

    public final WrappedType unwrap() {
        return this.wrapped;
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        this.wrapped.acceptCreatureVisitor(visitor);
    }

    @Override
    public ICreatureID getCreatureID() {
        return this.wrapped.getCreatureID();
    }

    @Override
    public final Collection<ICreature> getCreatures() {
        if (this.wrapped != null) {
            return Set.of(this);
        }
        return Set.of();
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        if (creature == null || creature.isAlive()) {
            return false;
        }
        if (creature == this || creature == this.wrapped) {
            ICreature.announceDeath(this); // forward
            return true;
        }
        return false;
    }

    @Override
    public final boolean addCreature(ICreature creature) {
        throw new UnsupportedOperationException("Cannot add a creature to an already-wrapped Creature");
    }

    @Override
    public final boolean removeCreature(ICreature creature) {
        throw new UnsupportedOperationException("Cannot remove a creature from a wrapped Creature");
    }

    @Override
    public final Optional<ICreature> removeCreature(String name) {
        throw new UnsupportedOperationException("Cannot remove a creature from a wrapped Creature");
    }

    @Override
    public final boolean addPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot add Player to a wrapped Creature");
    }

    @Override
    public final boolean removePlayer(Player player) {
        throw new UnsupportedOperationException("Cannot remove Player from a wrapped Creature");
    }

    @Override
    public final Optional<Player> removePlayer(UserID id) {
        throw new UnsupportedOperationException("Cannot remove Player from a wrapped Creature");
    }

    @Override
    public final Optional<Player> removePlayer(String name) {
        throw new UnsupportedOperationException("Cannot remove Player from a wrapped Creature");
    }

    @Override
    public SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder().setExaminable(this));
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        return wrapped.equipItem(itemName, slot);
    }

    @Override
    public final void setSuccessor(CommandChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public Inventory getInventory() {
        return wrapped.getInventory();
    }

    @Override
    public boolean unequipItem(EquipmentSlots slot, String weapon) {
        return wrapped.unequipItem(slot, weapon);
    }

    @Override
    public final CommandChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public String printInventory() {
        return wrapped.printInventory();
    }

    @Override
    public Equipable getEquipped(EquipmentSlots slot) {
        return wrapped.getEquipped(slot);
    }

    @Override
    public GameEvent processEffectApplication(CreatureEffect effect) {
        return wrapped.processEffectApplication(effect);
    }

    @Override
    public Collection<IItem> getItems() {
        return wrapped.getItems();
    }

    @Override
    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return wrapped.getEquipmentSlots();
    }

    @Override
    public Optional<IItem> getItem(String name) {
        return wrapped.getItem(name);
    }

    @Override
    public boolean addItem(IItem item) {
        return wrapped.addItem(item);
    }

    @Override
    public boolean hasItem(String name) {
        return wrapped.hasItem(name);
    }

    @Override
    public Optional<IItem> removeItem(String name) {
        return wrapped.removeItem(name);
    }

    @Override
    public boolean removeItem(IItem item) {
        return wrapped.removeItem(item);
    }

    @Override
    public Iterator<? extends IItem> itemIterator() {
        return wrapped.itemIterator();
    }

    @Override
    public Collection<IItem> filterItems(EnumSet<ItemFilters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends AItem> clazz, Boolean isVisible) {
        return wrapped.filterItems(filters, className, objectName, objNameRegexLen, clazz, isVisible);
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        return wrapped.getCommands(ctx);
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        wrapped.log(logLevel, logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        wrapped.log(logLevel, logMessageSupplier);
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        wrapped.log(level, msg, thrown);
    }

    @Override
    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        wrapped.log(level, thrown, msgSupplier);
    }

    @Override
    public Reply handle(CommandContext ctx, Command cmd) {
        this.addSelfToContext(ctx); // just in case
        Reply reply = wrapped.handle(ctx, cmd);
        this.addSelfToContext(ctx);
        return reply;
    }

    @Override
    public GameEvent processEffectRemoval(CreatureEffect effect) {
        return wrapped.processEffectRemoval(effect);
    }

    @Override
    public GameEvent processEffectEvent(CreatureEffect effect, GameEvent event) {
        return wrapped.processEffectEvent(effect, event);
    }

    @Override
    public GameEvent applyEffect(CreatureEffect effect) {
        return wrapped.applyEffect(effect);
    }

    @Override
    public boolean hasItem(String name, Integer minimumLength) {
        return wrapped.hasItem(name, minimumLength);
    }

    @Override
    public boolean hasItem(IItem item) {
        return wrapped.hasItem(item);
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public NavigableSet<CreatureEffect> getEffects() {
        return wrapped.getEffects();
    }

    @Override
    public NavigableSet<CreatureEffect> getMutableEffects() {
        return wrapped.getMutableEffects();
    }

    @Override
    public Reply handleChain(CommandContext ctx, Command cmd) {
        return wrapped.handleChain(ctx, cmd);
    }

    @Override
    public void removeEffectByName(String name) {
        wrapped.removeEffectByName(name);
    }

    @Override
    public boolean hasEffect(String name) {
        return wrapped.hasEffect(name);
    }

    @Override
    public CommandInvoker getController() {
        return wrapped.getController();
    }

    @Override
    public ClientID getClientID() {
        return wrapped.getClientID();
    }

    @Override
    public HealthBuckets getHealthBucket() {
        return wrapped.getHealthBucket();
    }

    @Override
    public boolean isAlive() {
        return wrapped.isAlive();
    }

    @Override
    public void updateHitpoints(int value) {
        wrapped.updateHitpoints(value);
    }

    @Override
    public void updateXp(int value) {
        wrapped.updateXp(value);
    }

    @Override
    public AttributeBlock getAttributes() {
        return wrapped.getAttributes();
    }

    @Override
    public MultiRollResult check(Attributes attribute) {
        return INonPlayerCharacter.super.check(attribute);
    }

    @Override
    public void updateModifier(Attributes modifier, int value) {
        INonPlayerCharacter.super.updateModifier(modifier, value);
    }

    @Override
    public Map<Stats, Integer> getStats() {
        return wrapped.getStats();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public Weapon defaultWeapon() {
        return wrapped.defaultWeapon();
    }

    @Override
    public boolean checkName(String otherName) {
        return INonPlayerCharacter.super.checkName(otherName);
    }

    @Override
    public boolean CheckNameRegex(String possName, Integer minimumLength) {
        return INonPlayerCharacter.super.CheckNameRegex(possName, minimumLength);
    }

    @Override
    public void setConvoTree(ConversationTree tree) {
        wrapped.setConvoTree(tree);
    }

    @Override
    public void setConvoTree(ConversationManager manager, String name) {
        wrapped.setConvoTree(manager, name);
    }

    @Override
    public CreatureFaction getFaction() {
        return wrapped.getFaction();
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        wrapped.setFaction(faction);
    }

    @Override
    public Attributes getHighestAttributeBonus(Set<Attributes> attrs) {
        return INonPlayerCharacter.super.getHighestAttributeBonus(attrs);
    }

    @Override
    public ConversationTree getConvoTree() {
        return wrapped.getConvoTree();
    }

    @Override
    public void restoreFaction() {
        wrapped.restoreFaction();
    }

    @Override
    public HarmMemories getHarmMemories() {
        return wrapped.getHarmMemories();
    }

    @Override
    public Set<EquipmentTypes> getProficiencies() {
        return wrapped.getProficiencies();
    }

    @Override
    public void setController(CommandInvoker cont) {
        wrapped.setController(cont);
    }

    @Override
    public final EnumSet<SubAreaSort> getSubAreaSorts() {
        return wrapped.getSubAreaSorts();
    }

    @Override
    public final boolean addSubArea(SubAreaSort subAreaSort) {
        return wrapped.addSubArea(subAreaSort);
    }

    @Override
    public final boolean removeSubArea(SubAreaSort subAreaSort) {
        return wrapped.removeSubArea(subAreaSort);
    }

    @Override
    public final boolean isInBattle() {
        return INonPlayerCharacter.super.isInBattle();
    }

    @Override
    public Attack attack(Weapon weapon) {
        return INonPlayerCharacter.super.attack(weapon);
    }

    @Override
    public Attack attack(String itemName, String target) {
        return INonPlayerCharacter.super.attack(itemName, target);
    }

    @Override
    public String getCreatureRace() {
        return wrapped.getCreatureRace();
    }

    @Override
    public Vocation getVocation() {
        return wrapped.getVocation();
    }

    @Override
    public void setVocation(Vocation job) {
        wrapped.setVocation(job);
    }

    @Override
    public String printDescription() {
        return wrapped.printDescription();
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = CreatureStatusRequestedEvent.getStatusBuilder().setFromCreature(this);
        }
        seeOutMessage.setExaminable(this);
        for (CreatureEffect effect : this.getEffects()) {
            seeOutMessage.addSeen(SeeCategory.EFFECTS, effect);
        }
        return seeOutMessage.Build();
    }

    @Override
    public String getStartTag() {
        return wrapped.getStartTag();
    }

    @Override
    public String getEndTag() {
        return wrapped.getEndTag();
    }

    @Override
    public String getColorTaggedName() {
        return wrapped.getColorTaggedName();
    }

    @Override
    public int compareTo(ICreature other) {
        return wrapped.compareTo(other);
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return wrapped.getEventProcessorID();
    }

    @Override
    public Collection<GameEventProcessor> getGameEventProcessors() {
        return wrapped.getGameEventProcessors();
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return INonPlayerCharacter.super.getAcceptHook();
    }

    @Override
    public void tick(GameEvent tickEvent) {
        wrapped.tick(tickEvent);
    }

    @Override
    public final void intercept(CommandChainHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    @Override
    public boolean announceDirect(GameEvent gameEvent, Collection<? extends GameEventProcessor> recipients) {
        return wrapped.announceDirect(gameEvent, recipients);
    }

    @Override
    public boolean announceDirect(GameEvent gameEvent, GameEventProcessor... recipients) {
        return wrapped.announceDirect(gameEvent, recipients);
    }

    @Override
    public boolean announce(GameEvent gameEvent, Set<? extends GameEventProcessor> deafened) {
        return wrapped.announce(gameEvent, deafened);
    }

    @Override
    public boolean announce(Builder<?> builder, Set<? extends GameEventProcessor> deafened) {
        return wrapped.announce(builder, deafened);
    }

    @Override
    public boolean announce(GameEvent gameEvent, GameEventProcessor... deafened) {
        return wrapped.announce(gameEvent, deafened);
    }

    @Override
    public boolean announce(Builder<?> builder, GameEventProcessor... deafened) {
        return wrapped.announce(builder, deafened);
    }

    @Override
    public boolean announce(GameEvent gameEvent) {
        return wrapped.announce(gameEvent);
    }

    @Override
    public boolean announce(Builder<?> builder) {
        return wrapped.announce(builder);
    }

    @Override
    public CommandInvoker getInnerCommandInvoker() {
        return wrapped.getInnerCommandInvoker();
    }

    @Override
    public final CommandContext addSelfToContext(CommandContext ctx) {
        ctx.setCreature(this);
        return ctx;
    }

    @Override
    public String getLeaderName() {
        return wrapped.getLeaderName();
    }

    @Override
    public void setLeaderName(String leaderName) {
        wrapped.setLeaderName(leaderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof WrappedINonPlayerCharacter))
            return false;
        WrappedINonPlayerCharacter<?> other = (WrappedINonPlayerCharacter<?>) obj;
        return Objects.equals(wrapped, other.wrapped);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WrappedINonPlayerCharacter [wrapped=").append(wrapped).append("]");
        return builder.toString();
    }

}
