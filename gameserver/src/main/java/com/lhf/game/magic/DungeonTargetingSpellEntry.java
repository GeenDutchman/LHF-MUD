package com.lhf.game.magic;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.map.DungeonEffectSource;
import com.lhf.messages.out.CastingMessage;

public class DungeonTargetingSpellEntry extends SpellEntry {
    // add way to create dungeon?
    protected final boolean addsRoomToDungeon;

    public DungeonTargetingSpellEntry(Integer level, String name, Set<DungeonEffectSource> effectSources,
            Set<VocationName> allowed, String description,
            boolean addsRoomToDungeon) {
        super(level, name, effectSources, allowed, description);
        this.addsRoomToDungeon = addsRoomToDungeon;
    }

    public boolean isAddsRoomToDungeon() {
        return addsRoomToDungeon;
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        StringJoiner sj = new StringJoiner(", ", "Targeting: ", "").setEmptyValue("nothing");
        if (targets != null) {
            for (Taggable taggable : targets) {
                sj.add(taggable.getColorTaggedName());
            }
        }
        return CastingMessage.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sj.toString()).Build();
    }

    @Override
    public String printDescription() {
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
