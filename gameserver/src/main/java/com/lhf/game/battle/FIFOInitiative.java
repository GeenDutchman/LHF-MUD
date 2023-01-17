package com.lhf.game.battle;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.messages.out.SeeOutMessage;

public class FIFOInitiative implements Initiative {
    private Deque<Creature> participants;

    public FIFOInitiative() {
        this.participants = new ArrayDeque<>();
    }

    @Override
    public String getName() {
        return "Initiative!";
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        if (this.isRunning()) {
            sb.append("The battle is on! ");
        } else {
            sb.append("There is no fight right now. ");
        }
        return sb.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        Collection<Creature> battlers = this.getParticipants();
        SeeOutMessage seeMessage;
        if (battlers == null || battlers.size() == 0) {
            seeMessage = new SeeOutMessage(this);
        } else {
            seeMessage = new SeeOutMessage(this, "Current: " + this.getCurrent().getColorTaggedName());
            for (Creature c : battlers) {
                seeMessage.addSeen("Participants", c);
            }
        }

        return seeMessage;
    }

    @Override
    public Collection<Creature> getParticipants() {
        return this.participants;
    }

    @Override
    public Creature getCurrent() {
        return this.participants.peekFirst();
    }

    @Override
    public boolean addCreature(Creature joiner) {
        if (this.participants.offerLast(joiner)) {
            joiner.setInBattle(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCreature(Creature leaver) {
        if (this.participants.remove(leaver)) {
            leaver.setInBattle(false);
            leaver.tick(TickType.BATTLE);
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        for (Creature creature : this.getParticipants()) {
            this.removeCreature(creature);
        }
    }

    @Override
    public Creature nextTurn() {
        Creature current = this.participants.pollFirst();
        if (current != null) {
            this.participants.offerLast(current);
        }
        return this.getCurrent();
    }

}
