package com.lhf.messages.out;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.OutMessageType;

public class SpellEntryMessage extends OutMessage {
    private NavigableSet<SpellEntry> entries;

    public SpellEntryMessage(SpellEntry entry) {
        super(OutMessageType.SPELL_ENTRY);
        NavigableSet<SpellEntry> protoEntries = new TreeSet<>();
        protoEntries.add(entry);
        this.entries = Collections.unmodifiableNavigableSet(protoEntries);
    }

    public SpellEntryMessage(NavigableSet<SpellEntry> providedEntries) {
        super(OutMessageType.SPELL_ENTRY);
        this.entries = Collections.unmodifiableNavigableSet(providedEntries);
    }

    public NavigableSet<SpellEntry> getEntries() {
        return this.entries;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n").setEmptyValue("No spells found.");
        for (SpellEntry entry : this.getEntries()) {
            sj.add(entry.getColorTaggedName()).add("\r\n");
            sj.add(entry.printDescription());
        }
        return sj.toString();
    }
}
