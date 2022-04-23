package com.lhf.messages.grammar;

import java.util.ArrayList;
import java.util.List;

// package private
class GrammarTestCase {
    public List<String> tokens;
    public List<Boolean> accepted;
    public String result;
    public Boolean valid;

    GrammarTestCase(String result, Boolean valid) {
        this.result = result;
        this.valid = valid;
        this.tokens = new ArrayList<>();
        this.accepted = new ArrayList<>();
    }

    public GrammarTestCase addToken(String token, Boolean acc) {
        this.tokens.add(token);
        this.accepted.add(acc);
        return this;
    }

}
