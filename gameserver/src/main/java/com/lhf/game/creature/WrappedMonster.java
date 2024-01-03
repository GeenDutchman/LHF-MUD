package com.lhf.game.creature;

import java.io.FileNotFoundException;

import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.interfaces.NotNull;

public abstract class WrappedMonster extends WrappedINonPlayerCharacter<Monster> implements IMonster {

    protected WrappedMonster(@NotNull Monster monster) {
        super(monster);
    }

    protected WrappedMonster(@NotNull MonsterBuilder builder, AIRunner aiRunner, CommandChainHandler successor,
            StatblockManager statblockManager, ConversationManager conversationManager) throws FileNotFoundException {
        super(builder.build(aiRunner, null, statblockManager, conversationManager));
        this.successor = successor;
    }

}