package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class CastingMessage extends OutMessage {
    private String spellName;
    private String castEffects;

    public CastingMessage(String spellName, String castEffects) {
        super(OutMessageType.CASTING);
        this.spellName = spellName;
        this.castEffects = castEffects;
    }

    @Override
    public String toString() {
        return this.castEffects;
    }

    public String getSpellName() {
        return spellName;
    }

    public String getCastEffects() {
        return castEffects;
    }

}
