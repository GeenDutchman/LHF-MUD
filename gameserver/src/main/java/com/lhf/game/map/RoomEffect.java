package com.lhf.game.map;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.CreatureFactory;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.IMonster.IMonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.interfaces.NotNull;

public class RoomEffect extends EntityEffect {
    protected INonPlayerCharacter summonedNPC;
    protected IMonster summonedMonster;

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

    private IMonsterBuildInfo getMonsterToSummon() {
        return this.getSource().getMonsterToSummon();
    }

    public INonPlayerCharacter getCachedNPC() {
        return this.summonedNPC;
    }

    public IMonster getCachedMonster() {
        return this.summonedMonster;
    }

    public INonPlayerCharacter getQuickSummonedNPC(CommandChainHandler successor) {
        if (this.summonedNPC == null) {
            INPCBuildInfo builder = getNPCToSummon();
            if (builder != null) {
                CreatureFactory factory = CreatureFactory.withAIRunner(successor, null);
                factory.visit(builder);
                this.summonedNPC = new SummonedNPC(factory.getBuiltCreatures().getNpcs().first(),
                        builder.getSummonState(), this.creatureResponsible(), this.getPersistence().getTicker());
            }
        }
        return summonedNPC;
    }

    public IMonster getQuickSummonedMonster(CommandChainHandler successor) {
        if (this.summonedMonster == null) {
            IMonsterBuildInfo builder = getMonsterToSummon();
            if (builder != null) {
                CreatureFactory factory = CreatureFactory.withAIRunner(successor, null);
                factory.visit(builder);
                this.summonedMonster = new SummonedMonster(factory.getBuiltCreatures().getMonsters().first(),
                        builder.getSummonState(), this.creatureResponsible(), this.getPersistence().getTicker());
            }
        }
        return this.summonedMonster;
    }

    public INonPlayerCharacter getSummonedNPC(CommandChainHandler successor) {
        if (this.summonedNPC == null) {
            INPCBuildInfo builder = getNPCToSummon();
            if (builder != null) {
                CreatureFactory factory = new CreatureFactory(successor, null, null, false);
                factory.visit(builder);
                this.summonedMonster = new SummonedMonster(factory.getBuiltCreatures().getMonsters().first(),
                        builder.getSummonState(), this.creatureResponsible(), this.getPersistence().getTicker());
            }
        }
        return this.summonedNPC;
    }

    public IMonster getSummonedMonster(CommandChainHandler successor) {
        if (this.summonedMonster == null) {
            IMonsterBuildInfo builder = getMonsterToSummon();
            if (builder != null) {
                CreatureFactory factory = new CreatureFactory(successor, null, null, false);
                factory.visit(builder);
                this.summonedMonster = new SummonedMonster(factory.getBuiltCreatures().getMonsters().first(),
                        builder.getSummonState(), this.creatureResponsible(), this.getPersistence().getTicker());
            }
        }
        return this.summonedMonster;
    }
}
