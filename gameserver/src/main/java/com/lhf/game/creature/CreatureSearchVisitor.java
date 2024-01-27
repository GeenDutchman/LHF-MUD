package com.lhf.game.creature;

import java.util.Optional;

import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.CreatureContainer.CreatureFilters;
import com.lhf.game.creature.vocation.Vocation;

public class CreatureSearchVisitor extends CreaturePartitionSetVisitor {
    private final CreatureFilterQuery query;

    public CreatureSearchVisitor(CreatureFilterQuery query) {
        this.query = query != null ? query : new CreatureFilterQuery();
    }

    public CreatureSearchVisitor(String name) {
        this.query = new CreatureFilterQuery();
        this.query.filters.add(CreatureFilters.NAME);
        this.query.name = name;
    }

    public CreatureSearchVisitor(String name, Integer regexLength) {
        this.query = new CreatureFilterQuery();
        if (name != null) {
            this.query.filters.add(CreatureFilters.NAME);
        }
        this.query.name = name;
        this.query.nameRegexLen = regexLength;
    }

    // public void copyFrom(CreaturePartitionSetVisitor partitioner) {
    // if (partitioner == null) {
    // return;
    // }
    // partitioner.getICreatures().stream().filter(creature -> creature != null)
    // .forEachOrdered(creature -> creature.acceptVisitor(this));
    // }

    private boolean queryCreature(ICreature creature) {
        if (creature == null) {
            return false;
        }
        if (this.query.filters.contains(CreatureFilters.NAME) && this.query.name != null
                && !(this.query.nameRegexLen != null ? creature.CheckNameRegex(this.query.name, this.query.nameRegexLen)
                        : creature.checkName(this.query.name))) {
            return false;
        }
        if (this.query.filters.contains(CreatureFilters.FACTION) && this.query.faction != null
                && !this.query.faction.equals(creature.getFaction())) {
            return false;
        }
        if (this.query.filters.contains(CreatureFilters.VOCATION) && this.query.vocation != null) {
            final Vocation cVocation = creature.getVocation();
            if (cVocation == null) {
                return false;
            }
            if (!this.query.vocation.equals(cVocation.getVocationName())) {
                return false;
            }
        }
        if (this.query.filters.contains(CreatureFilters.BATTLING) && this.query.isBattling != null
                && this.query.isBattling != creature.isInBattle()) {
            return false;
        }
        return true;
    }

    @Override
    public void visit(Player player) {
        if (this.queryCreature(player)) {
            super.visit(player);
        }
    }

    @Override
    public void visit(NonPlayerCharacter npc) {
        if (this.queryCreature(npc)) {
            super.visit(npc);
        }
    }

    @Override
    public void visit(DungeonMaster dungeonMaster) {
        if (this.queryCreature(dungeonMaster)) {
            super.visit(dungeonMaster);
        }
    }

    @Override
    public void visit(SummonedNPC sNpc) {
        if (this.queryCreature(sNpc)) {
            super.visit(sNpc);
        }
    }

    @Override
    public void visit(Monster monster) {
        if (this.queryCreature(monster)) {
            super.visit(monster);
        }
    }

    @Override
    public void visit(SummonedMonster sMonster) {
        if (this.queryCreature(sMonster)) {
            super.visit(sMonster);
        }
    }

    public Optional<ICreature> getICreature() {
        return Optional.ofNullable(super.getICreatures().first());
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(super.getPlayers().first());
    }

    public Optional<INonPlayerCharacter> getINPC() {
        return Optional.ofNullable(this.getINpcs().first());
    }

    public Optional<NonPlayerCharacter> getNonPlayerCharacter() {
        return Optional.ofNullable(super.getNpcs().first());
    }

    public Optional<DungeonMaster> getDungeonMaster() {
        return Optional.ofNullable(super.getDungeonMasters().first());
    }

    public Optional<? extends SummonedINonPlayerCharacter<?>> getSummonedINPC() {
        return Optional.ofNullable(super.getSummonedINPCs().first());
    }

    public Optional<SummonedNPC> getSummonedNPC() {
        return Optional.ofNullable(super.getSummonedNPCs().first());
    }

    public Optional<IMonster> getIMonster() {
        return Optional.ofNullable(super.getIMonsters().first());
    }

    public Optional<Monster> getMonster() {
        return Optional.ofNullable(super.getMonsters().first());
    }

    public Optional<SummonedMonster> getSummonedMonster() {
        return Optional.ofNullable(super.getSummonedMonsters().first());
    }
}
