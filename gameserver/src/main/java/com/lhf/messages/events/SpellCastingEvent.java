package com.lhf.messages.events;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.GameEventType;

public class SpellCastingEvent extends GameEvent {
    private final ICreature caster;
    private final SpellEntry spellEntry;
    private final Collection<Taggable> targets;
    private final TargetingStyle targetingStyle;
    private final String extras;

    public static class TargetingStyle {
        private final boolean forEach;
        private final String prefix;
        private final String infix;
        private final String suffix;

        public TargetingStyle() {
            this.forEach = false;
            this.prefix = "";
            this.infix = " is targeting ";
            this.suffix = "!";
        }

        public TargetingStyle(boolean forEach, String prefix, String infix, String suffix) {
            this.forEach = forEach;
            this.prefix = prefix;
            this.infix = infix;
            this.suffix = suffix;
        }

        public void buildOutput(OutputBuilder builder, ICreature caster, Collection<Taggable> targets) {
            if (builder == null || caster == null || targets == null || targets.isEmpty()) {
                return;
            }
            if (this.forEach) {
                for (Taggable taggable : targets) {
                    if (this.prefix != null) {
                        builder.appendString(this.prefix);
                    }
                    builder.appendTaggable(caster);
                    if (this.infix != null) {
                        builder.appendString(infix);
                    }
                    builder.appendTaggable(taggable);
                    if (this.suffix != null) {
                        builder.appendString(suffix);
                    }
                }
            } else {
                if (this.prefix != null) {
                    builder.appendString(this.prefix);
                }
                builder.appendTaggable(caster);
                if (this.infix != null) {
                    builder.appendString(infix);
                }
                builder.appendTaggables(targets);
                if (this.suffix != null) {
                    builder.appendString(suffix);
                }
            }
        }

    }

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature caster;
        private SpellEntry spellEntry;
        private Collection<Taggable> targets;
        private TargetingStyle targetingStyle = new TargetingStyle();
        private String extras;

        public Builder() {
            super(GameEventType.CASTING);
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

        public Collection<Taggable> getTargets() {
            return targets;
        }

        public Builder setTargets(Collection<Taggable> targets) {
            this.targets = targets;
            return this;
        }

        public Builder setTargets(Taggable... targets) {
            if (this.targets == null) {
                this.targets = new LinkedHashSet<>();
            }
            Collections.addAll(this.targets, targets);
            return this;
        }

        public Builder setTargets(List<? extends Taggable> targets2) {
            if (this.targets == null) {
                this.targets = new LinkedHashSet<>();
            }
            this.targets.addAll(targets2);
            return this;
        }

        public Builder addTarget(Taggable target) {
            if (target != null) {
                if (this.targets == null) {
                    this.targets = new LinkedHashSet<>();
                }
                this.targets.add(target);
            }
            return this;
        }

        public TargetingStyle getTargetingStyle() {
            return targetingStyle != null ? targetingStyle : new TargetingStyle();
        }

        public Builder setTargetingStyle(TargetingStyle style) {
            this.targetingStyle = style != null ? style : new TargetingStyle();
            return this;
        }

        public Builder defaultTargetingStyle() {
            this.targetingStyle = new TargetingStyle();
            return this;
        }

        public String getExtras() {
            return extras;
        }

        public Builder setExtras(String extras) {
            this.extras = extras;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public SpellCastingEvent Build() {
            return new SpellCastingEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpellCastingEvent(Builder builder) {
        super(builder);
        this.caster = builder.getCaster();
        this.spellEntry = builder.getSpellEntry();
        this.targets = builder.getTargets();
        this.targetingStyle = builder.getTargetingStyle();
        this.extras = builder.getExtras();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public Collection<Taggable> getTargets() {
        return Collections.unmodifiableCollection(targets);
    }

    public final TargetingStyle getTargetingStyle() {
        return targetingStyle != null ? targetingStyle : new TargetingStyle();
    }

    public ICreature getCaster() {
        return caster;
    }

    public SpellEntry getSpellEntry() {
        return spellEntry;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        this.addressCreature(builder, caster);
        builder.appendString("casts");
        if (this.spellEntry != null) {
            builder.appendTaggable(this.spellEntry);
        } else {
            builder.appendString("a spell");
        }
        builder.appendString("!", null, "\r\n");
        final TargetingStyle style = this.getTargetingStyle();
        final Collection<Taggable> foundTargets = this.getTargets();
        if (foundTargets != null && !foundTargets.isEmpty()) {
            style.buildOutput(builder, caster, foundTargets);
        }
        if (this.extras != null) {
            builder.appendString(extras, "\r\n", null);
        }
    }

}
