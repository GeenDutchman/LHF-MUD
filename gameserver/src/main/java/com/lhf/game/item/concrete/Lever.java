package com.lhf.game.item.concrete;

import org.w3c.dom.Element;

import com.lhf.game.Lockable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.InteractObject;
import com.lhf.messages.CommandContext;
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
            ICreature.eventAccepter.accept(creature, builder.setNotBroadcast().setXmlCallbackFunction(nodeGenerator -> {
                if (nodeGenerator == null) {
                    return null;
                }
                Element description = nodeGenerator.createElement("InteractionDescription");
                description.appendChild(nodeGenerator.createTextNode("The "));
                description.appendChild(this.buildXMLElement(nodeGenerator));
                description.appendChild(nodeGenerator
                        .createTextNode(" moves, but it seems too loose, like it is not connected to anything."));
                return description;
            }).Build());
            return;
        } else {
            if (this.lockable.isUnlocked()) {
                this.lockable.lock();
            } else {
                this.lockable.unlock();
            }
            builder.setPerformed().setXmlCallbackFunction(nodeGenerator -> {
                if (nodeGenerator == null) {
                    return null;
                }
                Element description = nodeGenerator.createElement("InteractionDescription");
                description.appendChild(nodeGenerator
                        .createTextNode("A **thunk** is heard, and you are pretty sure something changed because of "));
                description.appendChild(creature.buildXMLElement(nodeGenerator));
                return description;
            });
            this.broadcast(creature, builder);
        }
        this.interactCount++;
    }
}
