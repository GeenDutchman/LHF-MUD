package com.lhf.messages.out;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.OutMessageType;

public class SpellEntryMessage extends OutMessage {
    private final NavigableSet<SpellEntry> entries;

    public static class Builder extends OutMessage.Builder<Builder> {
        private NavigableSet<SpellEntry> entries = new TreeSet<>();

        protected Builder() {
            super(OutMessageType.SPELL_ENTRY);
        }

        public Builder addEntry(SpellEntry entry) {
            if (this.entries == null) {
                this.entries = new TreeSet<>();
            }
            this.entries.add(entry);
            return this;
        }

        public NavigableSet<SpellEntry> getEntries() {
            return Collections.unmodifiableNavigableSet(entries);
        }

        public Builder setEntries(NavigableSet<SpellEntry> entries) {
            this.entries = entries != null ? entries : new TreeSet<>();
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public SpellEntryMessage Build() {
            return new SpellEntryMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpellEntryMessage(Builder builder) {
        super(builder);
        this.entries = builder.getEntries();
    }

    public NavigableSet<SpellEntry> getEntries() {
        return Collections.unmodifiableNavigableSet(this.entries);
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

    @Override
    public String print() {
        return this.toString();
    }

}
