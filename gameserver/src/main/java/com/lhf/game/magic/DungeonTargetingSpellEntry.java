package com.lhf.game.magic;

import com.lhf.game.EntityEffector.EffectPersistence;
import com.lhf.messages.out.CastingMessage;
import com.lhf.messages.out.SeeOutMessage;

public class DungeonTargetingSpellEntry extends SpellEntry {
    // add way to create dungeon?
    protected boolean addsRoomToDungeon;

    public DungeonTargetingSpellEntry(Integer level, String name, EffectPersistence persistence, String description,
            boolean addsRoomToDungeon) {
        super(level, name, persistence, description);
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    public DungeonTargetingSpellEntry(Integer level, String name, String invocation, EffectPersistence persistence,
            String description, boolean addsRoomToDungeon) {
        super(level, name, invocation, persistence, description);
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    public DungeonTargetingSpellEntry(DungeonTargetingSpellEntry other) {
        super(other);
        this.addsRoomToDungeon = other.addsRoomToDungeon;
    }

    public boolean isAddsRoomToDungeon() {
        return addsRoomToDungeon;
    }

    @Override
    public SeeOutMessage produceMessage() {
        return new SeeOutMessage(this);
    }

    @Override
    public CastingMessage Cast() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (this.isAddsRoomToDungeon()) {
            sb.append("And will add a room to the current dungeon.");
        } else {
            sb.append("And will modify the current dungeon.");
        }
        sb.append("\r\n");
        return sb.toString();
    }

}
