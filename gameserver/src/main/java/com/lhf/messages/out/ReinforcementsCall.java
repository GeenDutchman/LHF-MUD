package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.OutMessageType;

public class ReinforcementsCall extends OutMessage {
    private Creature caller;
    private boolean callerAddressed;

    public ReinforcementsCall(Creature caller, boolean callerAddressed) {
        super(OutMessageType.REINFORCEMENTS_CALL);
        this.caller = caller;
        this.callerAddressed = callerAddressed;
    }

    @Override
    public String toString() {
        if (this.callerAddressed) {
            if (this.caller.getFaction() == null || CreatureFaction.RENEGADE.equals(this.caller.getFaction())) {
                return "You are a RENEGADE or not a member of a faction.  No one is obligated to help you.";
            }
            return "You call for reinforcements!";
        } else {
            return this.caller.getColorTaggedName() + " calls for reinforcements!";
        }
    }

    public Creature getCaller() {
        return caller;
    }

}
