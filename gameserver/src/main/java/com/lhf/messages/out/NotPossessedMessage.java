package com.lhf.messages.out;

import com.lhf.Taggable;

public class NotPossessedMessage extends OutMessage {
    private String type;
    private String itemName;
    private Taggable found;

    public NotPossessedMessage(String type, String itemName) {
        this.type = type;
        this.itemName = itemName;
    }

    public NotPossessedMessage(String type, String itemName, Taggable found) {
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
