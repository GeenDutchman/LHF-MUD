package com.lhf.game.creature;

import java.util.Objects;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class Monster extends NonPlayerCharacter implements IMonster {
    private final long monsterNumber;

    protected Monster(MonsterBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            ConversationTree conversationTree) {
        super(builder, controller, successor, conversationTree);
        this.monsterNumber = builder.getSerialNumber();
    }

    public static MonsterBuildInfo getMonsterBuilder() {
        return new MonsterBuildInfo();
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        IMonster monster = (IMonster) o;
        return monsterNumber == monster.getMonsterNumber();
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }

    @Override
    public long getMonsterNumber() {
        return this.monsterNumber;
    }

}
