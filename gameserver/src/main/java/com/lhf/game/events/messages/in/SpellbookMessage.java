package com.lhf.game.events.messages.in;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class SpellbookMessage extends Command {

    SpellbookMessage(String arguments) {
        super(CommandMessage.SPELLBOOK, arguments, true);
        this.addPreposition("with");
    }

    public String getTarget() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public String getSpellName() {
        return this.getTarget();
    }

    @Override
    public Boolean isValid() {
        Boolean indirectsvalid = true;
        if (this.indirects.size() >= 1) {
            indirectsvalid = this.indirects.containsKey("with");
        }
        return super.isValid() && this.directs.size() >= 0 && indirectsvalid;
    }

    public List<String> getWithFilters() {
        if (!this.indirects.containsKey("with")) {
            return new ArrayList<>();
        }
        List<String> filters = new ArrayList<>();
        String[] splitten = this.indirects.get("with").split(Pattern.quote(","));
        for (String filter : splitten) {
            filters.add(filter.trim());
        }
        return filters;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("SpellName:");
        String thing = this.getTarget();
        if (thing != null) {
            sj.add(thing);
        } else {
            sj.add("None!");
        }
        return sj.toString();
    }
}
