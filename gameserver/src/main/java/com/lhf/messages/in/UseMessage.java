package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public class UseMessage extends Command {
    public UseMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    public String getUsefulItem() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    public String getTarget() {
        return this.getFirstByPreposition(Prepositions.ON);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        String item = this.getUsefulItem();
        sj.add("Item:");
        if (item != null) {
            sj.add(item);
        } else {
            sj.add("Not using anything!");
        }
        String target = this.getTarget();
        if (target != null) {
            sj.add("Targeting:").add(target);
        }
        return sj.toString();
    }

}
