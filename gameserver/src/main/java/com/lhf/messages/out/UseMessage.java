package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.game.item.interfaces.Usable;
import com.lhf.messages.OutMessageType;

public class UseMessage extends OutMessage {
    public enum UseMessageOption {
        OK, USED_UP, NO_USES, REQUIRE_EQUIPPED;
    }

    private UseMessageOption umo;
    private Usable usable;
    private Taggable target;

    public UseMessage(UseMessageOption umo, Usable usable, Taggable target) {
        super(OutMessageType.USE);
        this.umo = umo;
        this.usable = usable;
        this.target = target;
    }

    public UseMessageOption getUmo() {
        return umo;
    }

    public Usable getUsable() {
        return usable;
    }

    public Taggable getTarget() {
        return target;
    }

    @Override
    public String toString() {
        switch (this.umo) {
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
