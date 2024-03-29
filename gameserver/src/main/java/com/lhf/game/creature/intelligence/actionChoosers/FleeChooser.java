package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.INonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD100;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.map.Directions;
import com.lhf.messages.events.GameEvent;

public class FleeChooser implements AIChooser<String> {
    private final Dice roller = new DiceD100(1);
    private final HealthBuckets fleeLevel;

    public FleeChooser(HealthBuckets fleeLevel) {
        this.fleeLevel = fleeLevel != null ? fleeLevel : HealthBuckets.CRITICALLY_INJURED;
    }

    @Override
    public SortedMap<String, Double> choose(Set<BattleStatRecord> battleMemories,
            HarmMemories harmMemories, Collection<GameEvent> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();

        if (harmMemories != null && this.fleeLevel != null && battleMemories != null && battleMemories.size() > 0) {
            Optional<HealthBuckets> watched = battleMemories.stream()
                    .filter(stat -> stat != null && harmMemories.getOwnerName().equals(stat.getTargetName()))
                    .map(stat -> stat.getBucket())
                    .findFirst();
            if (watched.isPresent() && watched.get().compareTo(this.fleeLevel) < 0) {
                for (Directions dir : Directions.values()) {
                    results.put(dir.toString(), (double) roller.rollDice().getRoll() / roller.getType().getType());
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
