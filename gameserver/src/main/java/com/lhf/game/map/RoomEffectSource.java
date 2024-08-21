package com.lhf.game.map;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.MonsterBuildInfo;

public class RoomEffectSource extends EntityEffectSource {

    // TODO: implement banishment, with limited to how many

    protected final INPCBuildInfo npcToSummon;
    protected final MonsterBuildInfo monsterToSummon;

    public enum ShowHidden {
        TO_NONE, TO_USER_ONLY, TO_AREA;
    }

    protected final ShowHidden showHidden;

    public static abstract class AbstractBuilder<AB extends AbstractBuilder<AB>>
            extends EntityEffectSource.Builder<AB> {
        private INPCBuildInfo npcToSummon;
        private MonsterBuildInfo monsterToSummon;
        private ShowHidden showHidden = ShowHidden.TO_NONE;

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

        public ShowHidden getShowHidden() {
            return showHidden;
        }

        public AB setShowHidden(ShowHidden showHidden) {
            this.showHidden = showHidden;
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
        this.showHidden = builder.getShowHidden();
    }

    public INPCBuildInfo getNpcToSummon() {
        return npcToSummon != null ? new INPCBuildInfo(npcToSummon) : null;
    }

    public MonsterBuildInfo getMonsterToSummon() {
        return monsterToSummon != null ? new MonsterBuildInfo(monsterToSummon) : null;
    }

    @Override
    public boolean isOffensive() {
        // can be offensive when banished
        return false;
    }

    @Override
    public int aiScore() {
        return (this.monsterToSummon != null ? 2 : 1) * (this.npcToSummon != null ? 10 : 1);
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder(super.getDescription());
        if (this.monsterToSummon != null) {
            sb.append("\r\nWill summon the following Monster:\r\n");
            sb.append(this.monsterToSummon.toString());
        }
        if (this.npcToSummon != null) {
            sb.append("\r\nWill summon the following NPC:\r\n");
            sb.append(this.npcToSummon.toString());
        }
        if (this.showHidden != null) {
            switch (this.showHidden) {
            case TO_AREA:
                sb.append("\r\nWill broadcast the presence of hidden things to the area.");
                break;
            case TO_USER_ONLY:
                sb.append("\r\nWill show hidden things to the user.");
                break;
            case TO_NONE:
                break;
            default:
                break;

            }
        }

        return sb.toString();
    }

    public ShowHidden getShowHidden() {
        return showHidden;
    }

}
