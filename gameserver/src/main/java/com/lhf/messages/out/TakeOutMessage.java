package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.item.Item;
import com.lhf.messages.OutMessageType;

public class TakeOutMessage extends OutMessage {
    public enum TakeOutType {
        FOUND_TAKEN, NOT_FOUND, SHORT, INVALID, GREEDY, NOT_TAKEABLE, UNCLEVER;
    }

    private String attemptedName;
    private Item item;
    private TakeOutType type;

    public TakeOutMessage(String attemptedName, TakeOutType type) {
        super(OutMessageType.TAKE);
        this.item = null;
        this.attemptedName = attemptedName;
        this.type = type;
    }

    public TakeOutMessage(String attemptedName, Item item) {
        super(OutMessageType.TAKE);
        this.type = TakeOutType.FOUND_TAKEN;
        this.attemptedName = attemptedName;
        this.item = item;
    }

    public TakeOutMessage(String attemptedName, Item item, TakeOutType type) {
        super(OutMessageType.TAKE);
        this.attemptedName = attemptedName;
        this.item = item;
        this.type = type;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        switch (this.type) {
            case FOUND_TAKEN:
                sj.add(this.item.getColorTaggedName()).add("successfully taken\n");
                return sj.toString();
            case NOT_FOUND:
                sj.add("Could not find that item '").add(this.attemptedName).add("' in this room.\n");
                return sj.toString();
            case SHORT:
                sj.add("You'll need to be more specific than '").add(this.attemptedName).add("'!\n");
                return sj.toString();
            case INVALID:
                sj.add("I don't think '").add(this.attemptedName).add("' is a valid name\n");
                return sj.toString();
            case GREEDY:
                sj.add("Aren't you being a bit greedy there by trying to grab '").add(this.attemptedName)
                        .add("'?\n");
                return sj.toString();
            case NOT_TAKEABLE:
                sj.add(
                        "That's strange--it's stuck in its place. You can't take the ")
                        .add(this.item.getColorTaggedName())
                        .add("\n");
                return sj.toString();
            case UNCLEVER:
                sj.add("Are you trying to be too clever with '").add(this.attemptedName).add("'?\n");
                return sj.toString();

            default:
                sj.add("You tried to take an item.");
                if (this.attemptedName != null) {
                    sj.add("You tried to find it using the name:").add(this.attemptedName).add(".");
                }
                if (this.item != null) {
                    sj.add("You found this item:").add(this.item.getColorTaggedName());
                }
                return sj.toString();
        }
    }

    public String getAttemptedName() {
        return this.attemptedName;
    }

    public Item getItem() {
        return this.item;
    }

    public TakeOutType getType() {
        return this.type;
    }
}
