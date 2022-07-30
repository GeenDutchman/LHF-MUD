package com.lhf.messages;

import com.lhf.Taggable;

public enum CommandMessage implements Taggable {
    HELP, SAY, SEE, GO, ATTACK, CAST, DROP, EQUIP, UNEQUIP, INTERACT, INVENTORY, TAKE, USE, STATUS,
    PLAYERS, EXIT, CREATE, SHOUT, PASS;

    public static CommandMessage getCommandMessage(String value) {
        for (CommandMessage v : values()) {
            if (v.toString().equalsIgnoreCase(value)) {
                return v;
            }
        }
        return null;
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
    public String getStartTag() {
        return "<command>";
    }

    @Override
    public String getEndTag() {
        return "</command>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.toString() + this.getEndTag();
    }

}
