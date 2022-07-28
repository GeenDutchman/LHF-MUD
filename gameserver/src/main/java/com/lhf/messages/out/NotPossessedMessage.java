package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.messages.OutMessageType;

public class NotPossessedMessage extends OutMessage {
    private String type;
    private String itemName;
    private Taggable found;

    public NotPossessedMessage(String type, String itemName) {
        super(OutMessageType.NOT_POSSESSED);
        this.type = type;
        this.itemName = itemName;
    }

    public NotPossessedMessage(String type, String itemName, Taggable found) {
        super(OutMessageType.NOT_POSSESSED);
        this.type = type;
        this.itemName = itemName;
        this.found = found;
    }

    @Override
    public String toString() {
        if (this.found == null) {
            return "You do not have that " + this.type.toString() + " named '" + this.itemName.toString() + "'";
        }
        return this.found.getColorTaggedName() + " is not a " + this.type.toString();
    }
}
