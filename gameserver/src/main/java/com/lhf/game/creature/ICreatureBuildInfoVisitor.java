package com.lhf.game.creature;

import com.lhf.game.creature.IMonster.IMonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;

public interface ICreatureBuildInfoVisitor {
    public void visit(PlayerBuildInfo buildInfo);

    public void visit(IMonsterBuildInfo buildInfo);

    public void visit(INPCBuildInfo buildInfo);

    public void visit(CreatureBuildInfo buildInfo);
}
