package com.lhf.game.lewd;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lhf.game.creature.ICreature;
import com.lhf.messages.events.LewdEvent;
import com.lhf.messages.events.LewdEvent.LewdOutMessageType;

/**
 * The steps are to set everyone to INCLUDED except
 * the Initiator, who has `ACCEPTED`
 * already.
 * Then the proposal is sent out, and everyone (except the Initiator) is
 * `ASKED`.
 * Is is then on the others to respond with either `ACCEPTED` or `DENIED`.
 * 
 * @see com.lhf.game.lewd.LewdAnswer
 */
public class VrijPartij {
    protected final int hash;
    protected final ICreature initiator;
    protected Map<ICreature, LewdAnswer> party;
    protected Set<String> names;

    public VrijPartij(ICreature initiator, Set<ICreature> partners) {
        this.initiator = initiator;
        this.names = new TreeSet<>();
        this.party = Collections.synchronizedNavigableMap(new TreeMap<>());
        if (initiator != null) {
            this.party.put(initiator, LewdAnswer.ACCEPTED);
        }
        if (partners != null) {
            for (ICreature partner : partners) {
                this.party.putIfAbsent(partner, LewdAnswer.INCLUDED);
            }
        }
        this.hash = Objects.hash(party.keySet());
    }

    public ICreature getInitiator() {
        return this.initiator;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VrijPartij)) {
            return false;
        }
        VrijPartij other = (VrijPartij) obj;
        return Objects.equals(party.keySet(), other.party.keySet());
    }

    public void propose() {
        LewdEvent lom = LewdEvent.getBuilder().setSubType(LewdOutMessageType.PROPOSED).setCreature(initiator)
                .setParty(party).setBroacast().Build();
        this.messageParticipants(lom);
        this.party.replaceAll((creature, answer) -> {
            return LewdAnswer.merge(answer, LewdAnswer.ASKED);
        });
    }

    public VrijPartij addNames(Set<String> babyNames) {
        if (babyNames != null && babyNames.size() > 0) {
            this.names.addAll(babyNames);
        }
        return this;
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

    public Map<ICreature, LewdAnswer> getParty() {
        return Map.copyOf(this.party);
    }

    public synchronized NavigableSet<ICreature> getParticipants(LewdAnswer answer) {
        if (answer == null) {
            answer = LewdAnswer.ACCEPTED;
        }
        TreeSet<ICreature> doers = new TreeSet<>();
        for (Map.Entry<ICreature, LewdAnswer> entry : this.getParty().entrySet()) {
            if (answer.equals(entry.getValue())) {
                doers.add(entry.getKey());
            }
        }
        return doers;
    }

    public synchronized NavigableSet<ICreature> getParticipants() {
        return this.getParticipants(LewdAnswer.ACCEPTED);
    }

    public synchronized void messageParticipants(LewdEvent lom) {
        if (lom != null) {
            for (Map.Entry<ICreature, LewdAnswer> entry : this.getParty().entrySet()) {
                if (!LewdAnswer.DENIED.equals(entry.getValue())) {
                    ICreature.eventAccepter.accept(entry.getKey(), lom);
                }
            }
        }
    }

    public VrijPartij merge(Map<ICreature, LewdAnswer> partij) {
        partij.forEach((key, value) -> this.party.merge(key, value, LewdAnswer::merge));
        return this;
    }

    public boolean match(Set<ICreature> partij) {
        if (partij == null) {
            return false;
        }
        if (partij.size() == this.party.size() && partij.containsAll(this.party.keySet())) {
            return true;
        }
        return false;
    }

    public boolean isMember(ICreature creature) {
        return this.party.containsKey(creature);
    }

    public LewdAnswer getAnswer(ICreature creature) {
        return this.party.get(creature);
    }

    protected synchronized VrijPartij accept(ICreature creature) {
        if (this.party.containsKey(creature)) {
            this.party.put(creature, LewdAnswer.ACCEPTED);
            LewdEvent lom = LewdEvent.getBuilder().setSubType(LewdOutMessageType.ACCEPTED)
                    .setCreature(creature).setParty(party).setBroacast().Build();
            this.messageParticipants(lom);
        }
        return this;
    }

    public boolean check() {
        LewdEvent.Builder lom = LewdEvent.getBuilder().setParty(party).setBroacast();
        boolean allDone = this.getParticipants(LewdAnswer.ASKED).size() == 0
                && this.getParticipants(LewdAnswer.INCLUDED).size() == 0;

        NavigableSet<ICreature> lewdies = this.getParticipants();
        if (allDone && lewdies.size() > 1) {
            this.messageParticipants(lom.setSubType(LewdOutMessageType.DUNNIT).Build());
        } else if (allDone && lewdies.size() > 0) {
            this.messageParticipants(
                    lom.setSubType(LewdOutMessageType.SOLO_UNSUPPORTED).setCreature(lewdies.first()).Build());
        } else if (allDone) {
            this.messageParticipants(lom.setSubType(LewdOutMessageType.DENIED).Build());
        }
        return allDone;
    }

    public boolean acceptAndCheck(ICreature creature) {
        this.accept(creature);
        return this.check();
    }

    public synchronized VrijPartij pass(ICreature creature) {
        if (party.containsKey(creature)) {
            party.put(creature, LewdAnswer.DENIED);
            LewdEvent lom = LewdEvent.getBuilder().setSubType(LewdOutMessageType.DENIED).setCreature(creature)
                    .setParty(party).setBroacast().Build();
            ICreature.eventAccepter.accept(creature, lom);
            this.messageParticipants(lom);
        }
        return this;
    }

    public boolean passAndCheck(ICreature creature) {
        this.pass(creature);
        return this.check();
    }

    public int size() {
        if (this.party == null) {
            return -1;
        }
        return this.party.size();
    }

    public void remove(ICreature creature) {
        this.party.remove(creature);
    }
}