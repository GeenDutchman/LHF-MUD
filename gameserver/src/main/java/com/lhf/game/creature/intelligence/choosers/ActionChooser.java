package com.lhf.game.creature.intelligence.choosers;

import java.util.SortedMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.enums.CreatureFaction;

public interface ActionChooser {
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction);
}
