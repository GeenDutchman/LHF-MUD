package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public class AttackMessage extends Command {

    public AttackMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    @Override
    public Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor) {
        return visitor.visit(ctx, this);
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
