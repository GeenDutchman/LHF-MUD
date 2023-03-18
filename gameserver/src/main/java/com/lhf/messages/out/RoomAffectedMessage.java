package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomEffect;
import com.lhf.messages.OutMessageType;

public class RoomAffectedMessage extends OutMessage {
    private final Room room;
    private final RoomEffect effect;
    private final boolean reversed;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Room room;
        private RoomEffect effect;
        private boolean reversed;

        protected Builder() {
            super(OutMessageType.ROOM_AFFECTED);
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
        public RoomAffectedMessage Build() {
            return new RoomAffectedMessage(this);
        }

    }

    public RoomAffectedMessage(Builder builder) {
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

        StringJoiner lister = null;

        if (this.effect.getItemsToSummon().size() > 0) {
            lister = new StringJoiner(", ", "", this.reversed ? " are de-summoned.\r\n" : " are summoned.\r\n")
                    .setEmptyValue("No items ");
            for (Item item : this.effect.getItemsToSummon()) {
                lister.add(item.getColorTaggedName());
            }
            sj.add(lister.toString());
        }
        if (this.effect.getItemsToBanish().size() > 0) {
            lister = new StringJoiner(", ", "", this.reversed ? " are de-banished.\r\n" : " are banished.\r\n")
                    .setEmptyValue("No items ");
            for (Item item : this.effect.getItemsToBanish()) {
                lister.add(item.getColorTaggedName());
            }
            sj.add(lister.toString());
        }

        if (this.effect.getCreaturesToSummon().size() > 0) {
            lister = new StringJoiner(", ", "", this.reversed ? " are de-summoned.\r\n" : " are summoned.\r\n")
                    .setEmptyValue("No creatures ");
            for (Creature creature : this.effect.getCreaturesToSummon()) {
                lister.add(creature.getColorTaggedName());
            }
            sj.add(lister.toString());
        }
        if (this.effect.getCreaturesToBanish().size() > 0) {
            lister = new StringJoiner(", ", "", this.reversed ? " are de-banished.\r\n" : " are banished.\r\n")
                    .setEmptyValue("No creatures ");
            for (Creature creature : this.effect.getCreaturesToBanish()) {
                lister.add(creature.getColorTaggedName());
            }
            sj.add(lister.toString());
        }

        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }
}
