package com.lhf.messages.in;

import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class LewdInMessage extends CommandAdapter {
    public LewdInMessage(Command command) {
        super(command);
    }

    public Set<String> getPartners() {
        if (this.getDirects().size() < 1) {
            return new TreeSet<>();
        }
        Set<String> partners = new TreeSet<>();
        partners.addAll(this.getDirects());
        return partners;
    }

    private String[] split() {
        String[] splitten = this.getIndirects().getOrDefault(Prepositions.USE, "").split(Pattern.quote(", "));
        return splitten;
    }

    private Set<String> makeSet(String[] splitten) {
        Set<String> babies = new TreeSet<>();
        for (String baby : splitten) {
            babies.add(baby);
        }
        return babies;
    }

    public Set<String> getNames() {
        if (this.getIndirects().size() < 1) {
            return new TreeSet<>();
        }
        String[] splitten = this.split();
        return this.makeSet(splitten);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("Partners:");
        Set<String> partners = this.getPartners();
        if (partners != null && partners.size() > 0) {
            sj.add(partners.toString());
        } else {
            sj.add("No partner specified");
        }
        Set<String> names = this.getNames();
        if (names != null && names.size() > 0) {
            sj.add("Baby Names:").add(names.toString());
        }
        return sj.toString();
    }
}
