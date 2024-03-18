package com.lhf.messages.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.messages.GrammarStateMachine;

public class PhraseList implements GrammarStateMachine, Iterable<Phrase> {
    protected List<Phrase> phrases;
    protected Set<String> enders;
    protected Set<String> listSeparator;

    public PhraseList(Set<String> listEnders) {
        this.phrases = new ArrayList<>();
        this.listSeparator = new HashSet<>();
        this.listSeparator.add(",");
        this.listSeparator.add("and");
        this.enders = new HashSet<>(listEnders);
        this.enders.addAll(this.listSeparator);
    }

    public PhraseList(Set<String> listEnders, Set<String> separator) {
        this.phrases = new ArrayList<>();
        this.listSeparator = new HashSet<>();

        if (separator != null && separator.size() > 0 && !separator.contains(" ")) {
            this.listSeparator = separator;
        } else {
            this.listSeparator = new HashSet<>();
            this.listSeparator.add(",");
            this.listSeparator.add("and");
        }
        this.enders = new HashSet<>(listEnders);
        this.enders.addAll(this.listSeparator);
    }

    public Boolean startNextEntry() {
        return this.parse((String) this.listSeparator.toArray()[0]);
    }

    public Integer getPhraseCount() {
        return this.phrases.size();
    }

    @Override
    public Boolean parse(String token) {
        if (token == null) {
            return false;
        }
        if (this.phrases.size() == 0) {
            this.phrases.add(new Phrase(this.enders));
        }
        Phrase lastPhrase = this.phrases.get(this.phrases.size() - 1);
        if (this.listSeparator.contains(token.toLowerCase()) && !lastPhrase.opened()) {
            this.phrases.add(new Phrase(this.enders));
            return true;
        }
        // delegation!
        return lastPhrase.parse(token);
    }

    @Override
    public Boolean isValid() {
        if (this.phrases.size() == 0) {
            return false;
        }
        for (Phrase phrase : this.phrases) {
            if (!phrase.isValid()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return this.phrases == null || this.phrases.size() == 0;
    }

    public List<String> getListResult() {
        List<String> result = new ArrayList<>();
        for (final Phrase phrase : this.phrases) {
            if (phrase != null) {
                final String phraseResult = phrase.getResult();
                if (phraseResult == null || phraseResult.isEmpty() || phraseResult.isBlank()) {
                    continue;
                }
                result.add(phraseResult);
            }
        }
        return result;
    }

    @Override
    public String getResult() {
        StringJoiner sj = new StringJoiner(", ");
        for (final String result : this.getListResult()) {
            if (result.length() > 0) {
                sj.add(result);
            }
        }
        return sj.toString().trim();
    }

    @Override
    public String toString() {
        return this.getResult();
    }

    @Override
    public Iterator<Phrase> iterator() {
        return this.phrases.iterator();
    }

}
