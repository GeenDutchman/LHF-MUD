package com.lhf.messages.events;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.lhf.OutputBuilder;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.GameEventType;

public class SpellEntryRequestedEvent extends GameEvent {
    private final NavigableSet<SpellEntry> entries;
    private final boolean cubeHolder;

    public static class Builder extends GameEvent.Builder<Builder> {
        private NavigableSet<SpellEntry> entries = new TreeSet<>();
        private boolean cubeHolder;

        protected Builder() {
            super(GameEventType.SPELL_ENTRY);
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
        public SpellEntryRequestedEvent Build() {
            return new SpellEntryRequestedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpellEntryRequestedEvent(Builder builder) {
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (!this.cubeHolder) {
            builder.appendString("Only cubeholders can cast spells.\r\n");
        }
        final NavigableSet<SpellEntry> retrievedEntries = this.getEntries();
        if (retrievedEntries != null && !retrievedEntries.isEmpty()) {
            for (SpellEntry entry : retrievedEntries) {
                OutputBuilder subBuilder = builder.produceSubBuilder(entry.getName());
                subBuilder.appendExaminable(entry);
                subBuilder.appendString("Invocation:").appendString(entry.getInvocation(), null, "\r\n");
            }
        } else {
            builder.appendString("No spells found");
        }
    }

}
