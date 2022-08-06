package com.lhf.messages.out;

import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.OutMessageType;

public class SpellEntryMessage extends OutMessage {
    private SpellEntry entry;

    public SpellEntryMessage(SpellEntry entry) {
        super(OutMessageType.SPELL_ENTRY);
        this.entry = entry;
    }

    public SpellEntry getEntry() {
        return entry;
    }

    @Override
    public String toString() {
        return this.entry.toString();
    }
}
