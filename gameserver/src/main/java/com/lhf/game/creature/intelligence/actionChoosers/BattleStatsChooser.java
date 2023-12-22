package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.battle.BattleStats.BattleStatRecord.BattleStat;
import com.lhf.game.creature.INonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.messages.out.GameEvent;

public class BattleStatsChooser implements AIChooser<String> {

    private final BattleStat keyStat;

    public BattleStatsChooser() {
        this.keyStat = BattleStat.AGGRO_DAMAGE;
    }

    public BattleStatsChooser(BattleStat stat) {
        this.keyStat = stat == null ? BattleStat.AGGRO_DAMAGE : stat;
    }

    @Override
    public SortedMap<String, Double> choose(Set<BattleStatRecord> battleMemories,
            HarmMemories harmMemories, Collection<GameEvent> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories != null && battleMemories.size() > 0) {
            IntSummaryStatistics intStats = battleMemories.stream()
                    .filter(stat -> stat != null)
                    .collect(Collectors.summarizingInt(stat -> stat.get(this.keyStat)));

            battleMemories.stream()
                    .filter(stat -> stat != null)
                    .forEach(stat -> {
                        double calculated = (intStats.getMin() == intStats.getMax()) ? AIChooser.MIN_VALUE
                                : (stat.get(this.keyStat) - intStats.getMin())
                                        / (intStats.getMax() - intStats.getMin());
                        results.put(stat.getTargetName(), Math.max(AIChooser.MIN_VALUE, calculated));
                    });

        }
        return results;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyStat);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BattleStatsChooser))
            return false;
        BattleStatsChooser other = (BattleStatsChooser) obj;
        return keyStat == other.keyStat;
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
        if (!(arg0 instanceof BattleStatsChooser)) {
            return 1;
        }
        BattleStatsChooser other = (BattleStatsChooser) arg0;
        return this.keyStat.compareTo(other.keyStat);
    }

}
