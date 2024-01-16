package com.lhf.game.item;

import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.Builder;

public class Usable extends Takeable {
    protected final int numCanUseTimes;
    private int useLeftCount;
    protected final CreatureVisitor creatureVisitor;
    protected final ItemVisitor itemVisitor;
    // something for rooms?

    public Usable(String name, CreatureVisitor creatureVisitor) {
        super(name);
        this.numCanUseTimes = 1;
        this.useLeftCount = this.numCanUseTimes;
        this.creatureVisitor = creatureVisitor;
        this.itemVisitor = null;
    }

    /**
     * Create a new Usable object.
     *
     * @param name           The name to give the object
     * @param description    The description for the Usable
     * @param useSoManyTimes if > 0 then can use that many times, if < 0 then has
     *                       infinite uses
     */
    public Usable(String name, String description, int useSoManyTimes, CreatureVisitor creatureVisitor,
            ItemVisitor itemVisitor) {
        super(name, description);
        this.numCanUseTimes = useSoManyTimes;
        this.useLeftCount = this.numCanUseTimes;
        this.creatureVisitor = creatureVisitor;
        this.itemVisitor = itemVisitor;
    }

    protected Usable(Usable other) {
        this(other.getName(), other.descriptionString, other.numCanUseTimes, other.creatureVisitor, other.itemVisitor);
    }

    @Override
    public Usable makeCopy() {
        if (this.numCanUseTimes < 0) {
            return this;
        }
        return new Usable(this);
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
        if (this.creatureVisitor == null || creature == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        if (!hasUsesLeft()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        // TODO: how are we gonna get messages about specific changes from the visitor?
        creature.acceptCreatureVisitor(creatureVisitor);
        ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK));
        return true;
    }

    public boolean useOn(CommandContext ctx, IItem item) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature()).setUsable(this);
        if (this.itemVisitor == null || item == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        if (!hasUsesLeft()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        item.acceptItemVisitor(itemVisitor);
        ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK));
        return true;
    }

    public boolean useOn(CommandContext ctx, Area area) {
        throw new UnsupportedOperationException("TODO: support using on area");
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
    public SeeEvent produceMessage(Builder seeOutMessage) {
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
