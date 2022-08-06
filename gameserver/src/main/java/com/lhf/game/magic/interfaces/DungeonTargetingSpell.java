package com.lhf.game.magic.interfaces;

import com.lhf.game.magic.ISpell;
import com.lhf.game.magic.SpellEntry;
import com.lhf.game.map.Directions;
import com.lhf.game.map.DungeonEffector;
import com.lhf.game.map.Room;
import com.lhf.game.map.RoomBuilder;

public class DungeonTargetingSpell extends ISpell implements DungeonEffector {
    protected String createdRoomName;
    protected Directions toCreatedRoom;
    protected String createdRoomDescription;
    protected Room createdRoom;

    public DungeonTargetingSpell(SpellEntry entry, String createdRoomName, Directions toCreatedRoom) {
        super(entry);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.createdRoomDescription = "Created by the great mage " + this.getCaster().getColorTaggedName();
        this.createdRoom = null;
    }

    public DungeonTargetingSpell(SpellEntry entry, String createdRoomName, Directions toCreatedRoom,
            String createdRoomDescription) {
        super(entry);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.createdRoomDescription = "Created by the great mage " + this.getCaster().getColorTaggedName() + "\r\n"
                + createdRoomDescription;
        this.createdRoom = null;
    }

    @Override
    public String getRoomName() {
        return this.createdRoomName;
    }

    @Override
    public Directions getDirectionToAddedRoom() {
        return this.toCreatedRoom;
    }

    @Override
    public String getRoomDescription() {
        return this.createdRoomDescription;
    }

    @Override
    public Room getRoomToAdd() {
        if (this.createdRoom == null) {
            RoomBuilder rb = RoomBuilder.getInstance();
            rb.setName(this.getRoomName());
            rb.setDescription(this.getRoomDescription());
            this.createdRoom = rb.build();
        }
        return this.createdRoom;
    }

}
