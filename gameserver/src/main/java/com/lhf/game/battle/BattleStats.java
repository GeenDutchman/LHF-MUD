package com.lhf.game.battle;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats.BattleStatRecord.BattleStat;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public class BattleStats implements ClientMessenger {
    /**
     *
     */
    public static class BattleStatRecord implements Comparable<BattleStatRecord> {

        public static enum BattleStat {
            MAX_DAMAGE, AGGRO_DAMAGE, TOTAL_DAMAGE, NUM_DAMAGES, HEALING_PERFORMED, AVG_DAMAGE;

            public static BattleStat getBattleStat(String value) {
                for (BattleStat stat : values()) {
                    if (stat.toString().equalsIgnoreCase(value)) {
                        return stat;
                    }
                }
                return null;
            }

            public static List<BattleStat> asList() {
                return List.of(values());
            }
        }

        protected final String targetName;
        private CreatureFaction faction;
        private Vocation vocation;
        private HealthBuckets bucket;
        private EnumMap<BattleStat, Integer> stats;
        private boolean dead;

        public BattleStatRecord(String targetName, CreatureFaction faction, Vocation vocation,
                HealthBuckets bucket) {
            this.targetName = targetName;
            this.faction = faction;
            this.vocation = vocation;
            this.bucket = bucket;
            this.stats = new EnumMap<>(BattleStat.class);
            for (BattleStat stat : BattleStat.values()) {
                this.stats.put(stat, 0);
            }
            this.dead = false;
        }

        // returns immutable map of stats
        public Map<BattleStat, Integer> getStats() {
            return Collections.unmodifiableMap(this.stats);
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
            return this.stats.getOrDefault(BattleStat.MAX_DAMAGE, 0);
        }

        public int getAggroDamage() {
            return this.stats.getOrDefault(BattleStat.AGGRO_DAMAGE, 0);
        }

        public int getTotalDamage() {
            return this.stats.getOrDefault(BattleStat.TOTAL_DAMAGE, 0);
        }

        public int getAverageDamage() {
            // return this.getTotalDamage() / this.getNumDamgages();
            return this.stats.getOrDefault(BattleStat.AVG_DAMAGE, 0);
        }

        public int getNumDamgages() {
            return this.stats.getOrDefault(BattleStat.NUM_DAMAGES, 0);
        }

        public int getHealingPerformed() {
            return this.stats.getOrDefault(BattleStat.HEALING_PERFORMED, 0);
        }

        public int get(BattleStat stat) {
            if (stat == null) {
                return 0;
            }
            return this.stats.getOrDefault(stat, 0);
        }

        public int get(String statString) {
            BattleStat stat = BattleStat.getBattleStat(statString);
            return this.get(stat);
        }

        public boolean isDead() {
            return this.dead;
        }

        public BattleStatRecord setDead() {
            this.dead = true;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BattleStats [targetName=").append(targetName)
                    .append(", dead=").append(this.dead)
                    .append(", faction=").append(faction)
                    .append(", vocation=").append(vocation)
                    .append(", bucket=").append(bucket);
            this.stats.entrySet().stream()
                    .forEach(entry -> builder.append(", ").append(entry.getKey()).append("=").append(entry.getValue()));
            builder.append("]");
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

    private Map<String, BattleStatRecord> battleStats;
    private ClientID clientID = new ClientID();

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
        found.stats.merge(BattleStat.NUM_DAMAGES, roll < 0 ? 1 : 0, (a, b) -> a + b);
        found.stats.merge(BattleStat.TOTAL_DAMAGE, roll, (a, b) -> a + b);
        found.stats.merge(BattleStat.MAX_DAMAGE, roll, (a, b) -> a > b ? a : b);
        int healingPerformed = ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.HEALING),
                false);
        found.stats.merge(BattleStat.HEALING_PERFORMED, healingPerformed, (a, b) -> a + b);
        found.stats.put(BattleStat.AGGRO_DAMAGE,
                ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.AGGRO), true));
        found.stats.put(BattleStat.AVG_DAMAGE,
                found.getNumDamgages() == 0 ? 0 : found.getTotalDamage() / found.getNumDamgages());

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

    public final Map<String, BattleStatRecord> getBattleStats(boolean onlyLiving) {
        if (!onlyLiving) {
            return Collections.unmodifiableMap(this.battleStats);
        }
        return Collections.unmodifiableMap(this.battleStats.entrySet().stream().filter(entry -> {
            BattleStatRecord record = entry.getValue();
            return record != null && !record.isDead();
        }).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
    }

    public final Collection<BattleStatRecord> getBattleStatSet(boolean onlyLiving) {
        return this.getBattleStats(onlyLiving).values();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BattleStats [battleStats=").append(battleStats != null ? battleStats.values() : null)
                .append("]");
        return builder.toString();
    }

    @Override
    public String getStartTag() {
        return "<BattleStats>";
    }

    @Override
    public String getEndTag() {
        return "</BattleStats>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + "Battle Statistics" + this.getEndTag();
    }

    @Override
    public void sendMsg(OutMessage msg) {
        if (msg != null && OutMessageType.CREATURE_AFFECTED.equals(msg.getOutType())) {
            CreatureAffectedMessage cam = (CreatureAffectedMessage) msg;
            this.update(cam);
        }
    }

    @Override
    public ClientID getClientID() {
        return this.clientID;
    }

}