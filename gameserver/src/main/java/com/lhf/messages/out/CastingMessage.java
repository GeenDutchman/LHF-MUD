package com.lhf.messages.out;

import com.lhf.game.creature.ICreature;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.OutMessageType;

public class CastingMessage extends OutMessage {
    private final ICreature caster;
    private final SpellEntry spellEntry;
    private final String castEffects;

    public static class Builder extends OutMessage.Builder<Builder> {
        private ICreature caster;
        private SpellEntry spellEntry;
        private String castEffects;

        public Builder() {
            super(OutMessageType.CASTING);
        }

        public ICreature getCaster() {
            return caster;
        }

        public Builder setCaster(ICreature caster) {
            this.caster = caster;
            return this;
        }

        public SpellEntry getSpellEntry() {
            return spellEntry;
        }

        public Builder setSpellEntry(SpellEntry spellEntry) {
            this.spellEntry = spellEntry;
            return this;
        }

        public String getCastEffects() {
            return castEffects;
        }

        public Builder setCastEffects(String castEffects) {
            this.castEffects = castEffects;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CastingMessage Build() {
            return new CastingMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CastingMessage(Builder builder) {
        super(builder);
        this.caster = builder.getCaster();
        this.spellEntry = builder.getSpellEntry();
        this.castEffects = builder.getCastEffects();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.caster != null) {
            sb.append(this.caster.getColorTaggedName());
        } else {
            sb.append("Someone");
        }
        sb.append(" casts ");
        if (this.spellEntry != null) {
            sb.append(this.spellEntry.getColorTaggedName());
        } else {
            sb.append("a spell");
        }
        sb.append("!");
        if (this.castEffects != null && !this.castEffects.isBlank()) {
            sb.append("\r\n").append(this.castEffects);
        }
        return sb.toString();
    }

    public String getCastEffects() {
        return castEffects;
    }

    public ICreature getCaster() {
        return caster;
    }

    public SpellEntry getSpellEntry() {
        return spellEntry;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
