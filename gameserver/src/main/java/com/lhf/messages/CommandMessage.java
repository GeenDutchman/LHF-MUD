package com.lhf.messages;

import com.lhf.Taggable;

public enum CommandMessage implements Taggable {
    HELP, SAY, SEE, GO, ATTACK, CAST, DROP, EQUIP, UNEQUIP, INTERACT, INVENTORY, TAKE, USE, STATUS,
    PLAYERS, EXIT, CREATE, SHOUT;

    public static CommandMessage getCommandMessage(String value) throws IllegalArgumentException {
        for (CommandMessage v : values()) {
            if (v.toString().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Bad value '" + value + "' for " + CommandMessage.class.toString());
    }

    public static Boolean isCommandMessage(String value) {
        for (CommandMessage v : values()) {
            if (v.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStartTagName() {
        return "<command>";
    }

    @Override
    public String getEndTagName() {
        return "</command>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTagName() + this.toString() + this.getEndTagName();
    }

}
