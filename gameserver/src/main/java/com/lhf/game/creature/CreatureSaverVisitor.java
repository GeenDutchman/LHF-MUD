package com.lhf.game.creature;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.game.creature.ICreature.ICreatureID;
import com.lhf.game.item.ItemSaverVisitor;

public final class CreatureSaverVisitor implements CreatureVisitor {
    private final Map<ICreatureID, ICreature> creatureMap = new TreeMap<>();
    private final ItemSaverVisitor itemSaverVisitor;

    public CreatureSaverVisitor() {
        this.itemSaverVisitor = new ItemSaverVisitor();
    }

    public CreatureSaverVisitor(ItemSaverVisitor itemSaver) {
        this.itemSaverVisitor = itemSaver != null ? itemSaver : new ItemSaverVisitor();
    }

    public Map<ICreatureID, ICreature> getCreatureMap() {
        return Collections.unmodifiableMap(this.creatureMap);
    }

    public ItemSaverVisitor getItemSaverVisitor() {
        return itemSaverVisitor;
    }

    @Override
    public void visit(Player player) {
        if (player == null) {
            return;
        }
        this.creatureMap.put(player.getCreatureID(), player);
        player.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(NonPlayerCharacter npc) {
        if (npc == null) {
            return;
        }
        this.creatureMap.put(npc.getCreatureID(), npc);
        npc.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(DungeonMaster dungeonMaster) {
        if (dungeonMaster == null) {
            return;
        }
        this.creatureMap.put(dungeonMaster.getCreatureID(), dungeonMaster);
        dungeonMaster.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(SummonedNPC sNpc) {
        if (sNpc == null) {
            return;
        }
        this.creatureMap.put(sNpc.getCreatureID(), sNpc);
        sNpc.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(Monster monster) {
        if (monster == null) {
            return;
        }
        this.creatureMap.put(monster.getCreatureID(), monster);
        monster.acceptItemVisitor(this.itemSaverVisitor);
    }

    @Override
    public void visit(SummonedMonster sMonster) {
        if (sMonster == null) {
            return;
        }
        this.creatureMap.put(sMonster.getCreatureID(), sMonster);
        sMonster.acceptItemVisitor(this.itemSaverVisitor);
    }

}
