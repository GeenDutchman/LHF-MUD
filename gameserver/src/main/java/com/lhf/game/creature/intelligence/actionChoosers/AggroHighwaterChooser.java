package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;

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
    public SortedMap<String, Double> chooseTarget(Optional<Collection<BattleStatRecord>> battleMemories,
            HarmMemories harmMemories, CreatureFaction myFaction) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (harmMemories != null && harmMemories.getLastMassAttackerName().isPresent()) {
            results.put(harmMemories.getLastMassAttackerName().get(), this.weight);
        }
        return results;
    }

}
