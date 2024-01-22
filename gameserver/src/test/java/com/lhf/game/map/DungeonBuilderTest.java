package com.lhf.game.map;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.map.Dungeon.DungeonBuilder;

public class DungeonBuilderTest {
    @Test
    void testBuildStaticDungeon() throws FileNotFoundException {
        AIRunner aiRunner = new GroupAIRunner(true);
        StatblockManager statblockManager = new StatblockManager();
        ConversationManager conversationManager = new ConversationManager();

        DungeonBuilder builder = StandardDungeonProducer.buildStaticDungeonBuilder(statblockManager);
        String builderMermaid = builder.toMermaid(false);
        System.out.println(builderMermaid);
        Truth.assertThat(builderMermaid).isNotEmpty();
        Truth.assertThat(builderMermaid).ignoringCase().contains("Armory");

        Dungeon built = builder.build(null, aiRunner, statblockManager, conversationManager, false, false);
        String mermaid = built.toMermaid(false);
        System.out.println(mermaid);
        Truth.assertThat(mermaid).isNotEmpty();
        Truth.assertThat(mermaid).ignoringCase().contains("Armory");
    }
}
