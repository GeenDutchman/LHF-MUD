package com.lhf.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.lhf.messages.grammar.GrammaredCommandPhrase;
import com.lhf.messages.grammar.Phrase;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.in.CommandAdapter;

public class CommandBuilder {
    public static Command parse(String messageIn) {
        String toParse = messageIn.trim();
        GrammaredCommandPhrase parser = new GrammaredCommandPhrase();

        try {
            Pattern splitter = Pattern.compile("\\w+|[^\\s]|\\s+");
            Matcher matcher = splitter.matcher(toParse);
            Boolean accepted = true;
            while (matcher.find()) {
                accepted = accepted && parser.parse(matcher.group());
            }
            Command parsed = CommandAdapter.fromCommand(parser.getCommandWord().getCommand(), toParse);
            parsed.setValid(accepted && parser.isValid());
            if (parser.getWhat().isPresent()) {
                for (Phrase direct : parser.getWhat().get()) {
                    parsed.addDirect(direct.getResult());
                }
            }
            if (parser.getPreps().isPresent()) {
                PrepositionalPhrases pp = parser.getPreps().get();
                for (String preposition : pp) {
                    parsed.addIndirect(preposition, pp.getPhraseListByPreposition(preposition).getResult()); // TODO:
                                                                                                             // use the
                                                                                                             // list
                }
            }
            return parsed;
        } catch (PatternSyntaxException e) {
            return CommandAdapter.fromCommand(CommandMessage.HELP, toParse).setValid(false);
        } catch (IllegalArgumentException iae) {
            return CommandAdapter.fromCommand(CommandMessage.HELP, toParse).setValid(false);
        } catch (NullPointerException npe) {
            return CommandAdapter.fromCommand(CommandMessage.HELP, toParse).setValid(false);
        }
    }

    public static Command fromCommand(CommandMessage cmdMsg, String arguments) {
        return CommandAdapter.fromCommand(cmdMsg, arguments);
    }

    public static Command addDirect(Command toEdit, String direct) {
        toEdit.addDirect(direct);
        return toEdit;
    }

    public static Command addIndirect(Command toEdit, String preposition, String phrase) {
        toEdit.addIndirect(preposition, phrase);
        return toEdit;
    }

}
