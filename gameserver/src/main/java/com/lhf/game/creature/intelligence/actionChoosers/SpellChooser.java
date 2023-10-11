package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.magic.SpellEntry;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpellEntryMessage;
import com.lhf.messages.out.StatsOutMessage;

public class SpellChooser implements AIChooser<SpellEntry> {
    private final double offensiveFocus;

    public SpellChooser(double offensiveFocus) {
        double temp = Math.max(AIChooser.MIN_VALUE, offensiveFocus);
        this.offensiveFocus = Math.min(1 - AIChooser.MIN_VALUE, temp);
    }

    private Map<SpellEntry, Double> offensiveBalanceSelection(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, SpellEntryMessage entries) {
        SortedMap<SpellEntry, Double> selection = entries.getEntries().stream()
                .filter(entry -> entry != null)
                .collect(Collectors.toMap(spellentry -> spellentry,
                        spellentry -> (spellentry.aiScore() / (spellentry.getLevel().toInt() + 0.1))
                                * (spellentry.isOffensive() ? this.offensiveFocus : 1 - this.offensiveFocus),
                        (scorea, scoreb) -> (scorea + scoreb) / 2, TreeMap::new));
        if (selection.isEmpty()) {
            return selection;
        }

        return selection;
    }

    @Override
    public SortedMap<SpellEntry, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, Collection<OutMessage> outMessages) {
        SortedMap<SpellEntry, Double> selection = new TreeMap<>();
        if (outMessages == null || battleMemories == null || battleMemories.isEmpty() || battleMemories.get() == null) {
            return selection;
        }
        Optional<SpellEntryMessage> sem = outMessages.stream()
                .filter(message -> message instanceof SpellEntryMessage).map(message -> (SpellEntryMessage) message)
                .findAny();
        if (sem.isEmpty() || sem.get().getEntries().isEmpty()) {
            return selection;
        }
        selection.putAll(this.offensiveBalanceSelection(battleMemories, harmMemories, targetFactions, sem.get()));
        return selection;
    }

}
