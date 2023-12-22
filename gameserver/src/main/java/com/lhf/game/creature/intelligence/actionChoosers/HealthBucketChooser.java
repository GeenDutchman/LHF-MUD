package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.INonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.out.GameEvent;

public class HealthBucketChooser implements AIChooser<String> {
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
            return AIChooser.MIN_VALUE;
        }
        if (this.threshold != null) {
            if (this.chooseMoreHurt && retrieved.compareTo(this.threshold) > 0) {
                return AIChooser.MIN_VALUE;
            } else if (!this.chooseMoreHurt && retrieved.compareTo(this.threshold) < 0) {
                return AIChooser.MIN_VALUE;
            }
        }
        return this.chooseMoreHurt ? AIChooser.MIN_VALUE - retrieved.getValue() : retrieved.getValue();
    }

    @Override
    public SortedMap<String, Double> choose(Set<BattleStatRecord> battleMemories,
            HarmMemories harmMemories, Collection<GameEvent> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories == null || battleMemories.isEmpty()) {
            return results;
        }

        for (BattleStatRecord stat : battleMemories) {
            results.put(stat.getTargetName(), this.calculate(stat));
        }
        return results;
    }

}
