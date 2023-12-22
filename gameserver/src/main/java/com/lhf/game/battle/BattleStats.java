package com.lhf.game.battle;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats.BattleStatRecord.BattleStat;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public class BattleStats implements ClientMessenger {

    private static final BiFunction<Integer, Integer, Integer> adder = (a, b) -> {
        if (a != null && b != null) {
            return a + b;
        }
        return a != null ? a : b;
    };

    private static final Function<EnumMap<BattleStat, Integer>, EnumMap<BattleStat, Integer>> xpLens = (deltas) -> {
        if (deltas == null) {
            deltas = new EnumMap<>(BattleStat.class);
        }
        Integer numDamages = deltas.getOrDefault(BattleStat.NUM_DAMAGES, 0);
        if (numDamages > 0) {
            deltas.merge(BattleStat.XP_EARNED, numDamages, adder);
        }
        return deltas;
    };

    public final static Function<EnumMap<BattleStat, Integer>, EnumMap<BattleStat, Integer>> getXPLens(
            Vocation vocation) {
        if (vocation == null) {
            return xpLens;
        }
        VocationName vocationName = vocation.getVocationName();
        if (vocationName == null) {
            return xpLens;
        }
        switch (vocationName) {
            case DUNGEON_MASTER:
                return xpLens.andThen((deltas) -> {
                    for (BattleStat stat : EnumSet.of(BattleStat.NUM_DAMAGES, BattleStat.MAX_DAMAGE)) {
                        Integer value = deltas.getOrDefault(stat, 1);
                        if (value > 0) {
                            deltas.merge(stat, value, adder);
                        }
                    }
                    return deltas;
                });
            case FIGHTER:
                return xpLens.andThen((deltas) -> {
                    for (BattleStat stat : EnumSet.of(BattleStat.TOTAL_DAMAGE, BattleStat.AGGRO_DAMAGE)) {
                        Integer value = deltas.getOrDefault(stat, 0) / Integer.max(vocation.getLevel(), 1);
                        if (value > 0) {
                            deltas.merge(stat, value, adder);
                        }
                    }
                    return deltas;
                });
            case HEALER:
                return xpLens.andThen((deltas) -> {
                    for (BattleStat stat : EnumSet.of(BattleStat.AVG_DAMAGE, BattleStat.HEALING_PERFORMED)) {
                        Integer value = deltas.getOrDefault(stat, 0) / Integer.max(vocation.getLevel(), 1);
                        if (value > 0) {
                            deltas.merge(stat, value, adder);
                        }
                    }
                    return deltas;
                });
            case MAGE:
                return xpLens.andThen((deltas) -> {
                    for (BattleStat stat : EnumSet.of(BattleStat.TOTAL_DAMAGE, BattleStat.AVG_DAMAGE)) {
                        Integer value = deltas.getOrDefault(stat, 0) / Integer.max(vocation.getLevel(), 1);
                        if (value > 0) {
                            deltas.merge(stat, value, adder);
                        }
                    }
                    return deltas;
                });
            default:
                return xpLens;

        }
    }

    /**
     *
     */
    public static class BattleStatRecord implements Comparable<BattleStatRecord> {

        public static enum BattleStat {
            MAX_DAMAGE, AGGRO_DAMAGE, TOTAL_DAMAGE, NUM_DAMAGES, HEALING_PERFORMED, AVG_DAMAGE, XP_EARNED;

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

        public int getXPEarned() {
            return this.stats.getOrDefault(BattleStat.XP_EARNED, 0);
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
    private final ClientID clientID = new ClientID();
    private int deadXP;
    private final Logger logger;

    public BattleStats() {
        this.logger = Logger.getLogger(this.getClass().getName() + "." + this.clientID.getUuid());
        this.battleStats = new TreeMap<>();
        this.deadXP = 0;
    }

    public BattleStats(Map<String, BattleStatRecord> seedStats) {
        this.logger = Logger.getLogger(this.getClass().getName() + "." + this.clientID.getUuid());
        this.battleStats = seedStats != null ? new TreeMap<>(seedStats) : new TreeMap<>();
    }

    // note that any duplicates will overwrite each other!
    public BattleStats(Iterable<BattleStatRecord> seedRecords) {
        this.logger = Logger.getLogger(this.getClass().getName() + "." + this.clientID.getUuid());
        this.battleStats = new TreeMap<>();
        if (seedRecords != null) {
            for (BattleStatRecord record : seedRecords) {
                this.battleStats.put(record.targetName, record);
            }
        }
    }

    public BattleStats reset() {
        this.battleStats.clear();
        this.deadXP = 0;
        return this;
    }

    public BattleStats update(CreatureAffectedMessage ca) {
        if (ca.getEffect() == null) {
            return this;
        }
        ICreature responsible = ca.getEffect().creatureResponsible();
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

        EnumMap<BattleStat, Integer> deltas = new EnumMap<>(BattleStat.class);
        deltas.put(BattleStat.TOTAL_DAMAGE, roll);
        if (roll > 0) {
            deltas.put(BattleStat.NUM_DAMAGES, 1);
            // the difference to the next running average
            deltas.put(BattleStat.AVG_DAMAGE, (roll - found.getAverageDamage()) / (found.getNumDamgages() + 1));
        }
        if (found.getMaxDamage() < roll) {
            deltas.put(BattleStat.MAX_DAMAGE, roll - found.getMaxDamage());
        }
        int healingPerformed = ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.HEALING),
                false);
        if (healingPerformed != 0) {
            deltas.put(BattleStat.HEALING_PERFORMED, healingPerformed);
        }
        int aggroPerformed = ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.AGGRO), true);
        if (aggroPerformed != 0) {
            deltas.put(BattleStat.AGGRO_DAMAGE, aggroPerformed);
        }

        Function<EnumMap<BattleStat, Integer>, EnumMap<BattleStat, Integer>> lens = BattleStats
                .getXPLens(found.getVocation());
        if (lens != null) {
            deltas = lens.apply(deltas);
        }

        deltas.forEach((key, value) -> {
            found.stats.merge(key, value, adder);
        });

        ICreature targeted = ca.getAffected();
        if (targeted != null && !targeted.isAlive()) {
            BattleStatRecord targetRecord = this.battleStats.get(targeted.getName());
            if (targetRecord != null) {
                Integer targetEarned = targetRecord.stats.remove(BattleStat.XP_EARNED);
                if (targetEarned == null) {
                    targetEarned = 0;
                }
                targetEarned = targetEarned
                        / Integer.max(1, targetRecord.getVocation() != null ? targetRecord.getVocation().getLevel()
                                : targetRecord.getNumDamgages());
                found.stats.merge(BattleStat.XP_EARNED, targetEarned, adder);
            }
        }

        return this;
    }

    public BattleStats initialize(Iterable<ICreature> creatures) {
        for (ICreature creature : creatures) {
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

    public BattleStatRecord getRecord(String creatureName) {
        return this.battleStats.get(creatureName);
    }

    public BattleStats setDead(String creatureName, int worth) {
        BattleStatRecord stat = this.battleStats.get(creatureName);
        if (stat != null) {
            stat.setDead();
        }
        this.deadXP += worth;
        return this;
    }

    public int getDeadXP() {
        return this.deadXP;
    }

    public enum BattleStatsQuery {
        ONLY_LIVING, ONLY_DEAD, ALL;
    }

    public final Map<String, BattleStatRecord> getBattleStats(BattleStatsQuery query) {
        if (query == null || BattleStatsQuery.ALL.equals(query)) {
            return Collections.unmodifiableMap(this.battleStats);
        }
        return Collections.unmodifiableMap(this.battleStats.entrySet().stream().filter(entry -> {
            BattleStatRecord record = entry.getValue();
            if (record == null) {
                return false;
            }
            switch (query) {
                case ALL:
                    return true;
                case ONLY_DEAD:
                    return record.isDead();
                case ONLY_LIVING:
                    return !record.isDead();
                default:
                    return true;

            }
        }).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
    }

    public final Collection<BattleStatRecord> getBattleStatSet(BattleStatsQuery query) {
        return this.getBattleStats(query).values();
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
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
    public void receive(OutMessage msg) {
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