package com.lhf.game.creature.intelligence.choosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStats;

public class AggroStatsChooser implements TargetChooser {

    @Override
    public SortedMap<String, Integer> chooseTarget(BattleMemories battleMemories) {
        SortedMap<String, Integer> results = new TreeMap<>();
        Map<String, BattleStats> stats = battleMemories.getBattleStats();
        if (stats != null) {
            int sum = 0;
        }
        return results;
    }

}
