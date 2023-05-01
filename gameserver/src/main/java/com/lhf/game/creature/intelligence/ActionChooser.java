package com.lhf.game.creature.intelligence.choosers;

import java.util.SortedMap;

import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.enums.CreatureFaction;

public interface ActionChooser extends Comparable<ActionChooser> {
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction);

    @Override
    public default int compareTo(ActionChooser arg0) {
        if (arg0 == null) {
            return 1;
        }
        return this.getClass().getName().compareTo(arg0.getClass().getName());
    }

}
