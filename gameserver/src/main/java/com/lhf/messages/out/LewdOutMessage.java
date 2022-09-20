package com.lhf.messages.out;

import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.lewd.LewdAnswer;
import com.lhf.messages.OutMessageType;

public class LewdOutMessage extends OutMessage {
    public enum LewdOutMessageType {
        NOT_NUDE, NO_BODY, ORGY_UNSUPPORTED, STATUS, DENIED, ACCEPTED, PROPOSED, DUNNIT;
    }

    private final LewdOutMessageType type;
    private final Map<Creature, LewdAnswer> participants;
    private final Creature creature;

    public LewdOutMessage(LewdOutMessageType type, Creature creature) {
        super(OutMessageType.LEWD);
        this.type = type;
        this.participants = null;
        this.creature = creature;
    }

    public LewdOutMessage(LewdOutMessageType type, Creature creature, Map<Creature, LewdAnswer> participants) {
        super(OutMessageType.LEWD);
        this.type = type;
        this.participants = participants;
        this.creature = creature;
    }

    private String statusString() {
        StringJoiner sj = new StringJoiner(", ");
        if (this.participants != null && this.participants.size() > 0) {
            for (Creature creature : this.participants.keySet()) {
                sj.add(creature.getColorTaggedName() + ":"
                        + this.participants.getOrDefault(creature, LewdAnswer.ASKED).name());
            }
            return sj.toString();
        } else {
            return "No one wants to do it right now. ";
        }
    }

    private String participantsString() {
        StringJoiner sj = new StringJoiner(" and ");
        sj.setEmptyValue(" no one ");

        if (this.participants != null && this.participants.size() > 1) {
            for (Creature creature : this.participants.keySet()) {
                if (LewdAnswer.ACCEPTED.equals(this.participants.get(creature))) {
                    sj.add(creature.getColorTaggedName());
                }
            }
        }
        return sj.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (this.type) {
            case DENIED:
                if (this.creature != null) {
                    sb.append(this.creature.getColorTaggedName()).append(" does not wish to do it. ");
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
                            .append(this.participantsString()).append("! \r\n");
                } else {
                    sb.append("There is a proposal to be lewd!\r\n");
                }
                sb.append("You can agree by entering \"lewd\", or you can pass on all lewding by entering \"pass\". ");
                sb.append("If in the lucky circumstance you are in more than one group,")
                        .append(" enter \"lewd\" followed by a comma separated list of who you want to be with! \r\n");
                sb.append(this.statusString());
                break;
            case DUNNIT:
                sb.append("A blur covers ").append(this.participantsString()).append(" as they do it! ");
                break;
            case NOT_NUDE:
                sb.append("Your ").append(EquipmentSlots.ARMOR)
                        .append(" equipment slot must be empty in order to participate! ");
                break;
            case NO_BODY:
                sb.append("You need to have a body in order to participate in that! ");
                break;
            case ORGY_UNSUPPORTED:
                sb.append("You are trying to lewd too many people! Perhaps you need to be more selective? ");
                break;
            case STATUS:
                // fallthrough
            default:
                sb.append(this.statusString());
                break;
        }
        return sb.toString();
    }

    public LewdOutMessageType getType() {
        return type;
    }

    public Map<Creature, LewdAnswer> getParticipants() {
        return participants;
    }

    public Creature getCreature() {
        return creature;
    }

}
