package com.lhf.game.battle;

import java.util.Collection;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.server.client.user.UserID;

public class FIFOInitiative implements Initiative {

    public static class Builder implements Initiative.Builder {

        private Builder() {
        }

        public static Builder getInstance() {
            return new Builder();
        }

        @Override
        public Initiative Build() {
            return new FIFOInitiative();
        }

    }

    private Deque<Creature> participants;

    public FIFOInitiative() {
        this.participants = new ConcurrentLinkedDeque<>();
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
        Collection<Creature> battlers = this.getCreatures();
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
    public Collection<Creature> getCreatures() {
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
    public boolean onCreatureDeath(Creature creature) {
        return this.removeCreature(creature);
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Optional<Creature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        Optional<Player> found = this.getPlayer(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Optional<Player> found = this.getPlayer(id);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removeCreature(player);
    }

    @Override
    public void start() {
        Logger.getLogger(this.getClass().getName()).finest(() -> "Starting initiative!");
    }

    @Override
    public void stop() {
        Logger.getLogger(this.getClass().getName()).finest(() -> "Stopping initiative!");
        for (Creature creature : this.getCreatures()) {
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
