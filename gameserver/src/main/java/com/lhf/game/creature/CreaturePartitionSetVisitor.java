package com.lhf.game.creature;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreaturePartitionSetVisitor implements CreatureVisitor {
    private final NavigableSet<Player> players = new TreeSet<>();
    private final NavigableSet<NonPlayerCharacter> npcs = new TreeSet<>();
    private final NavigableSet<DungeonMaster> dungeonMasters = new TreeSet<>();
    private final NavigableSet<SummonedNPC> summonedNPCs = new TreeSet<>();
    private final NavigableSet<Monster> monsters = new TreeSet<>();
    private final NavigableSet<SummonedMonster> summonedMonsters = new TreeSet<>();

    @Override
    public void visit(Player player) {
        if (player == null) {
            return;
        }
        this.players.add(player);
    }

    @Override
    public void visit(NonPlayerCharacter npc) {
        if (npc == null) {
            return;
        }
        this.npcs.add(npc);
    }

    @Override
    public void visit(DungeonMaster dungeonMaster) {
        if (dungeonMaster == null) {
            return;
        }
        this.dungeonMasters.add(dungeonMaster);
    }

    @Override
    public void visit(SummonedNPC sNpc) {
        if (sNpc == null) {
            return;
        }
        this.summonedNPCs.add(sNpc);
    }

    @Override
    public void visit(Monster monster) {
        if (monster == null) {
            return;
        }
        this.monsters.add(monster);
    }

    @Override
    public void visit(SummonedMonster sMonster) {
        if (sMonster == null) {
            return;
        }
        this.summonedMonsters.add(sMonster);
    }

    public NavigableSet<ICreature> getICreatures() {
        return Collections.unmodifiableNavigableSet(Stream.concat(this.getPlayers().stream(), this.getINpcs().stream())
                .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<Player> getPlayers() {
        return Collections.unmodifiableNavigableSet(this.players);
    }

    public NavigableSet<INonPlayerCharacter> getINpcs() {
        Stream<IMonster> iMonsters = this.getIMonsters().stream();
        Stream<? extends SummonedINonPlayerCharacter<?>> summonedINpcs = this.getSummonedINPCs().stream();
        Stream<DungeonMaster> dms = this.getDungeonMasters().stream();
        Stream<NonPlayerCharacter> npcStream = this.getNpcs().stream();
        Stream<INonPlayerCharacter> inpcs = Stream.of(npcStream, iMonsters, summonedINpcs, dms).flatMap(inpc -> inpc);
        return Collections.unmodifiableNavigableSet(inpcs.collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<NonPlayerCharacter> getNpcs() {
        return Collections.unmodifiableNavigableSet(npcs);
    }

    public NavigableSet<DungeonMaster> getDungeonMasters() {
        return Collections.unmodifiableNavigableSet(dungeonMasters);
    }

    public NavigableSet<? extends SummonedINonPlayerCharacter<?>> getSummonedINPCs() {
        return Collections.unmodifiableNavigableSet(
                Stream.concat(this.getSummonedNPCs().stream(), this.getSummonedMonsters().stream())
                        .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<SummonedNPC> getSummonedNPCs() {
        return Collections.unmodifiableNavigableSet(summonedNPCs);
    }

    public NavigableSet<IMonster> getIMonsters() {
        return Collections
                .unmodifiableNavigableSet(Stream.concat(this.monsters.stream(), this.getSummonedMonsters().stream())
                        .collect(Collectors.toCollection(() -> new TreeSet<>())));
    }

    public NavigableSet<Monster> getMonsters() {
        return Collections.unmodifiableNavigableSet(monsters);
    }

    public NavigableSet<SummonedMonster> getSummonedMonsters() {
        return Collections.unmodifiableNavigableSet(this.summonedMonsters);
    }

}
