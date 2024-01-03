package com.lhf.game.creature;

import java.io.FileNotFoundException;

import com.lhf.game.creature.NonPlayerCharacter.NPCBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.interfaces.NotNull;

public abstract class WrappedNPC extends WrappedINonPlayerCharacter<NonPlayerCharacter> {

    /**
     * Note that this can mask a Monster
     */
    protected WrappedNPC(@NotNull NonPlayerCharacter npc) {
        super(npc);
    }

    /**
     * Wraps a freshly created NPC
     * 
     * @throws FileNotFoundException
     */
    protected WrappedNPC(@NotNull NPCBuilder builder, AIRunner aiRunner, CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        super(builder.build(aiRunner, null, statblockManager, conversationManager));
        this.successor = successor;
    }

}