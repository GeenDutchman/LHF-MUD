package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.StatsOutMessage;

public class AggroHighwaterChooser implements ActionChooser {
    private static final double defaultValue = (double) 0.90;

    private final double weight;

    public AggroHighwaterChooser() {
        this.weight = defaultValue;
    }

    public AggroHighwaterChooser(double selectedWeight) {
        this.weight = selectedWeight > 0.0 ? selectedWeight : ActionChooser.MIN_VALUE;
    }

    @Override
    public SortedMap<String, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories, Set<CreatureFaction> targetFactions) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories == null || battleMemories.isEmpty()) {
            return results;
        }
        if (battleMemories.get().getRecords().stream()
                .filter(stat -> harmMemories != null && harmMemories.getLastMassAttackerName().isPresent()
                        && stat.getTargetName().equals(harmMemories.getLastMassAttackerName().get()))
                .findAny().isPresent()) {
            results.put(harmMemories.getLastMassAttackerName().get(), this.weight);
        }
        return results;
    }

}
