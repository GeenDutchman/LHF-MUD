package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.NonPlayerCharacter.NPCBuilder;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
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

    private NPCBuilder getNPCToSummon() {
        return this.getSource().getNpcToSummon();
    }

    private MonsterBuilder getMonsterToSummon() {
        return this.getSource().getMonsterToSummon();
    }

    public INonPlayerCharacter getCachedNPC() {
        return this.summonedNPC;
    }

    public IMonster getCachedMonster() {
        return this.summonedMonster;
    }

    public INonPlayerCharacter getQuickSummonedNPC(Supplier<CommandInvoker> controllerSupplier,
            CommandChainHandler successor) {
        if (this.summonedNPC == null) {
            NPCBuilder builder = getNPCToSummon();
            if (builder != null) {
                this.summonedNPC = new SummonedNPC(builder.quickBuild(null, creatureResponsible),
                        builder.getSummonState(), this.creatureResponsible(), this.getPersistence().getTicker());
            }
        }
        return summonedNPC;
    }

    public IMonster getQuickSummonedMonster(Supplier<CommandInvoker> controllerSupplier,
            CommandChainHandler successor) {
        if (this.summonedMonster == null) {
            MonsterBuilder builder = getMonsterToSummon();
            if (builder != null) {
                this.summonedMonster = new SummonedMonster(builder.quickBuild(controllerSupplier, successor),
                        builder.getSummonState(), this.creatureResponsible(), this.getPersistence().getTicker());
            }
        }
        return this.summonedMonster;
    }

    public INonPlayerCharacter getSummonedNPC(Supplier<CommandInvoker> controllerSupplier,
            CommandChainHandler successor, StatblockManager statblockManager,
            UnaryOperator<NPCBuilder> composedlazyLoaders) throws FileNotFoundException {
        if (this.summonedNPC == null) {
            NPCBuilder builder = getNPCToSummon();
            if (builder != null) {
                this.summonedNPC = new SummonedNPC(
                        builder.build(controllerSupplier, successor, statblockManager, composedlazyLoaders),
                        builder.getSummonState(), this.creatureResponsible(), this.getTicker());
            }
        }
        return this.summonedNPC;
    }

    public IMonster getSummonedMonster(Supplier<CommandInvoker> controllerSupplier,
            CommandChainHandler successor, StatblockManager statblockManager,
            UnaryOperator<MonsterBuilder> composedlazyLoaders) throws FileNotFoundException {
        if (this.summonedMonster == null) {
            MonsterBuilder builder = getMonsterToSummon();
            if (builder != null) {
                this.summonedMonster = new SummonedMonster(
                        builder.build(controllerSupplier, successor, statblockManager,
                                composedlazyLoaders),
                        builder.getSummonState(), this.creatureResponsible(), this.getTicker());
            }
        }
        return this.summonedMonster;
    }
}
