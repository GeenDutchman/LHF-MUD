package com.lhf.game.creature.intelligence.choosers;

import java.util.SortedMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;

public interface TargetChooser {
    public SortedMap<String, Integer> chooseTarget(BattleMemories battleMemories);
}
