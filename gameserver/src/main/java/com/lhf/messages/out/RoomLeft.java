package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Directions;
import com.lhf.messages.OutMessageType;

public class RoomLeft extends OutMessage {
    private final Creature creature;
    private final Directions whichWay;

    public RoomLeft(Creature creature, Directions whichWay) {
        super(OutMessageType.ROOM_EXITED);
        this.creature = creature;
        this.whichWay = whichWay;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(creature.getColorTaggedName()).append(" left the room");
        if (this.whichWay != null) {
            sb.append(" going ").append(this.whichWay.getColorTaggedName());
        }
        sb.append(".");
        return sb.toString();
    }

    public Creature getCreature() {
        return creature;
    }

    public Directions getWhichWay() {
        return whichWay;
    }

}
