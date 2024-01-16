package com.lhf.game.item;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.EffectResistance;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.dice.DiceDC;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.RoomEnteredEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
import com.lhf.server.client.CommandInvoker;

public class Trap extends InteractObject implements GameEventProcessor {
    private final GameEventProcessorID id = new GameEventProcessorID();
    protected final Set<CreatureEffectSource> effectSources;
    protected final EnumMap<Attributes, DiceDC> disarmDifficulties;
    protected boolean activated;

    public Trap(String name, String description, boolean isRepeatable, Map<Attributes, DiceDC> difficulties) {
        super(name, description, isRepeatable);
        this.effectSources = new HashSet<>();
        this.disarmDifficulties = new EnumMap<>(Attributes.class);
        this.disarmDifficulties.putAll(difficulties);
        this.activated = true;
    }

    public Trap(Trap other) {
        super(other);
        this.activated = other.activated;
        this.effectSources = new HashSet<>();
        for (final CreatureEffectSource source : other.getEffectSources()) {
            this.addEffect(source);
        }
        this.disarmDifficulties = new EnumMap<>(Attributes.class);
        this.disarmDifficulties.putAll(other.disarmDifficulties);
    }

    public Trap addEffect(CreatureEffectSource effectSource) {
        if (effectSource != null) {
            this.effectSources.add(effectSource);
        }
        return this;
    }

    public Set<CreatureEffectSource> getEffectSources() {
        return Set.copyOf(this.effectSources);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public void doAction(ICreature creature) {
        if (creature == null) {
            return;
        }
        final ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setTaggable(this);
        final Attributes highest = creature.getHighestAttributeBonus(this.disarmDifficulties.keySet());
        if (highest == null || this.disarmDifficulties.isEmpty()) {
            ICreature.eventAccepter.accept(creature,
                    builder.setDescription("There's not much you can do with this.").setNotBroadcast().Build());
            return;
        }
        final MultiRollResult roll = creature.check(highest);
        final DiceDC difficulty = this.disarmDifficulties.get(highest);
        final RollResult difficultyRoll = difficulty.rollDice();
        if (roll.getRoll() < difficultyRoll.getRoll()) {
            builder.setPerformed()
                    .setDescription(String.format("%s failed (%s vs %s) to %s the %s", creature.getColorTaggedName(),
                            roll.getColorTaggedName(), difficulty.getColorTaggedName(),
                            this.isActivated() ? "deactivate" : "activate", this.getColorTaggedName()));
        } else {
            if (this.interactCount > 1 && !this.isRepeatable()) {
                builder.setSubType(InteractOutMessageType.USED_UP).setDescription(String
                        .format("%s is not repeatable and thus cannot be interacted with.", this.getColorTaggedName()));
            } else {
                this.setActivated(!this.isActivated());
                builder.setPerformed()
                        .setDescription(
                                String.format("%s successfully (%s vs %s) %s the %s", creature.getColorTaggedName(),
                                        roll.getColorTaggedName(), difficulty.getColorTaggedName(),
                                        this.isActivated() ? "activated" : "deactivated", this.getColorTaggedName()));
            }
        }
        this.broadcast(creature, builder);
        this.interactCount++;
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null || event.getEventType() != GameEventType.ROOM_ENTERED || !Trap.this.isActivated()) {
                return;
            }
            if (this.interactCount > 1 && !this.isRepeatable()) {
                this.activated = false;
                return;
            }
            RoomEnteredEvent enterEvent = null;
            try {
                enterEvent = (RoomEnteredEvent) event;
            } catch (ClassCastException e) {
                return;
            }
            final CommandInvoker newbie = enterEvent.getNewbie();
            if (newbie == null) {
                return;
            }
            // We should find some way for this to not be instanceof
            if (newbie instanceof ICreature creature) {
                for (final CreatureEffectSource effectSource : Trap.this.getEffectSources()) {
                    if (effectSource == null) {
                        continue;
                    }
                    final CreatureEffect effect = new CreatureEffect(effectSource, creature, Trap.this);
                    EffectResistance resistance = effect.getResistance();
                    MultiRollResult trapResult = null;
                    MultiRollResult creatureResult = null;
                    if (resistance != null) {
                        final Attributes highest = creature.getHighestAttributeBonus(this.disarmDifficulties.keySet());
                        trapResult = resistance.getActorDC() != null
                                ? new MultiRollResult.Builder()
                                        .addRollResults(new DiceDC(resistance.getActorDC()).rollDice()).Build()
                                : new MultiRollResult.Builder().addRollResults(
                                        this.disarmDifficulties.getOrDefault(highest, new DiceDC(12)).rollDice())
                                        .Build();
                        creatureResult = resistance.targetEffort(creature, 0);
                    }
                    if (resistance == null || creatureResult == null
                            || (trapResult != null && (trapResult.getTotal() > creatureResult.getTotal()))) {
                        GameEvent cam = creature.applyEffect(effect);
                        this.broadcast(creature, cam);
                    } else {
                        ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setTaggable(this)
                                .setPerformed()
                                .setDescription(String.format("%s dodged (%s vs %s) an effect from %s",
                                        creature.getColorTaggedName(),
                                        creatureResult != null ? creatureResult.getColorTaggedName() : "effortlessly",
                                        trapResult != null ? trapResult.getColorTaggedName() : "not enough effort",
                                        this.getColorTaggedName()));
                        this.broadcast(creature, builder);
                    }
                }
                this.interactCount++;
            }
            return;

        };
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        if (this.area == null) {
            Logger.getLogger(this.getClass().getName()).log(logLevel, logMessage);
            return;
        }
        this.area.log(logLevel, String.format("%s: %s", this.getName(), logMessage));
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        if (this.area == null) {
            Logger.getLogger(this.getClass().getName()).log(logLevel, logMessageSupplier);
            return;
        }
        this.area.log(logLevel, () -> String.format("%s: %s", this.getName(),
                logMessageSupplier != null ? logMessageSupplier.get() : null));
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.id;
    }

}
