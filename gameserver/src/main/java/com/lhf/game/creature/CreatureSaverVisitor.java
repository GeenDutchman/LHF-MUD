package com.lhf.game.creature;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.game.creature.ICreature.ICreatureID;
import com.lhf.game.item.ItemSaverVisitor;

public final class CreatureSaverVisitor implements CreatureVisitor {
    private final transient LinkedHashMap<ICreatureID, Player> players = new LinkedHashMap<>();
    private final LinkedHashMap<ICreatureID, NonPlayerCharacter> npcs = new LinkedHashMap<>();
    private final LinkedHashMap<ICreatureID, DungeonMaster> dungeonMasters = new LinkedHashMap<>();
    private final transient LinkedHashMap<ICreatureID, SummonedNPC> summonedNPCs = new LinkedHashMap<>();
    private final LinkedHashMap<ICreatureID, Monster> monsters = new LinkedHashMap<>();
    private final transient LinkedHashMap<ICreatureID, SummonedMonster> summonedMonsters = new LinkedHashMap<>();
    private final ItemSaverVisitor itemSaverVisitor;

    public CreatureSaverVisitor() {
        this.itemSaverVisitor = new ItemSaverVisitor();
    }

    public CreatureSaverVisitor(ItemSaverVisitor itemSaver) {
        this.itemSaverVisitor = itemSaver != null ? itemSaver : new ItemSaverVisitor();
    }

    public ItemSaverVisitor getItemSaverVisitor() {
        return itemSaverVisitor;
    }

    @Override
    public void visit(Player player) {
        if (player == null) {
            return;
        }
        this.players.put(player.getCreatureID(), player);
        player.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(NonPlayerCharacter npc) {
        if (npc == null) {
            return;
        }
        this.npcs.put(npc.getCreatureID(), npc);
        npc.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(DungeonMaster dungeonMaster) {
        if (dungeonMaster == null) {
            return;
        }
        this.dungeonMasters.put(dungeonMaster.getCreatureID(), dungeonMaster);
        dungeonMaster.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(SummonedNPC sNpc) {
        if (sNpc == null) {
            return;
        }
        this.summonedNPCs.put(sNpc.getCreatureID(), sNpc);
        sNpc.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(Monster monster) {
        if (monster == null) {
            return;
        }
        this.monsters.put(monster.getCreatureID(), monster);
        monster.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(SummonedMonster sMonster) {
        if (sMonster == null) {
            return;
        }
        this.summonedMonsters.put(sMonster.getCreatureID(), sMonster);
        sMonster.acceptItemVisitor(this.itemSaverVisitor);
    }

    public Map<ICreatureID, ICreature> getICreaturesMap() {
        return Collections.unmodifiableMap(
                Stream.concat(this.getPlayersMap().entrySet().stream(), this.getINpcsMap().entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b,
                                LinkedHashMap::new)));
    }

    public Map<ICreatureID, Player> getPlayersMap() {
        return Collections.unmodifiableMap(this.players);
    }

    public Map<ICreatureID, INonPlayerCharacter> getINpcsMap() {
        LinkedHashMap<ICreatureID, INonPlayerCharacter> iNpcs = new LinkedHashMap<>();
        iNpcs.putAll(this.getIMonstersMap());
        iNpcs.putAll(this.getSummonedINPCsMap());
        iNpcs.putAll(this.getDungeonMastersMap());
        iNpcs.putAll(this.getNpcsMap());
        return Collections.unmodifiableMap(iNpcs);
    }

    public Map<ICreatureID, NonPlayerCharacter> getNpcsMap() {
        return Collections.unmodifiableMap(npcs);
    }

    public Map<ICreatureID, DungeonMaster> getDungeonMastersMap() {
        return Collections.unmodifiableMap(dungeonMasters);
    }

    public Map<ICreatureID, ? extends SummonedINonPlayerCharacter<?>> getSummonedINPCsMap() {
        return Collections.unmodifiableMap(
                Stream.concat(this.getSummonedNPCsMap().entrySet().stream(),
                        this.getSummonedMonstersMap().entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b,
                                LinkedHashMap::new)));
    }

    public Map<ICreatureID, SummonedNPC> getSummonedNPCsMap() {
        return Collections.unmodifiableMap(summonedNPCs);
    }

    public Map<ICreatureID, IMonster> getIMonstersMap() {
        LinkedHashMap<ICreatureID, IMonster> imonsters = new LinkedHashMap<>();
        imonsters.putAll(this.monsters);
        imonsters.putAll(this.getSummonedMonstersMap());
        return Collections.unmodifiableMap(imonsters);
    }

    public Map<ICreatureID, Monster> getMonstersMap() {
        return Collections.unmodifiableMap(monsters);
    }

    public Map<ICreatureID, SummonedMonster> getSummonedMonstersMap() {
        return Collections.unmodifiableMap(this.summonedMonsters);
    }

}
