package com.lhf.game.events.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

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

    public int getNumTargets() {
        if (this.directs == null) {
            return 0;
        }
        return this.directs.size();
    }

    public List<String> getTargets() {
        if (this.directs == null || this.directs.size() < 1) {
            return null;
        }
        return new ArrayList<>(this.directs);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getGameEventType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Targets:");
        if (this.getTargets() != null) {
            sj.add(this.getTargets().toString());
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
