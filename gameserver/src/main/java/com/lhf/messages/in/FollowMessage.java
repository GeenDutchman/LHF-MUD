package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public class FollowMessage extends Command {
    private static String AS_OVERRIDE = "override";
    private static String USE_NULL = "null";

    public FollowMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    public String getPersonToFollow() {
        return USE_NULL.equalsIgnoreCase(this.getFirstByPreposition(Prepositions.USE)) ? null
                : this.getDirects().get(0);
    }

    public Boolean isOverride() {
        return this.getIndirects().size() == 1
                && AS_OVERRIDE.equalsIgnoreCase(this.getFirstByPreposition(Prepositions.AS));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ").add(super.toString());
        sj.add("Override:").add(this.isOverride().toString());
        sj.add("New Leader:").add(this.getPersonToFollow());
        return sj.toString();
    }
}
