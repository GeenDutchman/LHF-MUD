package com.lhf.messages.events;

import java.util.StringJoiner;

import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomEffect;
import com.lhf.messages.GameEventType;

public class RoomAffectedEvent extends GameEvent {
    private final Room room;
    private final RoomEffect effect;
    private final boolean reversed;

    public static class Builder extends GameEvent.Builder<Builder> {
        private Room room;
        private RoomEffect effect;
        private boolean reversed;

        protected Builder() {
            super(GameEventType.ROOM_AFFECTED);
        }

        public Room getRoom() {
            return room;
        }

        public Builder setRoom(Room room) {
            this.room = room;
            return this;
        }

        public RoomEffect getEffect() {
            return effect;
        }

        public Builder setEffect(RoomEffect effect) {
            this.effect = effect;
            return this;
        }

        public boolean isReversed() {
            return reversed;
        }

        public Builder setReversed(boolean reversed) {
            this.reversed = reversed;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public RoomAffectedEvent Build() {
            return new RoomAffectedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public RoomAffectedEvent(Builder builder) {
        super(builder);
        this.room = builder.getRoom();
        this.effect = builder.getEffect();
        this.reversed = builder.isReversed();
    }

    public Room getAffectedRoom() {
        return room;
    }

    public RoomEffect getRoomEffect() {
        return effect;
    }

    public boolean isReversed() {
        return reversed;
    }

    public IMonster getSummonedMonster() {
        return this.effect.getCachedMonster();
    }

    public INonPlayerCharacter getSummonedNPC() {
        return this.effect.getCachedNPC();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.effect.creatureResponsible() != null) {
            sj.add(this.effect.creatureResponsible().getColorTaggedName()).add("used");
            sj.add(this.effect.getGeneratedBy().getColorTaggedName()).add("on");
        } else {
            sj.add(this.effect.getGeneratedBy().getColorTaggedName()).add("affected");
        }
        if (this.room != null) {
            sj.add("the room '").add(this.room.getName() + "'!");
        } else {
            sj.add("a room!");
        }
        sj.add("\r\n");
        if (this.reversed) {
            sj.add("But the effects have EXPIRED, and will now REVERSE!").add("\r\n");
        }

        IMonster summonedMonster = this.effect.getCachedMonster();
        if (summonedMonster != null) {
            sj.add("The monster").add(summonedMonster.getColorTaggedName()).add("was summoned.");
        }
        INonPlayerCharacter summonedNPC = this.effect.getCachedNPC();
        if (summonedNPC != null) {
            sj.add("The NPC").add(summonedNPC.getColorTaggedName()).add("was summoned.");
        }

        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }
}
