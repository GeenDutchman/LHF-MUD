package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;

public class JoinBattleMessage extends OutMessage {
    private Creature joiner;
    private boolean ongoing;
    private boolean addressJoiner;

    public JoinBattleMessage(Creature joiner, boolean ongoing, boolean addressJoiner) {
        this.joiner = joiner;
        this.ongoing = ongoing;
        this.addressJoiner = addressJoiner;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.addressJoiner) {
            sj.add("You have");
        } else {
            sj.add(this.joiner.getColorTaggedName()).add("has");
        }
        sj.add("joined the");
        if (this.ongoing) {
            sj.add("ongoing");
        }
        sj.add("battle!");
        return sj.toString();
    }

    public Creature getJoiner() {
        return joiner;
    }

    public boolean isOngoing() {
        return ongoing;
    }

}
