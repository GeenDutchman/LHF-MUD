package com.lhf.game.item.concrete;

import com.lhf.game.Lockable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.InteractObject;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.GameEvent.XMLOutputBuilder;
import com.lhf.messages.events.ItemInteractionEvent;

public class Lever extends InteractObject {
    protected Lockable lockable;

    public Lever(String name, String description) {
        super(name, description);
    }

    public Lever(String name, String description, boolean isRepeatable) {
        super(name, description, isRepeatable);
    }

    public void setLockable(Lockable lockable) {
        this.lockable = lockable;
    }

    @Override
    public Lever makeCopy() {
        Lever switcher = new Lever(this.getName(), this.descriptionString, this.repeatable);
        switcher.setLockable(this.lockable);
        return switcher;
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
        ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setTaggable(this)
                .setInteractor(creature);
        if (this.lockable == null) {
            ICreature.eventAccepter.accept(creature, builder.setNotBroadcast().setXmlCallback(nodeGenerator -> {
                if (nodeGenerator == null) {
                    return;
                }
                XMLOutputBuilder description = nodeGenerator.produceSubBuilder("InteractionDescription");
                description.appendString("The");
                description.appendTaggable(this);
                description.appendString("moves, but it seems too loose, like it is not connected to anything.");
            }).Build());
            return;
        } else {
            if (this.lockable.isUnlocked()) {
                this.lockable.lock();
            } else {
                this.lockable.unlock();
            }
            builder.setPerformed().setXmlCallback(nodeGenerator -> {
                if (nodeGenerator == null) {
                    return;
                }
                XMLOutputBuilder description = nodeGenerator.produceSubBuilder("InteractionDescription");
                description.appendString("A **thunk** is heard, and you are pretty sure something changed because of");
                description.appendTaggable(creature);
            });
            this.broadcast(creature, builder);
        }
        this.interactCount++;
    }
}
