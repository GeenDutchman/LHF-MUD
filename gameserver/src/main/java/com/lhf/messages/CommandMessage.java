package com.lhf.messages;

import com.lhf.Taggable;

public enum CommandMessage implements Taggable {
    HELP, SAY, SEE, GO, ATTACK, CAST, DROP, EQUIP, UNEQUIP, INTERACT, INVENTORY, TAKE, USE, STATUS,
    PLAYERS, EXIT, CREATE, SHOUT;

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
