package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.events.messages.out.OutMessage;

public class AggroHighwaterChooser implements AIChooser<String> {
    private static final double defaultValue = (double) 0.90;

    private final double weight;

    public AggroHighwaterChooser() {
        this.weight = defaultValue;
    }

    public AggroHighwaterChooser(double selectedWeight) {
        this.weight = Math.min(Math.max(AIChooser.MIN_VALUE, selectedWeight), 1.0);
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public SortedMap<String, Double> choose(Set<BattleStatRecord> battleMemories,
            HarmMemories harmMemories, Collection<OutMessage> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories == null || battleMemories.isEmpty()) {
            return results;
        }
        for (BattleStatRecord stat : battleMemories) {
            double priority = AIChooser.MIN_VALUE;
            if (harmMemories != null && harmMemories.getLastMassAttackerName().isPresent()
                    && stat.getTargetName().equals(harmMemories.getLastMassAttackerName().get())) {
                priority = this.weight;
            }
            results.put(stat.getTargetName(), priority);
        }
        return results;
    }

}
