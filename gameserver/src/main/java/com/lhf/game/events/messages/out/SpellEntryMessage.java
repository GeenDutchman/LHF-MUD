package com.lhf.game.events.messages.out;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.magic.SpellEntry;

public class SpellEntryMessage extends OutMessage {
    private final NavigableSet<SpellEntry> entries;
    private final boolean cubeHolder;

    public static class Builder extends OutMessage.Builder<Builder> {
        private NavigableSet<SpellEntry> entries = new TreeSet<>();
        private boolean cubeHolder;

        protected Builder() {
            super(OutMessageType.SPELL_ENTRY);
            this.cubeHolder = true;
        }

        public Builder setNotCubeHolder() {
            this.cubeHolder = false;
            return this;
        }

        public Builder setCubeHolder() {
            this.cubeHolder = true;
            return this;
        }

        public boolean isCubeHolder() {
            return this.cubeHolder;
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
            this.entries = entries != null && this.cubeHolder ? entries : new TreeSet<>();
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
        this.cubeHolder = builder.isCubeHolder();
    }

    public NavigableSet<SpellEntry> getEntries() {
        if (!this.cubeHolder) {
            return Collections.emptyNavigableSet();
        }
        return Collections.unmodifiableNavigableSet(this.entries);
    }

    public boolean isCubeHolder() {
        return this.cubeHolder;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n").setEmptyValue("No spells found.");
        for (SpellEntry entry : this.getEntries()) {
            sj.add(entry.getColorTaggedName()).add("\r\n");
            sj.add(entry.printDescription());
        }
        return (this.cubeHolder ? "" : "Only cubeholders can cast spells. ") + sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

}
