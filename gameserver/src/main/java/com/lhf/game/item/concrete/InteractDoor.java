package com.lhf.game.item.concrete;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import com.lhf.game.Lockable;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.ItemVisitor;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.TickEvent;

public final class InteractDoor extends InteractObject implements Lockable {
    private final String secondLocality;
    private final String secondName;
    protected transient Area secondArea;
    private final String preventJourneyFromName;
    private AtomicBoolean enabled;

    public InteractDoor(String name, String description, String secondLocality, String secondName) {
        super(name, description);
        this.enabled = new AtomicBoolean(true);
        this.preventJourneyFromName = null;
        this.secondLocality = secondLocality;
        this.secondName = secondName;
    }

    public InteractDoor(String name, String description, boolean isRepeatable, String secondLocality,
            String secondName) {
        super(name, description, isRepeatable);
        this.enabled = new AtomicBoolean(true);
        this.secondLocality = secondLocality;
        this.secondName = secondName;
        this.preventJourneyFromName = null;
    }

    public InteractDoor(String name, String description, boolean isRepeatable, String secondLocality, String secondName,
            String preventJourneyFromName, boolean enabled) {
        super(name, description, isRepeatable);
        this.secondLocality = secondLocality;
        this.secondName = secondName;
        this.preventJourneyFromName = preventJourneyFromName;
        this.enabled = new AtomicBoolean(enabled);
    }

    public InteractDoor(InteractDoor other) {
        super(other);
        this.enabled = new AtomicBoolean(other.enabled.get());
        this.secondLocality = other.secondLocality;
        this.secondName = other.secondName;
        this.preventJourneyFromName = other.preventJourneyFromName;
    }

    @Override
    public InteractObject makeCopy() {
        return new InteractDoor(this);
    }

    @Override
    public synchronized void setArea(Area area) {
        if (area == null) {
            this.area = null;
            this.secondArea = null;
        } else if (this.area == null || this.area.equals(area)) {
            this.area = area;
        } else if (this.secondArea == null) {
            this.secondArea = area;
        } else {
            throw new IllegalStateException(
                    String.format("First and second areas (%s %s) are already set, cannot set to %s", this.area,
                            this.secondArea, area));
        }
    }

    public synchronized void setSecondArea(Area area) {
        this.secondArea = area;
    }

    public Area getSecondArea() {
        return secondArea;
    }

    public String getSecondLocality() {
        return secondLocality;
    }

    public String getSecondName() {
        return secondName;
    }

    public void populateExternalReference(BiFunction<String, String, Area> populator) {
        if (populator != null && this.secondLocality != null) {
            this.secondArea = populator.apply(secondLocality, secondName);
        }
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void doAction(CommandContext ctx) {
        if (ctx == null) {
            return;
        }
        final ICreature creature = ctx.getCreature();
        if (creature == null) {
            return;
        }

        if (this.area == null || this.secondArea == null) {
            ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.NO_METHOD)
                    .Build());
            return;
        }
        final Area ctxArea = ctx.getArea();
        if (ctxArea == null || !(this.area.equals(ctxArea) || this.secondArea.equals(ctxArea))) {
            ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.ERROR)
                    .Build());
            return;
        }

        if (ctxArea.getName().equals(this.preventJourneyFromName) && !this.isAuthorized(creature)) {
            ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.CANNOT)
                    .Build());
            return;
        } else if (!((this.isUnlocked()) || this.isAuthorized(creature))) {
            ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.CANNOT)
                    .Build());
            return;
        }

        if (this.area.equals(ctxArea) && this.area.removeCreature(creature)) {
            this.area.announce(RoomExitedEvent.getBuilder().setLeaveTaker(creature).setBecauseOf(this).Build());
            ICreature.eventAccepter.accept(ctx.getCreature(),
                    TickEvent.getBuilder().setTickType(TickType.ROOM).Build());
            this.secondArea.addCreature(creature);
            return;
        } else if (this.secondArea.equals(ctxArea) && this.secondArea.removeCreature(creature)) {
            this.secondArea.announce(RoomExitedEvent.getBuilder().setLeaveTaker(creature).setBecauseOf(this).Build());
            ICreature.eventAccepter.accept(ctx.getCreature(),
                    TickEvent.getBuilder().setTickType(TickType.ROOM).Build());
            this.area.addCreature(creature);
            return;
        } else {
            ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(this).setSubType(InteractOutMessageType.CANNOT)
                    .Build());
            return;
        }
    }

    @Override
    public UUID getLockUUID() {
        return this.getItemID().getId();
    }

    @Override
    public boolean isUnlocked() {
        return this.enabled.get();
    }

    @Override
    public void unlock() {
        this.enabled.set(true);
    }

    @Override
    public void lock() {
        this.enabled.set(false);
    }

}
