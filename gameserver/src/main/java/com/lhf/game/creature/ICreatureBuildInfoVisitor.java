package com.lhf.game.creature;

import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;

public interface ICreatureBuildInfoVisitor {
    public void visit(PlayerBuildInfo buildInfo);

    public void visit(MonsterBuildInfo buildInfo);

    public void visit(INPCBuildInfo buildInfo);

    public void visit(DungeonMasterBuildInfo buildInfo);

    public void visit(CreatureBuildInfo buildInfo);
}
