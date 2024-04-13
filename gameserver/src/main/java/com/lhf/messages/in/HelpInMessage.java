package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;

public class HelpInMessage extends Command {

    public HelpInMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Payload:").add(this.getWhole());
        return sj.toString();
    }
}
