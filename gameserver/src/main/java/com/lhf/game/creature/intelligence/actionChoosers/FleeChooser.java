package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStatRecord;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD100;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.map.Directions;

public class FleeChooser implements ActionChooser {
    private final Dice roller = new DiceD100(1);
    private final String watchingHealth;
    private final HealthBuckets fleeLevel;

    public FleeChooser(String watchingHealth, HealthBuckets fleeLevel) {
        this.watchingHealth = watchingHealth;
        this.fleeLevel = fleeLevel;
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStatRecord> stats = battleMemories.getBattleStats();

        if (this.watchingHealth != null && this.fleeLevel != null && stats != null
                && stats.containsKey(this.watchingHealth)) {
            HealthBuckets watched = stats.get(this.watchingHealth).getBucket();
            if (watched != null && watched.compareTo(this.fleeLevel) < 0) {
                for (Directions dir : Directions.values()) {
                    results.put(dir.toString(), (float) roller.rollDice().getRoll() / roller.getType().getType());
                }
                results.replace(Directions.UP.toString(),
                        results.get(Directions.UP.toString()) / Directions.values().length);
                results.replace(Directions.DOWN.toString(),
                        results.get(Directions.DOWN.toString()) / Directions.values().length);

            }
        }

        return results;
    }

}
