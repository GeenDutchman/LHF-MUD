package com.lhf.game.creature;

public class CreatureNoOpVisitor implements CreatureVisitor {

    @Override
    public void visit(Player player) {
        // deliberately does nothing so it can be overriden
    }

    @Override
    public void visit(NonPlayerCharacter npc) {
        // deliberately does nothing so it can be overriden
    }

    @Override
    public void visit(DungeonMaster dungeonMaster) {
        // deliberately does nothing so it can be overriden
    }

    @Override
    public void visit(SummonedNPC sNpc) {
        // deliberately does nothing so it can be overriden
    }

    @Override
    public void visit(Monster monster) {
        // deliberately does nothing so it can be overriden
    }

    @Override
    public void visit(SummonedMonster sMonster) {
        // deliberately does nothing so it can be overriden
    }

}
