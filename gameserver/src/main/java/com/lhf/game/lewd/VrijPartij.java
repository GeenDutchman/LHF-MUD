package com.lhf.game.lewd;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.LewdOutMessage.LewdOutMessageType;

class VrijPartij {
    protected Map<Creature, LewdAnswer> party;
    protected StringJoiner sb;

    public VrijPartij(Map<Creature, LewdAnswer> frijPartij) {
        this.party = frijPartij;
        this.sb = new StringJoiner(" ").setEmptyValue("");
    }

    public String getNames() {
        return this.sb.toString();
    }

    public Set<Creature> getParticipants(LewdAnswer answer) {
        if (answer == null) {
            answer = LewdAnswer.ACCEPTED;
        }
        HashSet<Creature> doers = new HashSet<>();
        for (Map.Entry<Creature, LewdAnswer> entry : this.party.entrySet()) {
            if (answer.equals(entry.getValue())) {
                doers.add(entry.getKey());
            }
        }
        return doers;
    }

    public Set<Creature> getParticipants() {
        return this.getParticipants(LewdAnswer.ACCEPTED);
    }

    public VrijPartij addName(String name) {
        this.sb.add(name);
        return this;
    }

    public void messageParticipants(LewdOutMessage lom) {
        if (lom != null) {
            for (Creature participant : party.keySet()) {
                LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
                if (!LewdAnswer.DENIED.equals(answer)) {
                    participant.sendMsg(lom);
                }
            }
        }
    }

    public VrijPartij merge(Map<Creature, LewdAnswer> partij) {
        partij.forEach((key, value) -> this.party.merge(key, value, LewdAnswer::merge));
        return this;
    }

    public boolean match(Map<Creature, LewdAnswer> partij) {
        if (partij == null) {
            return false;
        }
        if (partij.size() == this.party.size() && partij.keySet().containsAll(this.party.keySet())) {
            return true;
        }
        return false;
    }

    public boolean isMember(Creature creature) {
        return this.party.containsKey(creature);
    }

    protected VrijPartij accept(Creature creature) {
        if (this.party.containsKey(creature)) {
            this.party.put(creature, LewdAnswer.ACCEPTED);
        }
        return this;
    }

    public boolean acceptAndCheck(Creature creature) {
        this.accept(creature);
        boolean allDone = true;
        for (Creature participant : party.keySet()) {
            LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
            if (!LewdAnswer.DENIED.equals(answer)) {
                participant.sendMsg(new LewdOutMessage(LewdOutMessageType.ACCEPTED, creature, party));
            }
            if (LewdAnswer.ASKED.equals(answer)) {
                allDone = false;
            }
        }
        if (allDone) {
            LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.DUNNIT, null, party);
            this.messageParticipants(lom);
        }
        return allDone;
    }

    public VrijPartij pass(Creature creature) {
        if (party.containsKey(creature)) {
            party.put(creature, LewdAnswer.DENIED);
            LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.DENIED, creature, party);
            creature.sendMsg(lom);
            this.messageParticipants(lom);
        }
        return this;
    }

    protected void remove(Creature creature) {
        this.party.remove(creature);
    }
}