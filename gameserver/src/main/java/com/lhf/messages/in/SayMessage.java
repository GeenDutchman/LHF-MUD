package com.lhf.messages.in;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.OutputBuilder.OutputSequence;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public class SayMessage extends Command {
    private final OutputSequence sequence;

    public SayMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
        this.sequence = new OutputSequence();
        final List<String> retrieved = this.getDirects();
        if (retrieved != null && retrieved.size() > 0) {
            this.sequence.appendString(retrieved.get(0), null, null);
        }
    }

    @Override
    public Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor) {
        return visitor.visit(ctx, this);
    }

    @Deprecated(forRemoval = false)
    public String getMessage() {
        return this.sequence.printString();
    }

    public OutputSequence getSequence() {
        return OutputSequence.copy(sequence);
    }

    public String getTarget() {
        return this.getFirstByPreposition(Prepositions.TO);

    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("Message:");
        String message = this.getMessage();
        if (message != null) {
            sj.add(message);
        } else {
            sj.add("No message!");
        }
        sj.add("Target:");
        String target = this.getTarget();
        if (target != null) {
            sj.add(target);
        } else {
            sj.add("No recipient");
        }
        return sj.toString();
    }

}
