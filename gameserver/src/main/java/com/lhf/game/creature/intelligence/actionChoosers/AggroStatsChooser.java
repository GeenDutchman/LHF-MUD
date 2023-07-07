package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.StatsOutMessage;

public class AggroStatsChooser implements ActionChooser {

    @Override
    public SortedMap<String, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories, Set<CreatureFaction> targetFactions) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories.isPresent() && battleMemories.get() != null) {
            float max = 0;
            float min = 0;
            for (BattleStatRecord stat : battleMemories.get().getRecords()) {
                if (harmMemories != null && harmMemories.getOwnerName().equals(stat.getTargetName())) {
                    continue;
                }
                if (targetFactions == null || targetFactions.contains(stat.getFaction())) {
                    if (stat.getAggroDamage() > max) {
                        max = stat.getAggroDamage();
                    }
                    if (stat.getAggroDamage() < min) {
                        min = stat.getAggroDamage();
                    }
                }
            }
            for (BattleStatRecord stat : battleMemories.get().getRecords()) {
                if (harmMemories != null && harmMemories.getOwnerName().equals(stat.getTargetName())) {
                    continue;
                }
                if (targetFactions == null || targetFactions.contains(stat.getFaction())) {
                    float calculated = (stat.getAggroDamage() - min) / (max - min);
                    results.put(stat.getTargetName(), calculated > 0.0f ? calculated : ActionChooser.MIN_VALUE);
                }
            }
        }
        return results;
    }

}
