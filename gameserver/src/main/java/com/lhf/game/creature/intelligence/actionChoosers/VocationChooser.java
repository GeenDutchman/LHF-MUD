package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BattleMemories;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;

public class VocationChooser implements ActionChooser {

    private final List<VocationName> targetOrder;

    public VocationChooser() {
        this.targetOrder = List.of(VocationName.HEALER, VocationName.MAGE, VocationName.FIGHTER);
    }

    public VocationChooser(List<VocationName> vocationOrder) {
        if (vocationOrder != null) {
            this.targetOrder = List.copyOf(vocationOrder);
        } else {
            this.targetOrder = List.of(VocationName.HEALER, VocationName.MAGE, VocationName.FIGHTER);
        }
    }

    @Override
    public SortedMap<String, Float> chooseTarget(BattleMemories battleMemories, CreatureFaction myFaction) {
        SortedMap<String, Float> results = new TreeMap<>();
        Map<String, BattleStatRecord> stats = battleMemories.getBattleStats();
        if (stats != null) {
            for (BattleStatRecord stat : stats.values()) {
                float priority = ActionChooser.MIN_VALUE;
                for (int i = 0; i < this.targetOrder.size(); i++) {
                    VocationName name = this.targetOrder.get(i);
                    if (name != null && stat.getVocation() != null
                            && name.equals(stat.getVocation().getVocationName())) {
                        priority = (this.targetOrder.size() - i) / (float) this.targetOrder.size();
                        break;
                    }
                }
                results.put(stat.getTargetName(), priority);
            }
        }
        return results;
    }

}
