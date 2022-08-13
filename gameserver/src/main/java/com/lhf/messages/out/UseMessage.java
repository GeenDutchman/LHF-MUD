package com.lhf.messages.out;

import com.lhf.game.item.interfaces.Usable;
import com.lhf.messages.OutMessageType;

public class UseMessage extends OutMessage {
    public enum UseMessageOption {
        OK, USED_UP, NO_USES;
    }

    private UseMessageOption umo;
    private Usable usable;
    private String target;

    public UseMessage(UseMessageOption umo, Usable usable, String target) {
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

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        switch (this.umo) {
            case NO_USES:
                return "You cannot use this " + this.usable.getColorTaggedName() + " like that!";
            case USED_UP:
                return "This " + this.usable.getColorTaggedName() + " has been used up.";
            case OK:
            default:
                return "You used " + this.usable.getColorTaggedName();

        }
    }
}
