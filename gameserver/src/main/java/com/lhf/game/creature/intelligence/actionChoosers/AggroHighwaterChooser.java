package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.enums.CreatureFaction;

public class AggroHighwaterChooser implements ActionChooser {
    private static final float defaultValue = (float) 0.90;

    private final float weight;

    public AggroHighwaterChooser() {
        this.weight = defaultValue;
    }

    public AggroHighwaterChooser(float selectedWeight) {
        this.weight = selectedWeight > 0.0f ? selectedWeight : ActionChooser.MIN_VALUE;
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        if (battleMemories.getLastAttakerName().isPresent()) {
            results.put(battleMemories.getLastAttakerName().get(), this.weight);
        }
        return results;
    }

}
