package com.lhf.game.events.messages.in;

import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class LewdInMessage extends Command {
    LewdInMessage(String payload) {
        super(CommandMessage.LEWD, payload, true);
        this.addPreposition("use");
    }

    @Override
    public Boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        boolean directsValid = this.directs.size() >= 0 && this.directs.size() == this.getPartners().size();
        if (!directsValid) {
            return directsValid;
        }
        if (this.indirects.size() > 0 && !this.indirects.containsKey("use")) {
            return false;
        }
        if (this.indirects.containsKey("use")) {
            String[] splitten = this.split();
            Set<String> babyNames = this.makeSet(splitten);
            return splitten.length == babyNames.size();
        }
        return true;
    }

    public Set<String> getPartners() {
        if (this.directs.size() < 1) {
            return new TreeSet<>();
        }
        Set<String> partners = new TreeSet<>();
        partners.addAll(this.directs);
        return partners;
    }

    private String[] split() {
        String[] splitten = this.indirects.getOrDefault("use", "").split(Pattern.quote(", "));
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
        if (this.indirects.size() < 1) {
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
