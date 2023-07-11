package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD100;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.StatsOutMessage;

public class RandomTargetChooser implements AIChooser<String> {
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
    public SortedMap<String, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories, Set<CreatureFaction> targetFactions, Collection<OutMessage> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories.isPresent()) {
            for (BattleStatRecord stat : battleMemories.get().getRecords()) {
                if (targetFactions == null || targetFactions.contains(stat.getFaction())) {
                    results.put(stat.getTargetName(),
                            (double) roller.rollDice().getRoll() / roller.getType().getType());
                }
            }
        }
        return results;
    }

}
