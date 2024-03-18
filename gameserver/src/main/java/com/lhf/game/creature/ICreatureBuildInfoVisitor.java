package com.lhf.game.creature;

import java.util.Collection;
import java.util.function.Consumer;

import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;

public interface ICreatureBuildInfoVisitor extends Consumer<ICreatureBuildInfo> {
    public void visit(PlayerBuildInfo buildInfo);

    public void visit(MonsterBuildInfo buildInfo);

    public void visit(INPCBuildInfo buildInfo);

    public void visit(DungeonMasterBuildInfo buildInfo);

    public void visit(CreatureBuildInfo buildInfo);

    @Override
    default void accept(ICreatureBuildInfo arg0) {
        if (arg0 != null) {
            arg0.acceptBuildInfoVisitor(this);
        }
    }

    default void forEach(final Collection<? extends ICreatureBuildInfo> collection) {
        if (collection == null) {
            return;
        }
        for (final ICreatureBuildInfo buildInfo : collection) {
            if (buildInfo != null) {
                buildInfo.acceptBuildInfoVisitor(this);
            }
        }
    }
}
