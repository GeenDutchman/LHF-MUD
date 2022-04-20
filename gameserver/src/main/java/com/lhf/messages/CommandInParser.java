package com.lhf.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lhf.messages.grammar.GrammaredCommandPhrase;
import com.lhf.messages.in.InMessage;

public class CommandInParser {
    public static Command parse(String messageIn) {
        String toParse = messageIn.trim();
        GrammaredCommandPhrase parser = new GrammaredCommandPhrase();

        try {
            Pattern splitter = Pattern.compile("\\w+|[^\\s]");
            Matcher matcher = splitter.matcher(toParse);
            Boolean accepted = true;
            while (matcher.find()) {
                accepted = accepted && parser.parse(matcher.group());
            }
            Command parsed = InMessage.fromCommand(parser.getCommandWord().getCommand(), toParse);
            parsed.setValid(accepted && parser.isValid());
            return parsed;
        } catch (Exception e) {
            return InMessage.fromCommand(CommandMessage.HELP, toParse).setValid(false);
        }
    }

}
