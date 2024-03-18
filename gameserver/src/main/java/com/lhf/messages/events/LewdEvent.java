package com.lhf.messages.events;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.creature.ICreature;
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

    private String statusString() {
        StringJoiner sj = new StringJoiner(", ");
        if (this.party != null && this.party.size() > 0) {
            for (ICreature creature : this.party.keySet()) {
                sj.add(creature.getColorTaggedName() + ":"
                        + this.party.getOrDefault(creature, LewdAnswer.ASKED).name());
            }
            return sj.toString();
        } else {
            return "No one wants to do it right now. ";
        }
    }

    private String acceptedNamesString(ICreature skip) {
        StringJoiner sj = new StringJoiner(" and ");
        sj.setEmptyValue(" no one ");

        if (this.party != null && this.party.size() > 1) {
            for (ICreature creature : this.party.keySet()) {
                if (skip != null && creature == skip) {
                    continue;
                }
                if (LewdAnswer.ACCEPTED.equals(this.party.get(creature))) {
                    sj.add(creature.getColorTaggedName());
                }
            }
        }
        return sj.toString();
    }

    private String notDeniedNamesString(ICreature skip) {
        StringJoiner sj = new StringJoiner(" and ");
        sj.setEmptyValue(" no one ");

        if (this.party != null && this.party.size() > 1) {
            for (ICreature creature : this.party.keySet()) {
                if (skip != null && creature == skip) {
                    continue;
                }
                if (!LewdAnswer.DENIED.equals(this.party.get(creature))) {
                    sj.add(creature.getColorTaggedName());
                }
            }
        }
        return sj.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.subType == null) {
            sb.append(this.statusString());
            return sb.toString();
        }
        switch (this.subType) {
            case DENIED:
                if (this.creature != null) {
                    sb.append(this.creature.getColorTaggedName())
                            .append(" does not wish to do it or is wearing ARMOR and cannot participate. ");
                    sb.append("\r\n").append(this.statusString());
                } else {
                    sb.append("No one wants to do it.");
                }
                break;
            case ACCEPTED:
                if (this.creature != null) {
                    sb.append(this.creature.getColorTaggedName()).append(" is excited to join! ");
                    sb.append("\r\n").append(this.statusString());
                } else {
                    sb.append("Let's do it! ");
                }
                break;
            case PROPOSED:
                if (this.creature != null) {
                    sb.append(this.creature.getColorTaggedName()).append(" has asked to lewd ")
                            .append(this.notDeniedNamesString(this.creature)).append("! \r\n");
                } else {
                    sb.append("There is a proposal to be lewd!\r\n");
                }
                sb.append("You can agree by entering \"lewd\", or you can pass on all lewding by entering \"pass\". ");
                sb.append("If in the lucky circumstance you are in more than one group,")
                        .append(" enter \"lewd\" followed by a comma separated list of who you want to be with! \r\n");
                sb.append(this.statusString());
                break;
            case DUNNIT:
                sb.append("A blur covers ").append(this.acceptedNamesString(null)).append(" as they do it! ");
                break;
            case NOT_READY:
                sb.append("Your ").append(EquipmentSlots.ARMOR)
                        .append(" equipment slot must be empty in order to participate and you must be in bed and not in a fight! ");
                sb.append(" The same goes for everyone you invite!");
                break;
            case NO_BODY:
                sb.append("You need to have a body in order to participate in that! ");
                break;
            case ORGY_UNSUPPORTED:
                sb.append("You are trying to lewd too many people! Perhaps you need to be more selective? ");
                break;
            case SOLO_UNSUPPORTED:
                sb.append("Your lewdness is meant to be shared!  Don't go flyin' solo!");
                break;
            case MISSED:
                sb.append("It looks like that lewdness has already been lewded. ");
                break;
            case STATUS:
                // fallthrough
            default:
                sb.append(this.statusString());
                break;
        }
        return sb.toString();
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
    public String print() {
        return this.toString();
    }

}
