package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.OutMessageType;

public class FleeMessage extends OutMessage {
    private Creature runner;
    private boolean runnerAddressed;
    private MultiRollResult roll;
    private boolean fled;

    public FleeMessage(Creature runner, boolean runnerAddressed, MultiRollResult roll, boolean fled) {
        super(OutMessageType.FLEE);
        this.runner = runner;
        this.runnerAddressed = runnerAddressed;
        this.roll = roll;
        this.fled = fled;
    }

    @Override
    public String toString() {
        if (this.fled) {
            if (this.runnerAddressed) {
                return "You successfully " + this.roll.getColorTaggedName() + " fled the battle!";
            } else {
                return this.runner.getColorTaggedName() + " flees " + this.roll.getColorTaggedName() + " the battle!";
            }
        } else {
            if (this.runnerAddressed) {
                return "You were not " + roll.getColorTaggedName() + " able to flee.";
            } else {
                return this.runner.getColorTaggedName() + " attempted " + roll.getColorTaggedName() + " to flee!";
            }
        }
    }

    public Creature getRunner() {
        return runner;
    }

    public MultiRollResult getRoll() {
        return roll;
    }

    public boolean isFled() {
        return fled;
    }

}
