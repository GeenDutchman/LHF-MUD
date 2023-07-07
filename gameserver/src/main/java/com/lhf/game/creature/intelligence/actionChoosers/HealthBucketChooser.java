package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.out.StatsOutMessage;

public class HealthBucketChooser implements ActionChooser {
    private final boolean chooseMoreHurt;
    private final HealthBuckets threshold;

    public HealthBucketChooser() {
        this.chooseMoreHurt = true;
        this.threshold = null;
    }

    public HealthBucketChooser(boolean chooseMoreHurt, HealthBuckets threshold) {
        this.chooseMoreHurt = chooseMoreHurt;
        this.threshold = threshold;
    }

    private Double calculate(BattleStatRecord stat) {
        HealthBuckets retrieved = stat.getBucket();
        if (retrieved == null || HealthBuckets.DEAD.equals(retrieved)) {
            return ActionChooser.MIN_VALUE;
        }
        if (this.threshold != null) {
            if (this.chooseMoreHurt && retrieved.compareTo(this.threshold) > 0) {
                return ActionChooser.MIN_VALUE;
            } else if (!this.chooseMoreHurt && retrieved.compareTo(this.threshold) < 0) {
                return ActionChooser.MIN_VALUE;
            }
        }
        return this.chooseMoreHurt ? ActionChooser.MIN_VALUE - retrieved.getValue() : retrieved.getValue();
    }

    @Override
    public SortedMap<String, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories, Set<CreatureFaction> targetFactions) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories.isEmpty()) {
            return results;
        }

        for (BattleStatRecord stat : battleMemories.get().getRecords()) {
            if (targetFactions == null || targetFactions.contains(stat.getFaction())) {
                results.put(stat.getTargetName(), this.calculate(stat));
            }
        }
        return results;
    }

}
