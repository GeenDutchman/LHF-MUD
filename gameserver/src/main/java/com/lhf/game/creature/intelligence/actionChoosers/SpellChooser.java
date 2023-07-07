package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.CommandContext;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpellEntryMessage;
import com.lhf.messages.out.StatsOutMessage;

public class SpellChooser implements ActionChooser {
    private final boolean offensiveFocus;

    public SpellChooser(boolean offensiveFocus) {
        this.offensiveFocus = offensiveFocus;
    }

    private SortedMap<String, Double> offensiveChoice(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, SpellEntryMessage entries) {
        NavigableSet<SpellEntry> offEntries = entries.getEntries().stream().filter(entry -> entry.isOffensive())
                .collect(Collectors.toCollection(TreeSet::new));
        SortedMap<String, Double> selection = new TreeMap<>();
        if (!offEntries.isEmpty()) {

        }
        return selection;
    }

    private SortedMap<String, Double> notOffensiveChoice(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, SpellEntryMessage entries) {
        return new TreeMap<>();
    }

    @Override
    public SortedMap<String, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, Collection<OutMessage> outMessages) {
        SortedMap<String, Double> selection = new TreeMap<>();
        if (outMessages == null) {
            return selection;
        }
        Optional<SpellEntryMessage> sem = outMessages.stream()
                .filter(message -> message instanceof SpellEntryMessage).map(message -> (SpellEntryMessage) message)
                .findAny();
        if (sem.isEmpty() || sem.get().getEntries().isEmpty()) {
            return selection;
        }
        selection.putAll(this.notOffensiveChoice(battleMemories, harmMemories, targetFactions, sem.get()));
        selection.putAll(this.offensiveChoice(battleMemories, harmMemories, targetFactions, sem.get()));
        return selection;
    }

}
