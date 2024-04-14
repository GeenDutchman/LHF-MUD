package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.map.Directions;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;

public class GoMessage extends Command {
    public GoMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    @Override
    public Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor) {
        return visitor.visit(ctx, this);
    }

    public Directions getDirection() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return Directions.getDirections(this.getDirects().get(0));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Direction:").add(this.getDirection().toString());
        return sj.toString();
    }

}
