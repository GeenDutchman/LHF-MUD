package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;

public class ExitMessage extends Command {
    public ExitMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    public String toString() {
        return "Good Bye";
    }

}
