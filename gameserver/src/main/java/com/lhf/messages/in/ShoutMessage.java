package com.lhf.messages.in;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;

public class ShoutMessage extends Command {
    private final RichOutput sequence;

    public ShoutMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
        RichOutputBuilder builder = new RichOutputBuilder();
        final List<String> retrieved = this.getDirects();
        if (retrieved != null && retrieved.size() > 0) {
            builder.appendString(retrieved.get(0), null, null);
        }
        this.sequence = builder.build();
    }

    private ShoutMessage(RichOutput output, Boolean isValid) {
        super(AMessageType.SHOUT, new StringBuilder("SHOUT \"").append(output.printString()).append("\"").toString(),
                isValid);
        this.sequence = output;
        this.addDirect(output.printString());
    }

    public static ShoutMessage fromOutputBuilder(RichOutput builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Cannot create ShoutMessage from null OutputBuilder");
        }
        return new ShoutMessage(builder, true);
    }

    @Override
    public Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor) {
        return visitor.visit(ctx, this);
    }

    @Deprecated(forRemoval = false)
    public String getMessage() {
        return this.sequence.printString();
    }

    public RichOutput getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        String message = this.getMessage();
        if (message != null) {
            sj.add(message);
        } else {
            sj.add("No message!");
        }
        return sj.toString();
    }

}
