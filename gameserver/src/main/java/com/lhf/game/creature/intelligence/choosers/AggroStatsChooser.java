package com.lhf.game.creature.intelligence.choosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStats;

public class AggroStatsChooser implements TargetChooser {

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStats> stats = battleMemories.getBattleStats();
        if (stats != null) {
            float max = 0;
            float min = 0;
            for (BattleStats stat : stats.values()) {
                if (stat.getAggroDamage() > max) {
                    max = stat.getAggroDamage();
                }
                if (stat.getAggroDamage() < min) {
                    min = stat.getAggroDamage();
                }
            }
            for (BattleStats stat : stats.values()) {
                float calculated = (stat.getAggroDamage() - min) / (max - min);
                results.put(stat.getTargetName(), calculated);
            }
        }
        return results;
    }

}
