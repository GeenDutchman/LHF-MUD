package com.lhf.messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.lhf.messages.grammar.GrammaredCommandPhrase;
import com.lhf.messages.grammar.Phrase;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public final class Command implements ICommand {
    protected final String whole;
    protected Boolean isValid;
    protected final CommandMessage command;
    protected final List<String> directs;
    protected final EnumMap<Prepositions, String> indirects;
    protected final EnumSet<Prepositions> prepositions;

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
            CommandMessage commandWord = parser.getCommandWord().getCommand();
            if (commandWord == null) {
                Logger.getLogger(Command.class.getName()).log(Level.WARNING, "Bad parsing, converting to help");
                return new Command(CommandMessage.HELP, toParse, false);
            }
            Command parsed = new Command(commandWord, toParse, accepted);
            parsed.setValid(accepted && parser.isValid());
            if (parser.getWhat().isPresent()) {
                for (Phrase direct : parser.getWhat().get()) {
                    parsed.addDirect(direct.getResult());
                }
            }
            if (parser.getPreps().isPresent()) {
                PrepositionalPhrases pp = parser.getPreps().get();
                for (final Prepositions preposition : pp) {
                    // TODO: use the list associated per preposition
                    parsed.addIndirect(preposition, pp.getPhraseListByPreposition(preposition).getResult());
                }
            }
            parsed.setValid(parsed.isValid() && commandWord.checkValidity(parsed));
            return parsed;
        } catch (PatternSyntaxException e) {
            Logger.getLogger(Command.class.getName()).log(Level.WARNING, toParse, e);
            return new Command(CommandMessage.HELP, toParse, false);
        } catch (IllegalArgumentException iae) {
            Logger.getLogger(Command.class.getName()).log(Level.WARNING, toParse, iae);
            return new Command(CommandMessage.HELP, toParse, false);
        } catch (NullPointerException npe) {
            Logger.getLogger(Command.class.getName()).log(Level.WARNING, toParse, npe);
            return new Command(CommandMessage.HELP, toParse, false);
        }
    }

    private Command(CommandMessage command, String whole, Boolean isValid) {
        this.command = command;
        this.whole = whole;
        this.isValid = isValid;
        this.directs = new ArrayList<>();
        this.indirects = new EnumMap<>(Prepositions.class);
        this.prepositions = EnumSet.noneOf(Prepositions.class);
    }

    protected Command addPreposition(Prepositions preposition) {
        this.prepositions.add(preposition);
        return this;
    }

    protected Set<Prepositions> getPrepositions() {
        return Collections.unmodifiableSet(this.prepositions);
    }

    public String getWhole() {
        return this.whole;
    }

    public CommandMessage getType() {
        return this.command;
    }

    public List<String> getDirects() {
        return directs;
    }

    public Boolean isValid() {
        return this.isValid;
    }

    protected Command setValid(Boolean valid) {
        this.isValid = valid;
        return this;
    }

    protected Command addDirect(String direct) {
        this.directs.add(direct);
        return this;
    }

    protected Command addIndirect(Prepositions preposition, String phrase) {
        this.indirects.put(preposition, phrase);
        return this;
    }

    @Deprecated(forRemoval = true)
    public List<String> getWhat() {
        return this.directs;
    }

    public String getByPreposition(Prepositions preposition) {
        return this.indirects.get(preposition);
    }

    public Map<Prepositions, String> getIndirects() {
        return Collections.unmodifiableMap(this.indirects);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((directs == null) ? 0 : directs.hashCode());
        result = prime * result + ((indirects == null) ? 0 : indirects.hashCode());
        result = prime * result + ((isValid == null) ? 0 : isValid.hashCode());
        result = prime * result + ((whole == null) ? 0 : whole.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Command)) {
            return false;
        }
        Command other = (Command) obj;
        if (command != other.command) {
            return false;
        }
        if (isValid() == null) {
            if (other.isValid() != null) {
                return false;
            }
        } else if (!isValid().equals(other.isValid())) {
            return false;
        }
        if (directs == null) {
            if (other.directs != null) {
                return false;
            }
        } else if (!directs.equals(other.directs)) {
            return false;
        }
        if (indirects == null) {
            if (other.indirects != null) {
                return false;
            }
        } else if (!indirects.equals(other.indirects)) {
            return false;
        }
        if (whole == null) {
            if (other.whole != null) {
                return false;
            }
        } else if (!whole.equals(other.whole)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Whole:").add(this.getWhole());
        return sj.toString();
    }

}
