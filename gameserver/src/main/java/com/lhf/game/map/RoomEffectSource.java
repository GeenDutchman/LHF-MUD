package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.MonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;

public class RoomEffectSource extends EntityEffectSource {

    // TODO: implement banishment, with limited to how many

    protected final INPCBuildInfo npcToSummon;
    protected final MonsterBuildInfo monsterToSummon;

    protected static abstract class AbstractBuilder<AB extends AbstractBuilder<AB>>
            extends EntityEffectSource.Builder<AB> {
        private INPCBuildInfo npcToSummon;
        private MonsterBuildInfo monsterToSummon;

        protected AbstractBuilder(String name) {
            super(name);
        }

        public INPCBuildInfo getNpcToSummon() {
            return npcToSummon;
        }

        public AB setNpcToSummon(INPCBuildInfo npcToSummon) {
            this.npcToSummon = npcToSummon;
            return getThis();
        }

        public MonsterBuildInfo getMonsterToSummon() {
            return monsterToSummon;
        }

        public AB setMonsterToSummon(MonsterBuildInfo monsterToSummon) {
            this.monsterToSummon = monsterToSummon;
            return getThis();
        }

    }

    public static class Builder extends AbstractBuilder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public RoomEffectSource build() {
            return new RoomEffectSource(getThis());
        }

    }

    public RoomEffectSource(AbstractBuilder<?> builder) {
        super(builder);
        this.npcToSummon = builder.getNpcToSummon();
        this.monsterToSummon = builder.getMonsterToSummon();
    }

    @Override
    public RoomEffectSource makeCopy() {
        return new RoomEffectSource(this);
    }

    public INPCBuildInfo getNpcToSummon() {
        return npcToSummon;
    }

    public MonsterBuildInfo getMonsterToSummon() {
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
