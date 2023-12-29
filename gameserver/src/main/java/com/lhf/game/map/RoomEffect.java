package com.lhf.game.map;

import java.util.Collections;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.NonPlayerCharacter.NPCBuilder;
import com.lhf.server.interfaces.NotNull;

public class RoomEffect extends EntityEffect {

    public RoomEffect(@NotNull RoomEffect other) {
        super(other.source, other.creatureResponsible, other.generatedBy);
    }

    public RoomEffect(RoomEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
    }

    public RoomEffectSource getSource() {
        return (RoomEffectSource) this.source;
    }

    public Set<NPCBuilder> getNPCsToSummon() {
        return Collections.unmodifiableSet(this.getSource().getNpcsToSummon());
    }

    public Set<MonsterBuilder> getMonstersToSummon() {
        return Collections.unmodifiableSet(this.getSource().getMonstersToSummon());
    }
}
