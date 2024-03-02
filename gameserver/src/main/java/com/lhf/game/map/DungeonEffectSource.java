package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.map.Area.AreaBuilder;

public class DungeonEffectSource extends EntityEffectSource {
    protected final AreaBuilder areaBuilder;

    public static class Builder extends EntityEffectSource.Builder<Builder> {
        private AreaBuilder areaBuilder;

        public Builder(String name) {
            super(name);
        }

        public AreaBuilder getAreaBuilder() {
            return areaBuilder;
        }

        public Builder setAreaBuilder(AreaBuilder areaBuilder) {
            this.areaBuilder = areaBuilder;
            return getThis();
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public DungeonEffectSource build() {
            return new DungeonEffectSource(getThis());
        }

    }

    public static Builder getBuilder(String name) {
        return new Builder(name);
    }

    protected DungeonEffectSource(Builder builder) {
        super(builder);
        this.areaBuilder = builder.getAreaBuilder();
    }

    @Override
    public DungeonEffectSource makeCopy() {
        return new DungeonEffectSource(this.getName(), persistence, resistance, description, addsRoomToDungeon);
    }

    public boolean addsRoomToDungeon() {
        return this.areaBuilder != null;
    }

    @Override
    public String printDescription() {
        return super.printDescription()
                + (this.addsRoomToDungeon() ? "\r\nAdds a room to the dungeon!"
                        : "\r\nWill modify the current dungeon.");
    }

    @Override
    public boolean isOffensive() {
        return false;
    }

    @Override
    public int aiScore() {
        return 0;
    }

}
