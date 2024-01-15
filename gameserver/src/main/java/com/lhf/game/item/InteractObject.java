package com.lhf.game.item;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;

public class InteractObject extends Item {
    protected transient Area area;
    // Indicates if the action can be used multiple times
    protected boolean repeatable;
    // Indicates if an interaction has already happened
    protected int interactCount;

    public InteractObject(String name, boolean isVisible, boolean isRepeatable, String description) {
        super(name, isVisible, description);
        this.repeatable = isRepeatable;
        this.interactCount = 0;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @Override
    public InteractObject makeCopy() {
        return new InteractObject(this.getName(), this.checkVisibility(), repeatable, descriptionString);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    public void doAction(ICreature creature) {
        if (creature == null) {
            return;
        }
        ICreature.eventAccepter.accept(creature, ItemInteractionEvent.getBuilder().setTaggable(this)
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
