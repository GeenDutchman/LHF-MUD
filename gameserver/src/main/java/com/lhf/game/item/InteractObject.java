package com.lhf.game.item;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;

public class InteractObject extends AItem {
    protected transient Area area;
    // Indicates if the action can be used multiple times
    protected boolean repeatable;
    // Indicates if an interaction has already happened
    protected int interactCount;

    public InteractObject(String name, String description) {
        super(name, description);
        this.repeatable = true;
        this.interactCount = 0;
    }

    public InteractObject(String name, String description, boolean isRepeatable) {
        super(name, description);
        this.repeatable = isRepeatable;
        this.interactCount = 0;
    }

    public InteractObject(InteractObject other) {
        this(other.getName(), other.descriptionString, other.repeatable);
        this.repeatable = other.repeatable;
        this.interactCount = 0;
    }

    @Override
    public InteractObject makeCopy() {
        return new InteractObject(this);
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public void broadcast(ICreature creature, GameEvent event) {
        if (event == null) {
            return;
        }
        if (this.area != null) {
            Area.eventAccepter.accept(this.area, event);
        } else if (creature != null) {
            ICreature.eventAccepter.accept(creature, event);
        }
    }

    public void broadcast(ICreature creature, GameEvent.Builder<?> builder) {
        if (builder == null) {
            return;
        }
        if (this.area != null) {
            this.broadcast(creature, builder.setBroacast().Build());
        } else if (creature != null) {
            this.broadcast(creature, builder.setNotBroadcast().Build());
        }
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    public void doAction(CommandContext ctx) {
        if (ctx == null || ctx.getCreature() == null) {
            return;
        }
        ctx.receive(ItemInteractionEvent.getBuilder().setTaggable(this)
                .setSubType(InteractOutMessageType.NO_METHOD).Build());
        this.interactCount++;
    }

    @Override
    public String printDescription() {
        String otherDescription = super.printDescription();
        if (interactCount > 0) {
            otherDescription += " It looks like it has been interacted with already, it might not work again.";
        }
        return otherDescription;
    }

    public int getInteractCount() {
        return interactCount;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public String getStartTag() {
        return "<interactable>";
    }

    @Override
    public String getEndTag() {
        return "</interactable>";
    }
}
