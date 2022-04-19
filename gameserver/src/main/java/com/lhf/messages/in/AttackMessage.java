package com.lhf.messages.in;

import java.util.HashSet;
import java.util.Set;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class AttackMessage extends Command {
    private String weaponName = "";
    private String targetName = "";

    AttackMessage(String payload) {
        super(CommandMessage.ATTACK, payload, true);
        this.addPreposition("with");
    }

    public String getWeapon() {
        return weaponName;
    }

    public String getTarget() {
        return targetName;
    }

    @Override
    public String toString() {
        return "Attacking " + this.targetName + " with " + this.weaponName;
    }
}
