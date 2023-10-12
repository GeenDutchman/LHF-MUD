package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Objects;
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
    public SortedMap<String, Double> choose(Optional<StatsOutMessage> battleMemories,
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

    @Override
    public int hashCode() {
        return Objects.hash(roller);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RandomTargetChooser))
            return false;
        RandomTargetChooser other = (RandomTargetChooser) obj;
        return Objects.equals(roller, other.roller);
    }

    @Override
    public int compareTo(AIChooser<String> arg0) {
        if (this == arg0) {
            return 0;
        }
        int compareResult = AIChooser.super.compareTo(arg0);
        if (compareResult != 0) {
            return compareResult;
        }
        if (!(arg0 instanceof RandomTargetChooser)) {
            return 1;
        }
        RandomTargetChooser other = (RandomTargetChooser) arg0;

        return this.roller.compareTo(other.roller);
    }

}
