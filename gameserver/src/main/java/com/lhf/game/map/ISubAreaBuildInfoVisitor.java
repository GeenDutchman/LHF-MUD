package com.lhf.game.map;

import com.lhf.game.battle.BattleManager;
import com.lhf.game.map.SubArea.ISubAreaBuildInfo;
import com.lhf.game.map.SubArea.SubAreaBuilder;

public interface ISubAreaBuildInfoVisitor {
    public abstract void visit(RestArea.Builder buildInfo);

    public abstract void visit(BattleManager.Builder buildInfo);

    public abstract void visit(SubAreaBuilder buildInfo);

    public default void visit(ISubAreaBuildInfo buildInfo) {
        throw new UnsupportedOperationException(String.format("Cannot yet visit %s", buildInfo));
    }
}