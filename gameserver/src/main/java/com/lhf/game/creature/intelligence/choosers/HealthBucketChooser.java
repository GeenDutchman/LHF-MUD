package com.lhf.game.creature.intelligence.choosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStats;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;

public class HealthBucketChooser implements TargetChooser {
    private final boolean chooseMoreHurt;
    private final boolean aimEnemies;
    private final HealthBuckets threshold;

    public HealthBucketChooser(boolean aimForMoreHurt, boolean aimAtEnemies) {
        this.chooseMoreHurt = aimForMoreHurt;
        this.aimEnemies = aimAtEnemies;
        this.threshold = null;
    }

    public HealthBucketChooser(boolean chooseMoreHurt, boolean aimEnemies, HealthBuckets threshold) {
        this.chooseMoreHurt = chooseMoreHurt;
        this.aimEnemies = aimEnemies;
        this.threshold = threshold;
    }

    private Float calculate(BattleStats stat) {
        HealthBuckets retrieved = stat.getBucket();
        if (retrieved == null || HealthBuckets.DEAD.equals(retrieved)) {
            return 0.0f;
        }
        return this.chooseMoreHurt ? 1.0f - retrieved.getValue() : retrieved.getValue();
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStats> stats = battleMemories.getBattleStats();

        for (BattleStats stat : stats.values()) {
            if (this.aimEnemies && (myFaction != null || myFaction.competing(stat.getFaction())) {

            }
        }
    }

}
