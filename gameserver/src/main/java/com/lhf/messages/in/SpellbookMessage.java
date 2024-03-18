package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class SpellbookMessage extends CommandAdapter {

    public SpellbookMessage(Command command) {
        super(command);
    }

    public String getSpellName() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    public List<String> getWithFilters() {
        if (!this.getIndirects().containsKey(Prepositions.WITH)) {
            return new ArrayList<>();
        }
        List<String> filters = new ArrayList<>();
        filters.addAll(this.getIndirects().get(Prepositions.WITH));

        return filters;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("SpellName:");
        String thing = this.getSpellName();
        if (thing != null) {
            sj.add(thing);
        } else {
            sj.add("None!");
        }
        return sj.toString();
    }
}
