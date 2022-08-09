package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.OutMessageType;

public class CastingMessage extends OutMessage {
    private Creature caster;
    private SpellEntry spellEntry;
    private String castEffects;

    public CastingMessage(Creature caster, SpellEntry spellEntry, String castEffects) {
        super(OutMessageType.CASTING);
        this.caster = caster;
        this.spellEntry = spellEntry;
        this.castEffects = castEffects;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.caster.getColorTaggedName()).append(" casts ").append(this.spellEntry.getColorTaggedName())
                .append("!");
        if (this.castEffects != null && !this.castEffects.isBlank()) {
            sb.append("\r\n").append(this.castEffects);
        }
        return sb.toString();
    }

    public String getCastEffects() {
        return castEffects;
    }

    public Creature getCaster() {
        return caster;
    }

    public SpellEntry getSpellEntry() {
        return spellEntry;
    }

}
