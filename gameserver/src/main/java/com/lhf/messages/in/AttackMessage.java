package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class AttackMessage extends Command {

    AttackMessage(String payload) {
        super(CommandMessage.ATTACK, payload, true);
        this.addPreposition("with");
    }

    @Override
    public Boolean isValid() {
        // have to attack at least one target, and can only attack with at most one
        // weapon
        return super.isValid() && this.directs.size() >= 1 && this.indirects.size() <= 1;
    }

    public String getWeapon() {
        return this.indirects.getOrDefault("with", null);
    }

    public String getTarget() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0); // TODO: can attack multiple based on level
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Target:");
        if (this.getTarget() != null) {
            sj.add(this.getTarget());
        } else {
            sj.add("No target specified!");
        }
        sj.add("Weapon:");
        if (this.getWeapon() != null) {
            sj.add(this.getWeapon());
        } else {
            sj.add("default weapon");
        }
        return sj.toString();
    }
}
