package com.lhf.messages.events;

import com.lhf.OutputBuilder;
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.effect.creatureResponsible() != null) {
            builder.appendTaggable(this.effect.creatureResponsible()).appendString("used");
            builder.appendTaggable(this.effect.getGeneratedBy()).appendString("on");
        } else {
            builder.appendTaggable(this.effect.getGeneratedBy()).appendString("affected");
        }

        if (this.room != null) {
            builder.appendString("the room").appendTaggable(this.room, " ", "!");
        } else {
            builder.appendString("a room!");
        }
        builder.appendString("\r\n");
        if (this.reversed) {
            builder.appendString("But the effects have EXPIRED, and will now REVERSE!\r\n");
        }

        IMonster summonedMonster = this.effect.getCachedMonster();
        if (summonedMonster != null) {
            builder.appendString("The monster").appendTaggable(summonedMonster).appendString("was summoned.");
        }
        INonPlayerCharacter summonedNPC = this.effect.getCachedNPC();
        if (summonedNPC != null) {
            builder.appendString("The NPC").appendTaggable(summonedNPC).appendString("was summoned.");
        }
    }

}
