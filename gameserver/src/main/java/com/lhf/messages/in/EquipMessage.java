package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public class EquipMessage extends Command {
    public EquipMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    @Override
    public Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor) {
        return visitor.visit(ctx, this);
    }

    public String getItemName() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    public EquipmentSlots getEquipSlot() {
        return EquipmentSlots.getEquipmentSlot(this.getFirstByPreposition(Prepositions.TO));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("What:");
        if (this.getItemName() != null) {
            sj.add(this.getItemName());
        } else {
            sj.add("equipping nothing!");
        }
        sj.add("To:");
        if (this.getEquipSlot() != null) {
            sj.add(this.getEquipSlot().toString());
        } else {
            sj.add("default slot");
        }
        return sj.toString();
    }
}
