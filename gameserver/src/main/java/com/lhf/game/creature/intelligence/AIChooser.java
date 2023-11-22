package com.lhf.game.creature.intelligence;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.events.messages.out.OutMessage;

public interface AIChooser<T extends Comparable<T>> extends Comparable<AIChooser<T>> {
    public static double MIN_VALUE = 0.01;

    public SortedMap<T, Double> choose(Set<BattleStatRecord> battleMemories,
            HarmMemories harmMemories,
            Collection<OutMessage> outMessages);

    @Override
    public default int compareTo(AIChooser<T> arg0) {
        if (arg0 == null) {
            return 1;
        }
        return this.getClass().getName().compareTo(arg0.getClass().getName());
    }

}
