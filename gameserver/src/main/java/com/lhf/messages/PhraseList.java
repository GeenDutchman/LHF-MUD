package com.lhf.messages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class PhraseList implements GrammarStateMachine {
    protected List<Phrase> phrases;
    protected Set<String> enders;

    public PhraseList(Set<String> listEnders) {
        this.phrases = new ArrayList<>();
        this.enders = new HashSet<>(listEnders);
        this.enders.add(",");
    }

    @Override
    public Boolean parse(String token) {
        if (this.phrases.size() == 0) {
            this.phrases.add(new Phrase(this.enders));
        }
        if (token.equals(",")) {
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
