package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.item.interfaces.Usable;
import com.lhf.messages.OutMessageType;

public class UseOutMessage extends OutMessage {
    public enum UseOutMessageOption {
        OK, USED_UP, NO_USES, REQUIRE_EQUIPPED;
    }

    private UseOutMessageOption uomo;
    private Creature itemUser;
    private Usable usable;
    private Taggable target;

    public UseOutMessage(UseOutMessageOption uomo, Creature itemUser, Usable usable, Taggable target) {
        super(OutMessageType.USE);
        this.uomo = uomo;
        this.itemUser = itemUser;
        this.usable = usable;
        this.target = target;
    }

    public UseOutMessageOption getUomo() {
        return uomo;
    }

    public Creature getItemUser() {
        return itemUser;
    }

    public Usable getUsable() {
        return usable;
    }

    public Taggable getTarget() {
        return target;
    }

    @Override
    public String toString() {
        switch (this.uomo) {
            case NO_USES:
                return "You cannot use this " + this.usable.getColorTaggedName() + " like that!";
            case USED_UP:
                return "This " + this.usable.getColorTaggedName() + " has been used up.";
            case REQUIRE_EQUIPPED:
                return "You need to have this " + this.usable.getColorTaggedName() + " equipped in order to use it!";
            case OK:
            default:
                return "You used " + this.usable.getColorTaggedName();

        }
    }
}
