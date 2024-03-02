package com.lhf.game.creature;

import java.util.function.Consumer;

public interface CreatureVisitor extends Consumer<ICreature> {
    public void visit(Player player);

    public void visit(NonPlayerCharacter npc);

    public void visit(DungeonMaster dungeonMaster);

    public void visit(SummonedNPC sNpc);

    public void visit(Monster monster);

    public void visit(SummonedMonster sMonster);

    @Override
    public default void accept(ICreature arg0) {
        if (arg0 != null) {
            arg0.acceptCreatureVisitor(this);
        }
    }

}
