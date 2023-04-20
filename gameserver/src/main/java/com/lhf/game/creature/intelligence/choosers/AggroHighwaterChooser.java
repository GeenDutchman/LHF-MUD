package com.lhf.game.creature.intelligence.choosers;

import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.enums.CreatureFaction;

public class AggroHighwaterChooser implements TargetChooser {
    private static final float defaultValue = (float) 0.90;

    private final float weight;

    public AggroHighwaterChooser() {
        this.weight = defaultValue;
    }

    public AggroHighwaterChooser(float selectedWeight) {
        this.weight = selectedWeight > 0.0f ? selectedWeight : 0.01f;
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        if (battleMemories.getLastAttaker() != null) {
            results.put(battleMemories.getLastAttaker().getName(), this.weight);
        }
        return results;
    }

}
