package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;

public class StatsInMessage extends Command {

    public StatsInMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    @Override
    public Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor) {
        return visitor.visit(ctx, this);
    }

}
