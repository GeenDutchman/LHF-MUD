package com.lhf.messages.events;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

        private String listTargets(String caster, Collection<Taggable> targets) {
            if (targets == null || targets.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder(this.prefix);
            sb.append(caster != null ? caster : "Someone").append(this.infix);
            if (this.forEach) {
                StringJoiner sj = new StringJoiner(this.suffix + " " + sb.toString(), sb.toString(), this.suffix);
                for (Taggable taggable : targets) {
                    sj.add(taggable.getSimpleContent());
                }
                return sj.toString();
            } else {
                StringJoiner sj = new StringJoiner(", ", sb.toString(), this.suffix);
                for (Taggable taggable : targets) {
                    sj.add(taggable.getSimpleContent());
                }
                return sj.toString();
            }

        }

        public Element buildXMLElement(Document nodeGenerator, ICreature caster, Collection<Taggable> targets) {
            if (nodeGenerator == null || caster == null || targets == null || targets.isEmpty()) {
                return null;
            }
            Element myElement = nodeGenerator.createElement("targeting");
            myElement.setAttribute("colored", "false");
            if (this.forEach) {
                for (Taggable taggable : targets) {
                    Element each = nodeGenerator.createElement("singleTarget");
                    each.setAttribute("colored", "false");
                    each.setAttribute("complex", "true");
                    if (this.prefix != null) {
                        each.appendChild(nodeGenerator.createTextNode(this.prefix));
                    }
                    each.appendChild(caster.buildXMLElement(nodeGenerator));
                    if (this.infix != null) {
                        each.appendChild(nodeGenerator.createTextNode(this.infix));
                    }
                    each.appendChild(taggable.buildXMLElement(nodeGenerator));
                    if (this.suffix != null) {
                        each.appendChild(nodeGenerator.createTextNode(this.suffix));
                    }
                    myElement.appendChild(each);
                }
            } else {
                myElement.setAttribute("complex", "true");
                if (this.prefix != null) {
                    myElement.appendChild(nodeGenerator.createTextNode(this.prefix));
                }
                myElement.appendChild(caster.buildXMLElement(nodeGenerator));
                if (this.infix != null) {
                    myElement.appendChild(nodeGenerator.createTextNode(this.infix));
                }
                for (Taggable taggable : targets) {
                    myElement.appendChild(taggable.buildXMLElement(nodeGenerator));
                }
                if (this.suffix != null) {
                    myElement.appendChild(nodeGenerator.createTextNode(this.suffix));
                }
            }
            return myElement;
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
    public Element buildXMLElement(Document nodeGenerator) {
        if (nodeGenerator == null) {
            return null;
        }
        Element myElement = this.produceContentNode(nodeGenerator);
        myElement.setAttribute("complex", "true");
        myElement.appendChild(this.addressCreatureXML(nodeGenerator, caster, true));
        myElement.appendChild(nodeGenerator.createTextNode(" casts "));
        if (this.spellEntry != null) {
            myElement.appendChild(
                    this.spellEntry.buildXMLElement(nodeGenerator).appendChild(nodeGenerator.createTextNode("!")));
        } else {
            myElement.appendChild(nodeGenerator.createTextNode("a spell!"));
        }
        final TargetingStyle style = this.getTargetingStyle();
        final Collection<Taggable> foundTargets = this.getTargets();
        if (foundTargets != null && !foundTargets.isEmpty()) {
            myElement.appendChild(style.buildXMLElement(nodeGenerator, caster, foundTargets));
        }
        if (this.extras != null) {
            Element extraElement = nodeGenerator
                    .createElement(this.caster != null ? this.caster.getTagName() : "extras");
            extraElement.setAttribute("colored", "true");
            extraElement.appendChild(nodeGenerator.createTextNode(this.extras));
            myElement.appendChild(extraElement);
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.addressCreature(caster, true));
        sb.append(" casts ");
        if (this.spellEntry != null) {
            sb.append(this.spellEntry.getName());
        } else {
            sb.append("a spell");
        }
        sb.append("!");
        final TargetingStyle style = this.getTargetingStyle();
        final Collection<Taggable> foundTargets = this.getTargets();
        if (foundTargets != null && !foundTargets.isEmpty()) {
            sb.append("\r\n");
            sb.append(style.listTargets(this.addressCreature(caster, true), foundTargets));
        }
        if (this.extras != null) {
            sb.append("\r\n").append(this.extras);
        }
        return sb.toString();
    }

}
