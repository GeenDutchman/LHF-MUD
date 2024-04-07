package com.lhf.messages.events;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.lhf.OutputBuilder;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.lewd.LewdAnswer;
import com.lhf.messages.GameEventType;

public class LewdEvent extends GameEvent {
    public enum LewdOutMessageType {
        NOT_READY, NO_BODY, ORGY_UNSUPPORTED, SOLO_UNSUPPORTED, STATUS, DENIED, ACCEPTED, PROPOSED, DUNNIT, MISSED;
    }

    private final LewdOutMessageType subType;
    private final Map<ICreature, LewdAnswer> party;
    private final ICreature creature;
    private final Set<ICreatureBuildInfo> templates;

    public static class Builder extends GameEvent.Builder<Builder> {
        private LewdOutMessageType subType;
        private Map<ICreature, LewdAnswer> party = Map.of();
        private ICreature creature;
        private Set<ICreatureBuildInfo> templates = Set.of();

        protected Builder() {
            super(GameEventType.LEWD);
        }

        public LewdOutMessageType getSubType() {
            return subType;
        }

        public Builder setSubType(LewdOutMessageType subType) {
            this.subType = subType;
            return this;
        }

        public Map<ICreature, LewdAnswer> getParty() {
            return Collections.unmodifiableMap(party);
        }

        public Builder setParty(Map<ICreature, LewdAnswer> party) {
            this.party = party != null ? party : Map.of();
            return this;
        }

        public ICreature getCreature() {
            return creature;
        }

        public Builder setCreature(ICreature creature) {
            this.creature = creature;
            return this;
        }

        public Set<ICreatureBuildInfo> getTemplates() {
            return Collections.unmodifiableSet(templates);
        }

        public Builder setTemplates(Set<? extends ICreatureBuildInfo> set) {
            if (set != null) {
                this.templates = new LinkedHashSet<>();
                for (ICreatureBuildInfo iCreatureBuildInfo : set) {
                    if (iCreatureBuildInfo == null) {
                        continue;
                    }
                    this.templates.add(iCreatureBuildInfo);
                }
            } else {
                this.templates = Set.of();
            }
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public LewdEvent Build() {
            return new LewdEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public LewdEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.party = builder.getParty();
        this.creature = builder.getCreature();
        this.templates = builder.getTemplates();
    }

    private void buildStatus(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.party != null && this.party.size() > 0) {
            for (ICreature creature : this.party.keySet()) {
                builder.appendTaggable(creature, " ", ":")
                        .appendString(this.party.getOrDefault(creature, LewdAnswer.ASKED).name(), null, "\r\n");
            }
        } else {
            builder.appendString("No one wants to do it right now.");
        }
    }

    private void buildAboutParty(OutputBuilder builder, Predicate<Map.Entry<ICreature, LewdAnswer>> filter) {
        if (builder == null) {
            return;
        }
        if (this.party != null && this.party.size() > 1) {
            builder.appendTaggables(this.party.entrySet().stream()
                    .filter(entry -> entry != null && entry.getKey() != null
                            && (filter != null ? filter.test(entry) : true))
                    .map(entry -> entry.getKey()).toList(), "and", " ", null, "no one");
        } else {
            builder.appendString("no one");
        }
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public LewdOutMessageType getSubType() {
        return subType;
    }

    public Map<ICreature, LewdAnswer> getParticipants() {
        if (party == null) {
            return Map.of();
        }
        return Map.copyOf(party);
    }

    public ICreature getCreature() {
        return creature;
    }

    public Set<ICreatureBuildInfo> getTemplates() {
        return this.templates;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.subType == null) {
            this.buildStatus(builder);
            return;
        }
        switch (this.subType) {
            case DENIED:
                if (this.creature != null) {
                    builder.appendTaggable(this.creature);
                    builder.appendString(" does not wish to do it or is wearing ARMOR and cannot participate. ");
                    this.buildStatus(builder);
                } else {
                    builder.appendString("No one wants to do it.");
                }
                break;
            case ACCEPTED:
                if (this.creature != null) {
                    builder.appendTaggable(this.creature);
                    builder.appendString("is excited to join!");
                    this.buildStatus(builder);
                } else {
                    builder.appendString("Let's do it!");
                }
                break;
            case PROPOSED:
                if (this.creature != null) {
                    builder.appendTaggable(creature);
                    builder.appendString("has asked to lewd");
                    this.buildAboutParty(builder, entry -> !LewdAnswer.DENIED.equals(entry.getValue()));
                    builder.appendString("!", null, "\r\n");
                } else {
                    builder.appendString("There is a proposal to be lewd!\r\n");
                }
                builder.appendString(
                        "You can agree by entering \"lewd\", or you can pass on all lewding by entering \"pass\". ");
                builder.appendString("If in the lucky circumstance you are in more than one group,")
                        .appendString(
                                " enter \"lewd\" followed by a comma separated list of who you want to be with! \r\n");
                this.buildStatus(builder);
                break;
            case DUNNIT:
                builder.appendString("A blur covers");
                this.buildAboutParty(builder, entry -> LewdAnswer.ACCEPTED.equals(entry.getValue()));
                builder.appendString("as they do it!");
                break;
            case NOT_READY:
                builder.appendString("Your").appendTaggable(EquipmentSlots.ARMOR).appendString(
                        " equipment slot must be empty in order to participate and you must be in bed and not in a fight! ");
                builder.appendString(" The same goes for everyone you invite!");
                break;
            case NO_BODY:
                builder.appendString("You need to have a body in order to participate in that! ");
                break;
            case ORGY_UNSUPPORTED:
                builder.appendString("You are trying to lewd too many people! Perhaps you need to be more selective? ");
                break;
            case SOLO_UNSUPPORTED:
                builder.appendString("Your lewdness is meant to be shared!  Don't go flyin' solo!");
                break;
            case MISSED:
                builder.appendString("It looks like that lewdness has already been lewded. ");
                break;
            case STATUS:
                // fallthrough
            default:
                this.buildStatus(builder);
                break;
        }
    }

}
