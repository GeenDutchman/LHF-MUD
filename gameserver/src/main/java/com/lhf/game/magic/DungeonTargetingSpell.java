package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Directions;
import com.lhf.game.map.DungeonEffect;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.game.map.Room;

public class DungeonTargetingSpell extends ISpell<DungeonEffect> {
    protected Set<DungeonEffect> effects;
    protected String createdRoomName;
    protected Directions toCreatedRoom;
    protected String createdRoomDescription;
    protected Room createdRoom;

    public DungeonTargetingSpell(DungeonTargetingSpellEntry entry, ICreature caster, String createdRoomName,
            Directions toCreatedRoom) {
        super(entry, caster);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.createdRoomDescription = "Created by the great mage " + this.getCaster().getColorTaggedName();
        this.createdRoom = null;
    }

    public DungeonTargetingSpell(DungeonTargetingSpellEntry entry, ICreature caster, String createdRoomName,
            Directions toCreatedRoom,
            String createdRoomDescription) {
        super(entry, caster);
        this.createdRoomName = createdRoomName;
        this.toCreatedRoom = toCreatedRoom;
        this.createdRoomDescription = "Created by the great mage " + this.getCaster().getColorTaggedName() + "\r\n"
                + createdRoomDescription;
        this.createdRoom = null;
    }

    public String getRoomName() {
        return this.createdRoomName;
    }

    public Directions getDirectionToAddedRoom() {
        return this.toCreatedRoom;
    }

    public String getRoomDescription() {
        return this.createdRoomDescription;
    }

    public Room getRoomToAdd() {
        if (this.createdRoom == null) {
            Room.RoomBuilder rb = Room.RoomBuilder.getInstance();
            rb.setName(this.getRoomName());
            rb.setDescription(this.getRoomDescription());
            this.createdRoom = rb.build();
        }
        return this.createdRoom;
    }

    @Override
    public boolean isOffensive() {
        return false;
    }

    @Override
    public Iterator<DungeonEffect> iterator() {
        return this.getEffects().iterator();
    }

    @Override
    public Set<DungeonEffect> getEffects() {
        if (this.effects == null) {
            this.effects = new HashSet<>();
            for (EntityEffectSource source : this.getEntry().getEffectSources()) {
                if (source instanceof DungeonEffectSource) {
                    this.effects.add(new DungeonEffect((DungeonEffectSource) source, this.getCaster(), this,
                            this.getRoomName(), this.getDirectionToAddedRoom(), this.getRoomDescription()));
                }
            }
        }
        return this.effects;
    }

}
