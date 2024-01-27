package com.lhf.game.item.concrete;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.IItem;
import com.lhf.game.item.InteractObject;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
import com.lhf.messages.events.SeeEvent;

public class Dispenser extends InteractObject implements ItemContainer {
    protected final Queue<IItem> itemsToDispense;

    public Dispenser(String name, String description) {
        super(name, description);
        this.itemsToDispense = new ArrayDeque<>();
    }

    public Dispenser(String name, String description, boolean isRepeatable) {
        super(name, description, isRepeatable);
        this.itemsToDispense = new ArrayDeque<>();
    }

    @Override
    public Dispenser makeCopy() {
        Dispenser next = new Dispenser(this.getName(), this.descriptionString, this.isRepeatable());
        ItemContainer.transfer(this, next, null, true);
        return next;
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
        ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setTaggable(this);
        if (this.area == null) {
            builder.setSubType(InteractOutMessageType.CANNOT).setNotBroadcast();
            ICreature.eventAccepter.accept(creature, builder.Build());
            return;
        }
        try {
            final IItem retrieved = this.itemsToDispense.remove();
            this.area.addItem(retrieved);
            builder.setPerformed().setBroacast()
                    .setDescription(String.format("%s was dispensed because of %s.", retrieved.getColorTaggedName(),
                            creature.getColorTaggedName()));
            Area.eventAccepter.accept(this.area, builder.Build());
            this.interactCount++;
        } catch (NoSuchElementException e) {
            builder.setSubType(InteractOutMessageType.USED_UP).setNotBroadcast();
            ICreature.eventAccepter.accept(creature, builder.Build());
        }
    }

    @Override
    public Collection<IItem> getItems() {
        return Collections.unmodifiableCollection(this.itemsToDispense);
    }

    @Override
    public boolean addItem(IItem item) {
        if (item != null) {
            return this.itemsToDispense.add(item);
        }
        return false;
    }

    @Override
    public Optional<IItem> removeItem(String name) {
        for (Iterator<? extends IItem> iterator = this.itemIterator(); iterator.hasNext();) {
            IItem thing = iterator.next();
            if (thing == null) {
                iterator.remove();
                continue;
            }
            if (thing.checkName(name)) {
                iterator.remove();
                return Optional.of(thing);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeItem(IItem item) {
        return this.itemsToDispense.remove(item);
    }

    @Override
    public Iterator<? extends IItem> itemIterator() {
        return this.itemsToDispense.iterator();
    }

    @Override
    public String printDescription() {
        // perhaps other things
        return super.printDescription();
    }

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        return seeOutMessage.Build();
    }
}
