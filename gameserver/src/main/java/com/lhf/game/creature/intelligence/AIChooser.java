package com.lhf.game.creature.intelligence;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;

import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.StatsOutMessage;

public interface AIChooser<T extends Comparable<T>> extends Comparable<AIChooser<T>> {
    public static double MIN_VALUE = 0.01;

    public SortedMap<T, Double> choose(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            Set<CreatureFaction> targetFactions, Collection<OutMessage> outMessages);

    @Override
    public default int compareTo(AIChooser<T> arg0) {
        if (arg0 == null) {
            return 1;
        }
        return this.getClass().getName().compareTo(arg0.getClass().getName());
    }

}
