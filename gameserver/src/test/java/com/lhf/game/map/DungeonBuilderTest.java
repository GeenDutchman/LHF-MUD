package com.lhf.game.map;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;

public class DungeonBuilderTest {
    @Test
    void testBuildStaticDungeon() throws FileNotFoundException {
        AIRunner aiRunner = new GroupAIRunner(true);
        Dungeon built = DungeonBuilder.buildStaticDungeon(null, aiRunner);
        String mermaid = built.toMermaid(false);
        System.out.println(mermaid);
        Truth.assertThat(mermaid).isNotEmpty();
        Truth.assertThat(mermaid).ignoringCase().contains("Armory");
    }
}
