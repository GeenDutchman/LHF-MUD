package com.lhf.messages.grammar;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.lhf.messages.GrammarStateMachine;

public class PrepositionalPhrases implements GrammarStateMachine, Iterable<Prepositions> {
    protected EnumSet<Prepositions> prepositions;
    protected EnumMap<Prepositions, PhraseList> phraseMap;
    protected Prepositions currentPreposition;
    protected Boolean allowList;

    public PrepositionalPhrases(EnumSet<Prepositions> providedPreps) {
        this.prepositions = providedPreps;
        this.phraseMap = new EnumMap<>(Prepositions.class);
        this.allowList = false;
    }

    public PrepositionalPhrases(EnumSet<Prepositions> providedPreps, Boolean allowList) {
        this.prepositions = providedPreps;
        this.phraseMap = new EnumMap<>(Prepositions.class);
        this.allowList = allowList;
    }

    public Set<Prepositions> getUsedPrepositions() {
        return this.phraseMap.keySet();
    }

    public PhraseList getPhraseListByPreposition(Prepositions preposition) {
        return this.phraseMap.get(preposition);
    }

    public EnumMap<Prepositions, List<String>> getMappedPhraseLists() {
        EnumMap<Prepositions, List<String>> mapped = new EnumMap<>(Prepositions.class);
        for (final Map.Entry<Prepositions, PhraseList> entry : this.phraseMap.entrySet()) {
            final Prepositions key = entry.getKey();
            final PhraseList phraseList = entry.getValue();
            if (key == null || phraseList == null || phraseList.isEmpty()) {
                continue;
            }
            final List<String> stringlist = phraseList.getListResult();
            if (stringlist == null || stringlist.isEmpty()) {
                continue;
            }
            mapped.put(entry.getKey(), stringlist);
        }
        return mapped;
    }

    @Override
    public String getResult() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Prepositions, PhraseList> entry : this.phraseMap.entrySet()) {
            sb.append(entry.getKey().toString().toLowerCase()).append(" ").append(entry.getValue().getResult())
                    .append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public Boolean isValid() {
        if (this.phraseMap.size() == 0) {
            return false;
        }
        for (Map.Entry<Prepositions, PhraseList> entry : this.phraseMap.entrySet()) {
            if (!entry.getValue().isValid()) {
                return false;
            }
            if (!this.allowList && entry.getValue().getPhraseCount() > 1) {
                return false;
            }
        }
        return true;
    }

    private Boolean addPrepPhrase(Prepositions token) {
        if (token == null) {
            return false;
        }
        if (this.prepositions.contains(token)) {
            this.currentPreposition = token;
            if (this.phraseMap.containsKey(this.currentPreposition)) {
                return this.phraseMap.get(this.currentPreposition).startNextEntry() && this.allowList;
            } else {
                this.phraseMap.put(this.currentPreposition, new PhraseList(
                        this.prepositions.stream().map(prep -> prep.name().toLowerCase()).collect(Collectors.toSet())));
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean parse(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Prepositions prepositionToken = Prepositions.getPreposition(token);
        if (currentPreposition == null && prepositionToken == null) {
            return false;
        } else if (currentPreposition == null && prepositionToken != null) {
            return this.addPrepPhrase(prepositionToken);
        }
        Boolean accepted = this.phraseMap.get(this.currentPreposition).parse(token);
        if (!accepted) {
            return this.addPrepPhrase(prepositionToken);
        }
        return accepted;
    }

    @Override
    public Iterator<Prepositions> iterator() {
        return this.phraseMap.keySet().iterator();
    }

}
