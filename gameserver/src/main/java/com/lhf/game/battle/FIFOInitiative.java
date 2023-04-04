package com.lhf.game.battle;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.server.client.user.UserID;

public class FIFOInitiative implements Initiative {

    public static class Builder implements Initiative.Builder {

        protected Set<Creature> creatures;

        private Builder() {
            this.creatures = new HashSet<>();
        }

        public static Builder getInstance() {
            return new Builder();
        }

        @Override
        public Initiative Build() {
            return new FIFOInitiative(this);
        }

        @Override
        public boolean addCreature(Creature joiner) {
            return this.creatures.add(joiner);
        }

    }

    private Deque<Creature> participants;
    private int roundCount;
    private int turnCount;

    public FIFOInitiative(FIFOInitiative.Builder builder) {
        this.participants = new ConcurrentLinkedDeque<>();
        for (Creature included : builder.creatures) {
            this.addCreature(included);
        }
        this.roundCount = -1;
        this.turnCount = -1;
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
            if (this.getRoundCount() > 0) {
                sb.append(String.format(" It is Round: %d. ", this.getRoundCount()));
            }
            if (this.getTurnCount() > 0) {
                sb.append(String.format(" It is the %d turn in the Round. ", this.getTurnCount()));
            }
        } else {
            sb.append("There is no fight right now. ");
        }
        return sb.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        Collection<Creature> battlers = this.getCreatures();
        SeeOutMessage.Builder seeMessage = SeeOutMessage.getBuilder().setExaminable(this);
        if (battlers != null && battlers.size() > 0) {
            seeMessage.addExtraInfo("Current: " + this.getCurrent().getColorTaggedName());
            for (Creature c : battlers) {
                seeMessage.addSeen("Participants", c);
            }
        }

        return seeMessage.Build();
    }

    @Override
    public Collection<Creature> getCreatures() {
        return this.participants;
    }

    @Override
    public synchronized Creature getCurrent() {
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
        this.roundCount = 1;
        this.turnCount = 1;
    }

    @Override
    public void stop() {
        Logger.getLogger(this.getClass().getName()).finest(() -> "Stopping initiative!");
        for (Creature creature : this.getCreatures()) {
            this.removeCreature(creature);
        }
        this.roundCount = -1;
        this.turnCount = -1;
    }

    @Override
    public synchronized Creature nextTurn() {
        Creature current = this.participants.pollFirst();
        if (current != null) {
            this.participants.offerLast(current);
        }
        this.turnCount++;
        if (this.turnCount > this.participants.size()) {
            this.roundCount++;
            this.turnCount = 1;
        }
        return this.getCurrent();
    }

    @Override
    public int getRoundCount() {
        return this.roundCount;
    }

    @Override
    public int getTurnCount() {
        return this.turnCount;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("FIFOInitiative [participants=").append(participants).append(", roundCount=").append(roundCount)
                .append(", turnCount=").append(turnCount).append("]");
        return builder2.toString();
    }

}
