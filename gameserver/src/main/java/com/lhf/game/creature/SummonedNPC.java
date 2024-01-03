package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumSet;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder.SummonData;
import com.lhf.game.creature.NonPlayerCharacter.NPCBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.messages.CommandChainHandler;

public class SummonedNPC extends SummonedINonPlayerCharacter<NonPlayerCharacter> {

    public SummonedNPC(NonPlayerCharacter NPC, EnumSet<SummonData> summonData, ICreature summoner, Ticker timeLeft) {
        super(NPC, summonData, summoner, timeLeft);
    }

    public SummonedNPC(NPCBuilder builder, ICreature summoner, Ticker timeLeft, AIRunner aiRunner,
            CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        super(builder.build(aiRunner, null, statblockManager, conversationManager), builder.getSummonState(), summoner,
                timeLeft);
        this.successor = successor;
    }

}
