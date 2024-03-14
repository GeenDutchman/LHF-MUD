package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class AttackMessage extends CommandAdapter {

    public AttackMessage(Command command) {
        super(command);
    }

    public String getWeapon() {
        return this.getFirstByPreposition(Prepositions.WITH);
    }

    public int getNumTargets() {
        if (this.getDirects() == null) {
            return 0;
        }
        return this.getDirects().size();
    }

    public List<String> getTargets() {
        if (this.getDirects() == null || this.getDirects().size() < 1) {
            return null;
        }
        return new ArrayList<>(this.getDirects());
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
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
