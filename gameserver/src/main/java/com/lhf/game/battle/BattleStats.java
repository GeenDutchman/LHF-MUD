package com.lhf.game.battle;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.out.CreatureAffectedMessage;

public class BattleStats {
    /**
     *
     */
    public static class BattleStatRecord implements Comparable<BattleStatRecord> {
        protected final String targetName;
        private CreatureFaction faction;
        private Vocation vocation;
        private HealthBuckets bucket;
        private int maxDamage;
        private int aggroDamage;
        private int totalDamage;
        private int numDamgages;
        private int healingPerformed;

        public BattleStatRecord(String targetName, CreatureFaction faction, Vocation vocation,
                HealthBuckets bucket) {
            this.targetName = targetName;
            this.faction = faction;
            this.vocation = vocation;
            this.bucket = bucket;
        }

        public String getTargetName() {
            return targetName;
        }

        public CreatureFaction getFaction() {
            return faction;
        }

        public Vocation getVocation() {
            return vocation;
        }

        public HealthBuckets getBucket() {
            return bucket;
        }

        public int getMaxDamage() {
            return maxDamage;
        }

        public int getAggroDamage() {
            return aggroDamage;
        }

        public int getTotalDamage() {
            return totalDamage;
        }

        public int getAverageDamage() {
            return this.getTotalDamage() / this.getNumDamgages();
        }

        public int getNumDamgages() {
            return numDamgages;
        }

        public int getHealingPerformed() {
            return healingPerformed;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BattleStats [targetName=").append(targetName).append(", faction=").append(faction)
                    .append(", vocation=").append(vocation).append(", bucket=").append(bucket)
                    .append(", maxDamage=").append(maxDamage).append(", aggroDamage=").append(aggroDamage)
                    .append(", totalDamage=").append(totalDamage).append(", numDamgages=").append(numDamgages)
                    .append(", healingPerformed=").append(healingPerformed).append("]");
            return builder.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hash(targetName);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BattleStatRecord)) {
                return false;
            }
            BattleStatRecord other = (BattleStatRecord) obj;
            return Objects.equals(targetName, other.targetName);
        }

        @Override
        public int compareTo(BattleStatRecord arg0) {
            return this.targetName.compareTo(arg0.targetName);
        }

    }

    protected Map<String, BattleStatRecord> battleStats;

    public BattleStats() {
        this.battleStats = new TreeMap<>();
    }

    public BattleStats(Map<String, BattleStatRecord> seedStats) {
        this.battleStats = seedStats != null ? new TreeMap<>(seedStats) : new TreeMap<>();
    }

    // note that any duplicates will overwrite each other!
    public BattleStats(Iterable<BattleStatRecord> seedRecords) {
        this.battleStats = new TreeMap<>();
        if (seedRecords != null) {
            for (BattleStatRecord record : seedRecords) {
                this.battleStats.put(record.targetName, record);
            }
        }
    }

    public BattleStats reset() {
        this.battleStats.clear();
        return this;
    }

    public BattleStats update(CreatureAffectedMessage ca) {
        if (ca.getEffect() == null) {
            return this;
        }
        Creature responsible = ca.getEffect().creatureResponsible();
        if (responsible == null) {
            return this;
        }

        // int origRoll = ca.getEffect().getDamageResult().getOrigRoll();
        int roll = ca.getEffect().getDamageResult().getRoll();

        if (!this.battleStats.containsKey(responsible.getName())) {
            this.battleStats.put(responsible.getName(),
                    new BattleStatRecord(responsible.getName(), responsible.getFaction(),
                            responsible.getVocation(),
                            responsible.getHealthBucket()));
        }

        BattleStatRecord found = this.battleStats.get(responsible.getName());
        if (found == null) {
            return this;
        }
        found.bucket = responsible.getHealthBucket();
        found.numDamgages += roll < 0 ? 1 : 0;
        found.totalDamage += roll;
        found.maxDamage = roll > found.maxDamage ? roll : found.maxDamage;
        found.healingPerformed += ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.HEALING),
                false);
        found.aggroDamage = ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.AGGRO), true);

        return this;
    }

    public BattleStats initialize(Iterable<Creature> creatures) {
        for (Creature creature : creatures) {
            if (creature != null) {
                if (!this.battleStats.containsKey(creature.getName())) {
                    this.battleStats.put(creature.getName(),
                            new BattleStatRecord(creature.getName(), creature.getFaction(),
                                    creature.getVocation(),
                                    creature.getHealthBucket()));
                } else {
                    BattleStatRecord found = this.battleStats.get(creature.getName());
                    found.faction = creature.getFaction(); // update just in case
                    found.bucket = creature.getHealthBucket();
                    found.vocation = creature.getVocation();
                }
            }
        }
        return this;
    }

    public boolean contains(String creatureName) {
        return this.battleStats.containsKey(creatureName);
    }

    public BattleStats remove(String creatureName) {
        this.battleStats.remove(creatureName);
        return this;
    }

    public Map<String, BattleStatRecord> getBattleStats() {
        return Collections.unmodifiableMap(this.battleStats);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BattleStats [battleStats=").append(battleStats != null ? battleStats.values() : null)
                .append("]");
        return builder.toString();
    }

}