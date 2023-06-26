package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.CommandContext;
import com.lhf.messages.out.SpellEntryMessage;

public class SpellChooser implements ActionChooser {
    private final boolean offensiveFocus;

    public SpellChooser(boolean offensiveFocus) {
        this.offensiveFocus = offensiveFocus;
    }

    private SortedMap<String, Float> offensiveChoice(BattleMemories battleMemories, CreatureFaction myFaction,
            NavigableSet<SpellEntry> entries) {
        NavigableSet<SpellEntry> offEntries = entries.stream().filter(entry -> entry.isOffensive())
                .collect(Collectors.toCollection(TreeSet::new));
        SortedMap<String, Float> selection = new TreeMap<>();
        if (!offEntries.isEmpty()) {

        }
        return selection;
    }

    private SortedMap<String, Float> notOffensiveChoice(BattleMemories battleMemories, CreatureFaction myFaction,
            NavigableSet<SpellEntry> entries) {
        return new TreeMap<>();
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> selection = new TreeMap<>();
        CommandContext.Reply reply = battleMemories.launchCommand("SPELLBOOK");
        if (!reply.isHandled()) {
            return selection;
        }
        Optional<SpellEntryMessage> sem = reply.getMessages().stream()
                .filter(message -> message instanceof SpellEntryMessage).map(message -> (SpellEntryMessage) message)
                .findAny();
        if (sem.isEmpty() || sem.get().getEntries().isEmpty()) {
            return selection;
        }
        selection.putAll(this.notOffensiveChoice(battleMemories, myFaction, sem.get().getEntries()));
        selection.putAll(this.offensiveChoice(battleMemories, myFaction, sem.get()
                .getEntries()));
        return selection;
    }

}