package com.lhf.game.item;

import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessorHub;
import com.lhf.messages.events.BattleRoundEvent;
import com.lhf.messages.events.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.events.SeeEvent;

public class Usable extends Takeable {
    protected final int numCanUseTimes;
    private int useLeftCount;
    protected final Set<CreatureEffectSource> creatureUseEffects;
    // something for items?
    // something for rooms?

    public Usable(String name, Set<CreatureEffectSource> useOnCreatureEffects) {
        super(name);
        this.numCanUseTimes = 1;
        this.useLeftCount = this.numCanUseTimes;
        this.creatureUseEffects = useOnCreatureEffects != null ? useOnCreatureEffects : Set.of();
    }

    /**
     * Create a new Usable object.
     *
     * @param name           The name to give the object
     * @param description    The description for the Usable
     * @param useSoManyTimes if > 0 then can use that many times, if < 0 then has
     *                       infinite uses
     */
    public Usable(String name, String description, int useSoManyTimes, Set<CreatureEffectSource> useOnCreatureEffects) {
        super(name, description);
        this.numCanUseTimes = useSoManyTimes;
        this.useLeftCount = this.numCanUseTimes;
        this.creatureUseEffects = useOnCreatureEffects != null ? useOnCreatureEffects : Set.of();
    }

    protected Usable(Usable other) {
        this(other.getName(), other.descriptionString, other.numCanUseTimes, other.creatureUseEffects);
    }

    @Override
    public Usable makeCopy() {
        if (this.numCanUseTimes < 0) {
            return this;
        }
        return new Usable(this);
    }

    protected void sendNotice(CommandContext ctx, ICreature creature, GameEvent event) {
        if (creature == null || event == null) {
            return;
        }
        GameEventProcessorHub hub = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (hub == null) {
            hub = ctx.getArea();
        }
        if (hub != null) {
            hub.announce(event);
        } else {
            ctx.receive(event);
            if (!creature.equals(ctx.getCreature())) {
                ICreature.eventAccepter.accept(creature, event);
            }
        }
    }

    protected void sendNotice(CommandContext ctx, ICreature creature, GameEvent.Builder<?> eventBuilder) {
        if (creature == null || eventBuilder == null) {
            return;
        }
        GameEventProcessorHub hub = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (hub == null) {
            hub = ctx.getArea();
        }
        if (hub != null) {
            hub.announce(eventBuilder.setBroacast().Build());
        } else {
            ctx.receive(eventBuilder.setNotBroadcast());
            if (!creature.equals(ctx.getCreature())) {
                ICreature.eventAccepter.accept(creature, eventBuilder.setBroacast().Build());
            }
        }
    }

    protected ItemUsedEvent.Builder getCreatureUseBuilder(CommandContext ctx, ICreature target) {
        return ItemUsedEvent.getBuilder().setUsable(this).setSubType(UseOutMessageOption.OK)
                .setItemUser(ctx.getCreature())
                .setMessage(this.creatureUseEffects == null || this.creatureUseEffects.isEmpty()
                        ? "It does nothing."
                        : "Affects try to take hold.")
                .setTarget(target);
    }

    protected ItemUsedEvent.Builder getItemUseBuilder(CommandContext ctx, IItem target) {
        return ItemUsedEvent.getBuilder().setUsable(this).setSubType(UseOutMessageOption.OK)
                .setItemUser(ctx.getCreature())
                .setMessage("It does nothing.")
                .setTarget(target);
    }

    protected void applyCreatureEffects(CommandContext ctx, ICreature creature) {
        if (creature == null) {
            return;
        }
        if (this.creatureUseEffects == null || this.creatureUseEffects.isEmpty()) {
            return;
        }
        for (final CreatureEffectSource source : this.creatureUseEffects) {
            final CreatureEffect effect = new CreatureEffect(source, ctx.getCreature(), this);
            this.sendNotice(ctx, creature, creature.applyEffect(effect));
        }
    }

    public Consumer<ICreature> produceCreatureConsumer(CommandContext ctx) {
        final Usable self = this;
        return new Consumer<ICreature>() {

            @Override
            public void accept(ICreature creature) {
                if (creature == null) {
                    return;
                }
                self.sendNotice(ctx, creature, self.getCreatureUseBuilder(ctx, creature));
                self.applyCreatureEffects(ctx, creature);
            }

        };

    }

    public Consumer<IItem> produceItemConsumer(CommandContext ctx) {
        return null; // we don't have this functionality for most Usables
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    protected int getUseLeftCount() {
        return useLeftCount;
    }

    public int addUses(int uses) {
        if (uses < 0) {
            throw new IllegalArgumentException(String.format("Cannot add negative uses to this %s", this));
        }
        this.useLeftCount += uses;
        if (this.numCanUseTimes > 0 && this.useLeftCount > this.numCanUseTimes) {
            int remainder = this.useLeftCount - this.numCanUseTimes;
            this.useLeftCount = this.numCanUseTimes;
            return remainder;
        }
        return 0;
    }

    public boolean hasUsesLeft() {
        return (numCanUseTimes < 0) || (useLeftCount > 0);
    }

    public boolean useOn(CommandContext ctx, ICreature creature) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature()).setUsable(this);
        if (creature == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        if (!hasUsesLeft()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        final Consumer<ICreature> visitor = this.produceCreatureConsumer(ctx);
        if (visitor == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        if (ctx.getSubAreaForSort(SubAreaSort.BATTLE) != null) {
            SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            if (bm.hasCreature(creature) && !bm.hasCreature(ctx.getCreature())) {
                // give out of turn message
                bm.addCreature(ctx.getCreature());
                ctx.receive(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.REJECTED)
                        .setNotBroadcast().Build());
                return false;
            }
        }
        visitor.accept(creature);
        return true;
    }

    public boolean useOn(CommandContext ctx, IItem item) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature()).setUsable(this);
        if (item == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        if (!hasUsesLeft()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        final Consumer<IItem> visitor = this.produceItemConsumer(ctx);
        if (visitor == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        visitor.accept(item);
        return true;
    }

    public boolean useOn(CommandContext ctx, Area area) {
        throw new UnsupportedOperationException("TODO: support using on area");
    }

    public final Set<CreatureEffectSource> getCreatureUseEffects() {
        return creatureUseEffects != null ? Collections.unmodifiableSet(creatureUseEffects) : Set.of();
    }

    public final boolean isOffensive() {
        for (final CreatureEffectSource source : this.getCreatureUseEffects()) {
            if (source.isOffensive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Uses the item once
     * 
     * @return true if it still can be used, false otherwise
     */
    public boolean useOnce() {
        if (numCanUseTimes > 0) {
            useLeftCount--;
            return useLeftCount > 0;
        }
        return true;
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner("\n", super.printDescription(), "").setEmptyValue("");
        if (this.creatureUseEffects != null || !this.creatureUseEffects.isEmpty()) {
            sj.add("When used on a Creature, it has the following affects:");
            for (final CreatureEffectSource source : this.creatureUseEffects) {
                sj.add(source.printDescription());
            }
        }
        return sj.toString();
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        if (this.numCanUseTimes > 0) {
            seeOutMessage.addExtraInfo(String.format("This has %d uses left.", this.getUseLeftCount()));
        }
        return super.produceMessage(seeOutMessage);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName()).append(" [numCanUseTimes=").append(numCanUseTimes)
                .append(", useLeftCount=").append(useLeftCount)
                .append("]");
        return builder.toString();
    }

}
