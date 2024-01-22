package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class Monster extends NonPlayerCharacter implements IMonster {
    private final long monsterNumber;

    public static Monster buildMonster(MonsterBuilder builder,
            CommandInvoker controller, CommandChainHandler successor,
            Statblock statblock, ConversationTree converstionTree,
            UnaryOperator<Monster> transformer) {
        Monster made = new Monster(builder, controller, successor, statblock,
                converstionTree);
        if (transformer != null) {
            made = transformer.apply(made);
        }
        return made;
    }

    protected Monster(IMonsterBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock, ConversationTree conversationTree) {
        super(builder, controller, successor, statblock, conversationTree);
        this.monsterNumber = builder.getSerialNumber();
    }

    public static MonsterBuilder getMonsterBuilder() {
        return new MonsterBuilder();
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
