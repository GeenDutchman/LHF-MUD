package com.lhf.game.magic;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoSafe;

public class CreatureAOESpellEntry_AutoSafeTest {
    @Test
    void testToString() {
        AutoSafe autoSafe = new AutoSafe();
        String safe = autoSafe.toString();
        Truth.assertThat(safe).contains("On a basic cast");
        Truth.assertThat(safe).contains("all");
        Truth.assertThat(safe).doesNotContain("NPC");
        Truth.assertThat(safe).contains("will be affected.");
    }
}
