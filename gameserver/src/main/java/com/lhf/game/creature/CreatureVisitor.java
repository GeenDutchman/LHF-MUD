package com.lhf.game.creature;

public interface CreatureVisitor {
    public void visit(Player player);

    public void visit(NonPlayerCharacter npc);

    public void visit(DungeonMaster dungeonMaster);

    public void visit(SummonedNPC sNpc);

    public void visit(Monster monster);

    public void visit(SummonedMonster sMonster);

}
