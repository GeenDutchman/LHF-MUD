package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.IMonster.IMonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;

public class RoomEffectSource extends EntityEffectSource {

    // TODO: implement banishment, with limited to how many

    protected INPCBuildInfo npcToSummon;
    protected IMonsterBuildInfo monsterToSummon;

    public RoomEffectSource(RoomEffectSource other) {
        super(other.name, other.persistence, other.resistance, other.description);
        this.npcToSummon = other.npcToSummon != null ? new INPCBuildInfo(other.npcToSummon) : null;
        this.monsterToSummon = other.monsterToSummon != null ? new IMonsterBuildInfo(other.monsterToSummon) : null;
    }

    public RoomEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description) {
        super(name, persistence, resistance, description);
        this.npcToSummon = null;
        this.monsterToSummon = null;
    }

    @Override
    public RoomEffectSource makeCopy() {
        return new RoomEffectSource(this);
    }

    public RoomEffectSource setCreatureToSummon(IMonsterBuildInfo toSummon) {
        this.monsterToSummon = toSummon;
        return this;
    }

    public RoomEffectSource setCreatureToSummon(INPCBuildInfo toSummon) {
        this.npcToSummon = toSummon;
        return this;
    }

    public INPCBuildInfo getNpcToSummon() {
        return npcToSummon;
    }

    public IMonsterBuildInfo getMonsterToSummon() {
        return monsterToSummon;
    }

    @Override
    public boolean isOffensive() {
        // can be offensive when banished
        return false;
    }

    @Override
    public int aiScore() {
        return (this.monsterToSummon != null ? 2 : 1) *
                (this.npcToSummon != null ? 10 : 1);
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        if (this.monsterToSummon != null) {
            sb.append("\r\nWill summon the following Monster:\r\n");
            sb.append(this.monsterToSummon.toString());
        }
        if (this.npcToSummon != null) {
            sb.append("\r\nWill summon the following NPC:\r\n");
            sb.append(this.npcToSummon.toString());
        }
        return sb.toString();
    }

}
