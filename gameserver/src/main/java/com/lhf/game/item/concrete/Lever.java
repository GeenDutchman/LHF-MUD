package com.lhf.game.item.concrete;

import com.lhf.game.Lockable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.InteractObject;
import com.lhf.game.map.Area;
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
    public void doAction(ICreature creature) {
        if (creature == null) {
            return;
        }
        ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setTaggable(this);
        if (this.lockable == null) {
            ICreature.eventAccepter
                    .accept(creature,
                            builder.setNotBroadcast().setDescription(String.format(
                                    "The %s moves, but it seems too loose, like it is not connected to anything.",
                                    this.getColorTaggedName())).Build());
            return;
        } else {
            if (this.lockable.isUnlocked()) {
                this.lockable.lock();
            } else {
                this.lockable.unlock();
            }
            builder.setPerformed()
                    .setDescription(String.format(
                            "A **thunk** is heard, and you are pretty sure something changed because of %s.",
                            creature.getColorTaggedName()));
            if (this.area != null) {
                Area.eventAccepter.accept(this.area, builder.setNotBroadcast().Build());
            } else {
                ICreature.eventAccepter.accept(creature, builder.setBroacast().Build());
            }
        }
        this.interactCount++;
    }
}
