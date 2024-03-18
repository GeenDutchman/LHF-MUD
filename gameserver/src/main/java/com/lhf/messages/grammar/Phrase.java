package com.lhf.messages.grammar;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lhf.messages.GrammarStateMachine;

public class Phrase implements GrammarStateMachine {
    class QuotedPhrase {
        public String opensWith;
        public StringBuilder qPhrase;

        public QuotedPhrase(String opensWith) {
            this.opensWith = opensWith;
            this.qPhrase = new StringBuilder();
            this.qPhrase.append(opensWith);
        }

        public QuotedPhrase add(String token) {
            this.qPhrase.append(token);
            return this;
        }
    }

    StringBuilder phrase;
    Deque<QuotedPhrase> stack;
    Map<String, String> openClosers;
    Set<String> endflags;

    Phrase(Set<String> endflags) {
        this.phrase = new StringBuilder();
        this.endflags = endflags;
        this.openClosers = new HashMap<>();
        this.setOpenClosers();
        this.stack = new ArrayDeque<>();
    }

    private void setOpenClosers() {
        this.openClosers.put("\"", "\"");
        this.openClosers.put("'", "'");
        this.openClosers.put("(", ")");
        this.openClosers.put("[", "]");
        this.openClosers.put("{", "}");
    }

    public boolean opened() {
        return this.stack.size() > 0;
    }

    public Boolean parse(String token) {
        if (token == null) {
            return false;
        }
        if (this.stack.size() > 0) {
            String endQuote = this.openClosers.get(this.stack.peek().opensWith);
            if (token.equals(endQuote)) {
                QuotedPhrase qPhrase = this.stack.pop();
                this.phrase.append(qPhrase.qPhrase.toString()).append(endQuote);
                return true;
            } else {
                this.stack.peek().add(token);
                return true;
            }
        }
        if (this.openClosers.keySet().contains(token)) {
            QuotedPhrase qPhrase = new QuotedPhrase(token);
            this.stack.push(qPhrase);
            return true;
        }
        if (this.stack.size() == 0 && endflags.contains(token.toLowerCase())) {
            return false;
        }
        this.phrase.append(token);
        return true;
    }

    @Override
    public Boolean isValid() {
        return this.stack.size() == 0 && this.phrase.length() > 0;
    }

    @Override
    public String getResult() {
        return this.phrase.toString().trim();
    }

    @Override
    public String toString() {
        return this.getResult();
    }
}