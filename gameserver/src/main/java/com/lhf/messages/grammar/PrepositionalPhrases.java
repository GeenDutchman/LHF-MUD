package com.lhf.messages.grammar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lhf.messages.GrammarStateMachine;

public class PrepositionalPhrases implements GrammarStateMachine {
    protected Set<String> prepositions;
    protected Map<String, PhraseList> phraseMap;
    protected String currentPreposition;
    protected Boolean allowList;

    public PrepositionalPhrases(Set<String> providedPreps) {
        this.prepositions = providedPreps;
        this.phraseMap = new HashMap<>();
        this.allowList = false;
    }

    public PrepositionalPhrases(Set<String> providedPreps, Boolean allowList) {
        this.prepositions = providedPreps;
        this.phraseMap = new HashMap<>();
        this.allowList = allowList;
    }

    public Set<String> getUsedPrepositions() {
        return this.phraseMap.keySet();
    }

    public PhraseList getPhraseListByPreposition(String preposition) {
        return this.phraseMap.get(preposition);
    }

    @Override
    public String getResult() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, PhraseList> entry : this.phraseMap.entrySet()) {
            sb.append(entry.getKey()).append(" ").append(entry.getValue().getResult()).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public Boolean isValid() {
        if (this.phraseMap.size() == 0) {
            return false;
        }
        for (Map.Entry<String, PhraseList> entry : this.phraseMap.entrySet()) {
            if (!entry.getValue().isValid()) {
                return false;
            }
            if (!this.allowList && entry.getValue().getPhraseCount() > 1) {
                return false;
            }
        }
        return true;
    }

    private Boolean addPrepPhrase(String token) {
        if (token == null) {
            return false;
        }
        if (this.prepositions.contains(token.toLowerCase())) {
            this.currentPreposition = token.toLowerCase();
            if (this.phraseMap.containsKey(this.currentPreposition)) {
                return this.phraseMap.get(this.currentPreposition).startNextEntry() && this.allowList;
            } else {
                this.phraseMap.put(this.currentPreposition, new PhraseList(this.prepositions));
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean parse(String token) {
        if (token == null) {
            return false;
        }
        if (currentPreposition == null) {
            return this.addPrepPhrase(token);
        }
        Boolean accepted = this.phraseMap.get(this.currentPreposition).parse(token);
        if (!accepted) {
            return this.addPrepPhrase(token);
        }
        return accepted;
    }

}
