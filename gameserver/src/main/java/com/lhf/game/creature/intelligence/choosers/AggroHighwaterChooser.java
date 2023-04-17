package com.lhf.game.creature.intelligence.choosers;

import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;

public class AggroHighwaterChooser implements TargetChooser {
    private static final int defaultValue = 90;

    private final int weight;

    public AggroHighwaterChooser() {
        this.weight = defaultValue;
    }

    public AggroHighwaterChooser(int selectedWeight) {
        this.weight = selectedWeight;
    }

    @Override
    public SortedMap<String, Integer> chooseTarget(BattleMemories battleMemories) {
        SortedMap<String, Integer> results = new TreeMap<>();
        if (battleMemories.getLastAttaker() != null) {
            results.put(battleMemories.getLastAttaker().getName(), this.weight);
        }
        return results;
    }

}
