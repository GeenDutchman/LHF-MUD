package com.lhf.game.magic.builder;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.CreatureAOESpellEntry;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoTargeted;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.game.magic.SpellEntry;
import com.lhf.game.magic.Spellbook;

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

        public EffectResistance buildEffectResistance();

        public AutoTargeted buildAutoSafe();

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

        Set<CreatureEffectSource> effectSources = null;

        switch (adapter.menuChoice(List.of("creaturetarget", "aoe", "room", "dungeon"))) {
            case 0:
                boolean singleTarget = adapter.buildSingleTarget();
                effectSources = adapter.buildCreatureEffectSources();
                return new CreatureTargetingSpellEntry(level, name, invocation, effectSources, allowed, description,
                        singleTarget);
            case 1:
                AutoTargeted safe = adapter.buildAutoSafe();
                effectSources = adapter.buildCreatureEffectSources();
                return new CreatureAOESpellEntry(level, name, invocation, effectSources, allowed, description, safe);
            default:
                throw new IllegalStateException("Cannot build other types of spells!  For now anyway...");
        }

    }

    private static void menu(CLIAdapter adapter) {
        int menuChoice = -1;
        Spellbook spellbook = new Spellbook();
        if (spellbook.loadFromFile()) {
            System.out.println("loaded from file");
        } else {
            System.out.println("Failed to load spellbook");
        }
        SpellEntry selected = null;
        do {
            System.out.println("Main menu:");
            System.out.println("What to do?");
            menuChoice = adapter.menuChoice(List.of("exit", "make", "print", "list", "add", "save"));
            System.out.println(menuChoice);
            switch (menuChoice) {
                case 0:
                    System.out.println("Exiting...");
                    return;
                case 1:
                    System.out.println("Making a spell entry...");
                    selected = SpellEntryBuilder.makeSpellEntry(adapter);
                    System.out.println(selected);
                    break;
                case 2:
                    if (selected != null) {
                        System.out.println("Selected:");
                        System.out.println(selected);
                    } else {
                        System.out.println("No spellentry selected or made");
                    }
                    break;
                case 3:
                    System.out.println("Printing spellbook:");
                    for (SpellEntry entry : spellbook.getEntries()) {
                        System.out.printf("%d %s\r\n", entry.getLevel(), entry.getName());
                        System.out.println(entry.printDescription());
                    }
                    break;
                case 4:
                    if (selected != null) {
                        System.out.println("Adding " + selected.getName());
                        if (!spellbook.addEntry(selected)) {
                            System.err.println("It was not added.");
                        } else {
                            System.out.println("Added, but not saved!");
                        }
                    } else {
                        System.out.println("No spellentry selected or made");
                    }
                    break;
                case 5:
                    System.out.println("Saving....");
                    try {
                        if (!spellbook.saveToFile()) {
                            System.err.println("It was not saved, but no exception");
                        }
                    } catch (Exception e) {
                        System.err.println("It was not saved.");
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Unrecognized option, repeating menu...");
                    break;
            }
        } while (menuChoice != 0);
    }

    public static void main(String[] args) {
        CLIAdapter adapter = new CLIAdapter();
        SpellEntryBuilder.menu(adapter);
    }
}
