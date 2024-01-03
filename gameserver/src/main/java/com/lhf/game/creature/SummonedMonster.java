package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumSet;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder.SummonData;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.messages.CommandChainHandler;

public class SummonedMonster extends SummonedINonPlayerCharacter<Monster> implements IMonster {

    public SummonedMonster(Monster monster, EnumSet<SummonData> summonData, ICreature summoner, Ticker timeLeft) {
        super(monster, summonData, summoner, timeLeft);
    }

    public SummonedMonster(MonsterBuilder builder, ICreature summoner, Ticker timeLeft, AIRunner aiRunner,
            CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        super(builder.build(aiRunner, null, statblockManager, conversationManager), builder.getSummonState(), summoner,
                timeLeft);
        this.successor = successor;
    }

    @Override
    public long getMonsterNumber() {
        return this.wrapped.getMonsterNumber();
    }

}
