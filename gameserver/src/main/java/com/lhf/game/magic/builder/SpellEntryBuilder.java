package com.lhf.game.magic.builder;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.game.magic.SpellEntry;

public class SpellEntryBuilder {
    public interface SpellEntryBuilderAdapter extends Closeable {
        public int menuChoice(List<String> choices);

        public void stepSucceeded(boolean succeeded);

        public Boolean yesOrNo();

        public void close();

        public String buildName();

        public int buildLevel();

        public String buildInvocation(String name);

        public String buildDescription(String name);

        public Set<VocationName> buildVocations();

        public EffectPersistence buildEffectPersistence();

        public Set<CreatureEffectSource> buildCreatureEffectSources();

        public boolean buildSingleTarget();

    }

    private static SpellEntry makeSpellEntry(SpellEntryBuilderAdapter adapter) {
        String name = adapter.buildName();
        if (name == null || name.isBlank()) {
            throw new NullPointerException("Cannot build with an empty name!");
        }
        name = name.trim();
        int level = adapter.buildLevel();
        String invocation = adapter.buildInvocation(name);
        String description = adapter.buildDescription(name);
        Set<VocationName> allowed = adapter.buildVocations();

        switch (adapter.menuChoice(List.of("creaturetarget", "room", "dungeon"))) {
            case 0:
                boolean singleTarget = adapter.buildSingleTarget();
                Set<CreatureEffectSource> effectSources = adapter.buildCreatureEffectSources();
                return new CreatureTargetingSpellEntry(level, name, invocation, effectSources, allowed, description,
                        singleTarget);

            default:
                throw new IllegalStateException("Cannot build other types of spells!  For now anyway...");
        }

    }

    public static void main(String[] args) {
        SpellEntryBuilderAdapter adapter = new CLIAdapter();
        System.out.println(SpellEntryBuilder.makeSpellEntry(adapter));
    }
}
