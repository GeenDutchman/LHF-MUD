package com.lhf.game.creature.intelligence.choosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStats;
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

    private Float calculate(BattleStats stat) {
        HealthBuckets retrieved = stat.getBucket();
        if (retrieved == null || HealthBuckets.DEAD.equals(retrieved)) {
            return 0.01f;
        }
        if (this.threshold != null) {
            if (this.chooseMoreHurt && retrieved.compareTo(this.threshold) > 0) {
                return 0.01f;
            } else if (!this.chooseMoreHurt && retrieved.compareTo(this.threshold) < 0) {
                return 0.01f;
            }
        }
        return this.chooseMoreHurt ? 1.0f - retrieved.getValue() : retrieved.getValue();
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStats> stats = battleMemories.getBattleStats();

        for (BattleStats stat : stats.values()) {
            if (this.aimEnemies && (myFaction == null || myFaction.competing(stat.getFaction()))) {
                results.put(stat.getTargetName(), this.calculate(stat));
            } else if (!this.aimEnemies && myFaction != null && !myFaction.competing(stat.getFaction())) {
                results.put(stat.getTargetName(), this.calculate(stat));
            }
        }
        return results;
    }

}
