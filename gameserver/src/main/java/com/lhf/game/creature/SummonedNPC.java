package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumSet;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo.SummonData;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.messages.CommandChainHandler;

public class SummonedNPC extends SummonedINonPlayerCharacter<NonPlayerCharacter> {

    public SummonedNPC(NonPlayerCharacter NPC, EnumSet<SummonData> summonData, ICreature summoner, Ticker timeLeft) {
        super(NPC, summonData, summoner, timeLeft);
    }

    public static SummonedNPC fromBuildInfo(INPCBuildInfo builder, ICreature summoner, Ticker timeLeft,
            AIRunner aiRunner,
            CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        CreatureFactory factory = new CreatureFactory(successor, statblockManager, conversationManager, aiRunner, false,
                false);
        factory.visit(builder);
        return new SummonedNPC(factory.getBuiltCreatures().getNpcs().first(), builder.getSummonState(),
                summoner, timeLeft);
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

}
