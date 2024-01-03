package com.lhf.game.map;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Room.RoomBuilder;

public class DungeonEffect extends EntityEffect {
    protected String createdRoomName;
    protected Directions toCreatedRoom;
    protected String createdRoomDescription;
    protected RoomBuilder roomToMake;

    private void setDefaultDescription() {
        this.createdRoomDescription = "Created by " + this.creatureResponsible().getColorTaggedName();
    }

    public DungeonEffect(DungeonEffectSource source, ICreature creatureResponsible, Taggable generatedBy,
            String createdRoomName, Directions toCreatedRoom) {
        super(source, creatureResponsible, generatedBy);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.roomToMake = null;
        this.setDefaultDescription();
    }

    public DungeonEffect(DungeonEffectSource source, ICreature creatureResponsible, Taggable generatedBy,
            String createdRoomName, Directions toCreatedRoom, String createdRoomDescription) {
        super(source, creatureResponsible, generatedBy);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.setDefaultDescription();
        this.createdRoomDescription += "\r\n" + createdRoomDescription;
        this.roomToMake = null;
    }

    public DungeonEffectSource getSource() {
        return (DungeonEffectSource) this.source;
    }

    public String getRoomName() {
        return createdRoomName;
    }

    public Directions getToCreatedRoom() {
        return toCreatedRoom;
    }

    public String getRoomDescription() {
        return createdRoomDescription;
    }

    public RoomBuilder getRoomToMake() {
        if (this.roomToMake == null) {
            this.roomToMake = RoomBuilder.getInstance();
            this.roomToMake.setName(this.getRoomName())
                    .setDescription(this.getRoomDescription());
        }
        return roomToMake;
    }

    public boolean isAddsRoomToDungeon() {
        return this.getSource().isAddsRoomToDungeon();
    }

}
