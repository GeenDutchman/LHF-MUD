package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpellEntryMessage;
import com.lhf.messages.out.StatsOutMessage;

public class SpellChooser implements ActionChooser {
    private final boolean offensiveFocus;

    public SpellChooser(boolean offensiveFocus) {
        this.offensiveFocus = offensiveFocus;
    }

    private HealthBuckets factionHealth(StatsOutMessage battleMemories, Set<CreatureFaction> targetFactions) {
        if (targetFactions == null || targetFactions.isEmpty() || battleMemories == null) {
            return null;
        }
        DoubleSummaryStatistics sum = battleMemories.getRecords().stream()
                .filter(statRecord -> statRecord != null && statRecord.getBucket() != null
                        && targetFactions.contains(statRecord.getFaction()))
                .map(statRecord -> statRecord.getBucket())
                .collect(Collectors.summarizingDouble(HealthBuckets::getValue));

        if (sum.getCount() < 1) {
            return null;
        }
        HealthBuckets avgBucket = HealthBuckets.fromPercent(sum.getAverage());
        return avgBucket;
    }

    private SortedMap<String, Double> offensiveChoice(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, SpellEntryMessage entries) {
        SortedMap<String, Double> selection = entries.getEntries().stream()
                .filter(entry -> entry != null && entry.isOffensive())
                .collect(Collectors.toMap(SpellEntry::getInvocation,
                        spellentry -> (double) (spellentry.aiScore() / entries.getEntries().size()),
                        (scorea, scoreb) -> (scorea + scoreb) / 2, TreeMap::new));
        if (selection.isEmpty()) {
            return selection;
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
        if (outMessages == null || battleMemories == null || battleMemories.isEmpty() || battleMemories.get() == null) {
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
