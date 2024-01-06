package com.lhf.messages.grammar;

import java.util.EnumSet;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.lhf.messages.GrammarStateMachine;

public class GrammaredCommandPhrase implements GrammarStateMachine {
    protected CommandWord commandWord;
    protected Optional<PhraseList> optWhat;
    protected Optional<PrepositionalPhrases> optPreps;
    protected Boolean invalidated;
    protected EnumSet<Prepositions> prepositions;

    public GrammaredCommandPhrase() {

        this.prepositions = Prepositions.asEnumSet();

        this.commandWord = new CommandWord();
        this.optWhat = Optional.empty();
        this.optPreps = Optional.empty();
        this.invalidated = false;

    }

    public GrammaredCommandPhrase(EnumSet<Prepositions> prepositions) {
        if (prepositions != null) {
            this.prepositions = prepositions;
        } else {
            this.prepositions = Prepositions.asEnumSet();
        }

        this.commandWord = new CommandWord();
        this.optWhat = Optional.empty();
        this.optPreps = Optional.empty();
        this.invalidated = false;

    }

    public CommandWord getCommandWord() {
        return this.commandWord;
    }

    public Optional<PhraseList> getWhat() {
        return this.optWhat;
    }

    public Optional<PrepositionalPhrases> getPreps() {
        return this.optPreps;
    }

    @Override
    public Boolean parse(String token) {
        Boolean accepted = false;

        if (!this.commandWord.isValid()) { // chew through 'em
            accepted = this.commandWord.parse(token);
            if (!accepted) {
                this.invalidated = true;
            }
            return accepted;
        }

        if (this.optWhat.isEmpty() && token.isBlank()) {
            // we can skip the post command spaces
            return true;
        }

        if (this.optWhat.isEmpty()) {
            // this should only happen on the second token or later
            this.optWhat = Optional.of(new PhraseList(
                    this.prepositions.stream().map(prep -> prep.name().toLowerCase()).collect(Collectors.toSet())));
            accepted = this.optWhat.get().parse(token);
            if (!accepted) {
                this.invalidated = true; // e.g. if we get a preposition right off
            }
            return accepted;
        }

        if (this.optWhat.isPresent() && this.optPreps.isEmpty()) {
            accepted = this.optWhat.get().parse(token);
            if (accepted) {
                return accepted;
            }
            // failed, make preps, probably was a preposition
            this.optPreps = Optional.of(new PrepositionalPhrases(this.prepositions));
        }

        if (this.optPreps.isPresent()) {
            return this.optPreps.get().parse(token);
        }

        return false;
    }

    @Override
    public Boolean isValid() {
        if (this.invalidated) {
            return false;
        }
        if (!this.commandWord.isValid()) {
            return false;
        }
        if (this.optWhat.isPresent() && !this.optWhat.get().isValid()) {
            return false;
        }
        if (this.optPreps.isPresent() && !this.optPreps.get().isValid()) {
            return false;
        }
        return true;
    }

    @Override
    public String getResult() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.commandWord.getResult());
        if (this.optWhat.isPresent()) {
            sj.add(this.optWhat.get().getResult());
        }
        if (this.optPreps.isPresent()) {
            sj.add(this.optPreps.get().getResult());
        }
        return sj.toString();
    }

}
