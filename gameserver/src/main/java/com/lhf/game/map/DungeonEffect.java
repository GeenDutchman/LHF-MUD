package com.lhf.game.map;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Room.RoomBuilder;

public class DungeonEffect extends EntityEffect {
    protected String createdRoomName;
    protected Directions toCreatedRoom;
    protected String createdRoomDescription;
    protected Room createdRoom;

    private void setDefaultDescription() {
        this.createdRoomDescription = "Created by " + this.creatureResponsible().getColorTaggedName();
    }

    public DungeonEffect(DungeonEffectSource source, ICreature creatureResponsible, Taggable generatedBy,
            String createdRoomName, Directions toCreatedRoom) {
        super(source, creatureResponsible, generatedBy);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.createdRoom = null;
        this.setDefaultDescription();
    }

    public DungeonEffect(DungeonEffectSource source, ICreature creatureResponsible, Taggable generatedBy,
            String createdRoomName, Directions toCreatedRoom, String createdRoomDescription) {
        super(source, creatureResponsible, generatedBy);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.setDefaultDescription();
        this.createdRoomDescription += "\r\n" + createdRoomDescription;
        this.createdRoom = null;
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

    public Room getCreatedRoom() {
        if (this.createdRoom == null) {
            if (this.createdRoom == null) {
                RoomBuilder rb = RoomBuilder.getInstance();
                rb.setName(this.getRoomName());
                rb.setDescription(this.getRoomDescription());
                this.createdRoom = rb.build();
            }
            return this.createdRoom;
        }
        return createdRoom;
    }

    public boolean isAddsRoomToDungeon() {
        return this.getSource().isAddsRoomToDungeon();
    }

}
