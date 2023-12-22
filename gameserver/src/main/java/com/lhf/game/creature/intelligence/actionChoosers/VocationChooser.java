package com.lhf.game.creature.intelligence.actionChoosers;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.INonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.messages.events.GameEvent;

public class VocationChooser implements AIChooser<String> {

    public static final List<VocationName> DEFAULT_VOCATION_ORDER = List.of(VocationName.HEALER, VocationName.MAGE,
            VocationName.FIGHTER);
    private final List<VocationName> targetOrder;

    public VocationChooser() {
        this.targetOrder = VocationChooser.DEFAULT_VOCATION_ORDER;
    }

    public VocationChooser(List<VocationName> vocationOrder) {
        if (vocationOrder != null) {
            this.targetOrder = List.copyOf(vocationOrder);
        } else {
            this.targetOrder = VocationChooser.DEFAULT_VOCATION_ORDER;
        }
    }

    @Override
    public SortedMap<String, Double> choose(Set<BattleStatRecord> battleMemories,
            HarmMemories harmMemories, Collection<GameEvent> outMessages) {
        SortedMap<String, Double> results = new TreeMap<>();
        if (battleMemories != null && battleMemories.size() > 0) {
            for (BattleStatRecord stat : battleMemories) {
                double priority = AIChooser.MIN_VALUE;
                for (int i = 0; i < this.targetOrder.size(); i++) {
                    VocationName name = this.targetOrder.get(i);
                    if (name != null && stat.getVocation() != null
                            && name.equals(stat.getVocation().getVocationName())) {
                        priority = (this.targetOrder.size() - i) / (double) this.targetOrder.size();
                        break;
                    }
                }
                results.put(stat.getTargetName(), priority);
            }
        }
        return results;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof VocationChooser))
            return false;
        VocationChooser other = (VocationChooser) obj;
        return Objects.equals(targetOrder, other.targetOrder);
    }

    @Override
    public int compareTo(AIChooser<String> arg0) {
        if (this == arg0) {
            return 0;
        }
        int superComparison = AIChooser.super.compareTo(arg0);
        if (superComparison != 0) {
            return superComparison;
        }
        if (!(arg0 instanceof VocationChooser)) {
            return 1;
        }
        VocationChooser other = (VocationChooser) arg0;

        int lenDiff = this.targetOrder.size() - other.targetOrder.size(); // > 0 if this is bigger than other
        if (lenDiff != 0) {
            return lenDiff;
        }

        // for each name
        for (int iName = 0; iName < this.targetOrder.size(); iName++) {
            VocationName mine = this.targetOrder.get(iName);
            VocationName theirs = other.targetOrder.get(iName);
            if (mine != null && theirs != null) {
                int comparison = mine.compareTo(theirs);
                if (comparison != 0) {
                    return comparison;
                }
            } else if (mine != null && theirs == null) {
                return -1;
            } else if (mine == null && theirs != null) {
                return 1;
            } // else both null -> continue
        }

        return 0;

    }

}
