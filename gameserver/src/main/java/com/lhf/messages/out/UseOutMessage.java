package com.lhf.messages.out;

import java.util.StringJoiner;

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
    private String message;

    public UseOutMessage(UseOutMessageOption uomo, Creature itemUser, Usable usable, Taggable target) {
        super(OutMessageType.USE);
        this.uomo = uomo;
        this.itemUser = itemUser;
        this.usable = usable;
        this.target = target;
        this.message = null;
    }

    public UseOutMessage(UseOutMessageOption uomo, Creature itemUser, Usable usable, Taggable target, String message) {
        super(OutMessageType.USE);
        this.uomo = uomo;
        this.itemUser = itemUser;
        this.usable = usable;
        this.target = target;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        switch (this.uomo) {
            case NO_USES:
                sj.add("You cannot use this").add(this.usable.getColorTaggedName()).add("like that!");
            case USED_UP:
                sj.add("This").add(this.usable.getColorTaggedName()).add("has been used up.");
            case REQUIRE_EQUIPPED:
                sj.add("You need to have this").add(this.usable.getColorTaggedName())
                        .add("equipped in order to use it!");
            case OK:
            default:
                sj.add("You used").add(this.usable.getColorTaggedName()).add(".");
        }
        if (this.message != null && !this.message.isBlank()) {
            sj.add(this.getMessage());
        }
        return sj.toString();
    }
}
