package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStats;
import com.lhf.game.enums.CreatureFaction;

public class AggroStatsChooser implements ActionChooser {

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStats> stats = battleMemories.getBattleStats();
        if (stats != null) {
            float max = 0;
            float min = 0;
            for (BattleStats stat : stats.values()) {
                if (myFaction == null || myFaction.competing(stat.getFaction())) {
                    if (stat.getAggroDamage() > max) {
                        max = stat.getAggroDamage();
                    }
                    if (stat.getAggroDamage() < min) {
                        min = stat.getAggroDamage();
                    }
                }
            }
            for (BattleStats stat : stats.values()) {
                if (myFaction == null || myFaction.competing(stat.getFaction())) {
                    float calculated = (stat.getAggroDamage() - min) / (max - min);
                    results.put(stat.getTargetName(), calculated > 0.0f ? calculated : ActionChooser.MIN_VALUE);
                }
            }
        }
        return results;
    }

}
