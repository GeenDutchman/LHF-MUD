package com.lhf.game.creature.intelligence.choosers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories.BattleStats;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD100;

public class RandomTargetChooser implements TargetChooser {
    private final Dice roller;

    public RandomTargetChooser() {
        this.roller = new DiceD100(1);
    }

    public RandomTargetChooser(Dice aRoller) {
        if (aRoller == null) {
            this.roller = new DiceD100(1);
        } else {
            this.roller = aRoller;
        }
    }

    @Override
    public SortedMap<String, Integer> chooseTarget(BattleMemories battleMemories) {
        SortedMap<String, Integer> results = new TreeMap<>();
        Map<String, BattleStats> stats = battleMemories.getStats();
        if (stats != null) {
            for (BattleStats stat : stats.values()) {
                results.put(stat.getTargetName(), roller.rollDice().getRoll());
            }
        }
        return results;
    }

}
