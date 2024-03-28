package com.lhf.game.magic;

import java.util.List;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.messages.events.SpellCastingEvent;

public class DungeonTargetingSpellEntry extends SpellEntry {
    // add way to create dungeon?
    protected final boolean addsRoomToDungeon;

    public DungeonTargetingSpellEntry(ResourceCost level, String name, Set<DungeonEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean addsRoomToDungeon) {
        super(level, name, effectSources, allowed, description);
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    public boolean isAddsRoomToDungeon() {
        return addsRoomToDungeon;
    }

    @Override
    public SpellCastingEvent Cast(ICreature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        return SpellCastingEvent.getBuilder().setCaster(caster).setSpellEntry(this).defaultTargetingStyle()
                .setTargets(targets).Build();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder(this.description);
        if (this.isAddsRoomToDungeon()) {
            sb.append("And will add a room to the current dungeon.");
        } else {
            sb.append("And will modify the current dungeon.");
        }
        sb.append("\r\n");
        return sb.toString() + super.printEffectDescriptions();
    }

}
