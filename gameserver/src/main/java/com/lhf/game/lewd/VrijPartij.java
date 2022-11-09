package com.lhf.game.lewd;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lhf.game.creature.Creature;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.LewdOutMessage.LewdOutMessageType;

class VrijPartij {
    protected Map<Creature, LewdAnswer> party;
    protected Set<String> names;

    public VrijPartij(Creature initiator, Set<Creature> partners) {
        this.names = new TreeSet<>();
        this.party = new TreeMap<>();
        if (initiator != null) {
            this.party.put(initiator, LewdAnswer.ACCEPTED);
        }
        if (partners != null) {
            for (Creature partner : partners) {
                this.party.putIfAbsent(partner, LewdAnswer.ASKED);
            }
        }
        LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.PROPOSED, initiator, party);
        this.messageParticipants(lom);
    }

    public VrijPartij addName(String name) {
        if (name != null && name.length() > 0) {
            this.names.add(name);
        }
        return this;
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(this.names);
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

    public boolean match(Set<Creature> partij) {
        if (partij == null) {
            return false;
        }
        if (partij.size() == this.party.size() && partij.containsAll(this.party.keySet())) {
            return true;
        }
        return false;
    }

    public boolean isMember(Creature creature) {
        return this.party.containsKey(creature);
    }

    public LewdAnswer getAnswer(Creature creature) {
        return this.party.get(creature);
    }

    protected VrijPartij accept(Creature creature) {
        if (this.party.containsKey(creature)) {
            this.party.put(creature, LewdAnswer.ACCEPTED);
            LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.ACCEPTED, creature, this.party);
            this.messageParticipants(lom);
        }
        return this;
    }

    public boolean check() {
        boolean allDone = true;
        for (Creature participant : party.keySet()) {
            LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
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

    public boolean acceptAndCheck(Creature creature) {
        this.accept(creature);
        return this.check();
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

    public boolean passAndCheck(Creature creature) {
        this.pass(creature);
        return this.check();
    }

    public int size() {
        if (this.party == null) {
            return -1;
        }
        return this.party.size();
    }

    protected void remove(Creature creature) {
        this.party.remove(creature);
    }
}