package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumSet;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo.SummonData;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.messages.CommandChainHandler;

public class SummonedMonster extends SummonedINonPlayerCharacter<Monster> implements IMonster {

    public SummonedMonster(Monster monster, EnumSet<SummonData> summonData, ICreature summoner, Ticker timeLeft) {
        super(monster, summonData, summoner, timeLeft);
    }

    public static SummonedMonster fromBuildInfo(MonsterBuildInfo builder, ICreature summoner, Ticker timeLeft,
            AIRunner aiRunner,
            CommandChainHandler successor,
            ConversationManager conversationManager) throws FileNotFoundException {
        CreatureFactory factory = CreatureFactory.fromAIRunner(successor, conversationManager,
                aiRunner, false);
        factory.visit(builder);
        return new SummonedMonster(factory.getBuiltCreatures().getMonsters().first(), builder.getSummonState(),
                summoner, timeLeft);
    }

    @Override
    public long getMonsterNumber() {
        return this.wrapped.getMonsterNumber();
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

}
