package com.lhf.game.map;

import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.MonsterBuildInfo;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
import com.lhf.server.interfaces.NotNull;

public class RoomEffect extends EntityEffect {
    protected SummonedNPC summonedNPC;
    protected SummonedMonster summonedMonster;

    public RoomEffect(@NotNull RoomEffect other) {
        super(other.source, other.creatureResponsible, other.generatedBy);
        this.summonedMonster = null;
        this.summonedNPC = null;
    }

    public RoomEffect(RoomEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
        this.summonedMonster = null;
        this.summonedNPC = null;
    }

    public RoomEffectSource getSource() {
        return (RoomEffectSource) this.source;
    }

    private INPCBuildInfo getNPCToSummon() {
        return this.getSource().getNpcToSummon();
    }

    private MonsterBuildInfo getMonsterToSummon() {
        return this.getSource().getMonsterToSummon();
    }

    public SortedSet<ICreatureBuildInfo> getBuildInfos() {
        TreeSet<ICreatureBuildInfo> buildInfos = new TreeSet<>();
        INPCBuildInfo npcSummon = this.getNPCToSummon();
        if (npcSummon != null) {
            buildInfos.add(npcSummon);
        }
        MonsterBuildInfo monsterSummon = this.getMonsterToSummon();
        if (monsterSummon != null) {
            buildInfos.add(monsterSummon);
        }
        return buildInfos;
    }

    public SummonedNPC getCachedNPC() {
        return this.summonedNPC;
    }

    public SummonedMonster getCachedMonster() {
        return this.summonedMonster;
    }

    public SummonedNPC getSummonedNPC(CreatureFactory factory) {
        if (this.summonedNPC == null) {
            INPCBuildInfo builder = getNPCToSummon();
            if (builder != null) {
                if (factory == null) {
                    factory = new CreatureFactory();
                }
                factory.visit(builder);
                this.summonedNPC = factory.summonNPC(builder, this.creatureResponsible(),
                        this.getPersistence().getTicker());
            }
        }
        return this.summonedNPC;
    }

    public SummonedMonster getSummonedMonster(CreatureFactory factory) {
        if (this.summonedMonster == null) {
            MonsterBuildInfo builder = getMonsterToSummon();
            if (builder != null) {
                if (factory == null) {
                    factory = new CreatureFactory();
                }
                this.summonedMonster = factory.summonMonster(builder, this.creatureResponsible(),
                        this.getPersistence().getTicker());
            }
        }
        return this.summonedMonster;
    }

    public NavigableSet<ICreature> getCreatures(CreatureFactory factory) {
        if (factory == null) {
            factory = new CreatureFactory();
        }
        this.getSummonedMonster(factory);
        this.getSummonedNPC(factory);
        return factory.getBuiltCreatures().getICreatures();
    }
}
