package com.lhf.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class PhraseList implements GrammarStateMachine {
    protected List<Phrase> phrases;
    protected Set<String> enders;
    protected Map<String, String> listSeparator;

    public PhraseList(Set<String> listEnders) {
        this.phrases = new ArrayList<>();
        this.listSeparator = new HashMap<>();
        this.listSeparator.put(",", ",");
        this.listSeparator.put("and", "and");
        this.enders = new HashSet<>(listEnders);
        this.enders.addAll(this.listSeparator.keySet());
    }

    public PhraseList(Set<String> listEnders, Set<String> separator) {
        this.phrases = new ArrayList<>();
        this.listSeparator = new HashMap<>();

        if (separator != null && separator.size() > 0 && !separator.contains(" ")) {
            for (String s : separator) {
                this.listSeparator.put(s, s);
            }
        } else {
            this.listSeparator = new HashMap<>();
            this.listSeparator.put(",", ",");
            this.listSeparator.put("and", "and");
        }
        this.enders = new HashSet<>(listEnders);
        this.enders.addAll(this.listSeparator.keySet());
    }

    public Boolean startNextEntry() {
        return this.parse(this.listSeparator.);
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
        if (this.listSeparator.containsKey(token.toLowerCase())) {
            this.phrases.add(new Phrase(this.enders));
            return true;
        }
        // delegation!
        return this.phrases.get(this.phrases.size() - 1).parse(token);
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

    @Override
    public String getResult() {
        StringJoiner sj = new StringJoiner(", ");
        for (Phrase phrase : this.phrases) {
            String result = phrase.getResult();
            if (result.length() > 0) {
                sj.add(result);
            }
        }
        return sj.toString().trim();
    }

}
