package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.StatsOutMessage;

public class VocationChooser implements AIChooser<String> {

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
    public SortedMap<String, Double> chooseTarget(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories, Set<CreatureFaction> targetFactions, Collection<OutMessage> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories != null && battleMemories.isPresent()) {
            for (BattleStatRecord stat : battleMemories.get().getRecords()) {
                double priority = AIChooser.MIN_VALUE;
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
