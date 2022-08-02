package com.lhf.game.map;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class DungeonBuilderTest {
    @Test
    void testBuildStaticDungeon() throws FileNotFoundException {
        Dungeon built = DungeonBuilder.buildStaticDungeon(null);
        String mermaid = built.toMermaid(false);
        System.out.println(mermaid);
        Truth.assertThat(mermaid).isNotEmpty();
        Truth.assertThat(mermaid).ignoringCase().contains("Armory");
    }
}
