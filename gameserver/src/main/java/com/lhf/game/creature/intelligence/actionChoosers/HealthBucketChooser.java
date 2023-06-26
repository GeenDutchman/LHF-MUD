package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;

public class HealthBucketChooser implements ActionChooser {
    private final boolean aimEnemies;
    private final boolean chooseMoreHurt;
    private final HealthBuckets threshold;

    public HealthBucketChooser() {
        this.aimEnemies = true;
        this.chooseMoreHurt = true;
        this.threshold = null;
    }

    public HealthBucketChooser(boolean aimEnemies) {
        this.aimEnemies = aimEnemies;
        this.chooseMoreHurt = true;
        this.threshold = null;
    }

    public HealthBucketChooser(boolean aimEnemies, boolean chooseMoreHurt, HealthBuckets threshold) {
        this.aimEnemies = aimEnemies;
        this.chooseMoreHurt = chooseMoreHurt;
        this.threshold = threshold;
    }

    private Float calculate(BattleStatRecord stat) {
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
    public SortedMap<String, Float> chooseTarget(BattleStats battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStatRecord> stats = battleMemories.getBattleStats();

        for (BattleStatRecord stat : stats.values()) {
            if (this.aimEnemies && (myFaction == null || myFaction.competing(stat.getFaction()))) {
                results.put(stat.getTargetName(), this.calculate(stat));
            } else if (!this.aimEnemies && myFaction != null && !myFaction.competing(stat.getFaction())) {
                results.put(stat.getTargetName(), this.calculate(stat));
            }
        }
        return results;
    }

}
