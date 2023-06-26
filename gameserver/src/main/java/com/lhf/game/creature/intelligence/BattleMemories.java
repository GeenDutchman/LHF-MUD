package com.lhf.game.creature.intelligence;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.messages.CommandContext;
import com.lhf.messages.out.CreatureAffectedMessage;

public class BattleMemories {
    /**
     *
     */
    private final BasicAI basicAI;

    public class BattleStatRecord {
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

    }

    protected Optional<String> lastAttakerName;
    protected int lastAggroDamage;
    protected Map<String, BattleStatRecord> battleStats;

    public BattleMemories(BasicAI basicAI) {
        this.basicAI = basicAI;
        this.lastAggroDamage = 0;
        this.lastAttakerName = Optional.empty();
        this.battleStats = new TreeMap<>();
    }

    public BattleMemories reset() {
        this.battleStats.clear();
        this.lastAggroDamage = 0;
        this.lastAttakerName = Optional.empty();
        return this;
    }

    public BattleMemories update(CreatureAffectedMessage ca) {
        if (ca.getEffect() == null) {
            return this;
        }
        Creature responsible = ca.getEffect().creatureResponsible();
        if (responsible == null) {
            return this;
        }

        int origRoll = ca.getEffect().getDamageResult().getOrigRoll();
        int roll = ca.getEffect().getDamageResult().getRoll();

        if (!this.battleStats.containsKey(responsible.getName())) {
            this.battleStats.put(responsible.getName(),
                    this.basicAI.new BattleStatRecord(responsible.getName(), responsible.getFaction(),
                            responsible.getVocation(),
                            responsible.getHealthBucket()));
        }
        if (ca.getAffected() == this.basicAI.npc && ca.getEffect().isOffensive()) {
            if (origRoll >= this.lastAggroDamage) {
                this.lastAggroDamage = origRoll;
                this.lastAttakerName = Optional.of(responsible.getName());
            }
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

    public BattleMemories initialize(Iterable<Creature> creatures) {
        for (Creature creature : creatures) {
            if (creature != null) {
                if (!this.battleStats.containsKey(creature.getName())) {
                    this.battleStats.put(creature.getName(),
                            this.basicAI.new BattleStatRecord(creature.getName(), creature.getFaction(),
                                    creature.getVocation(),
                                    creature.getHealthBucket()));
                } else {
                    BattleStatRecord found = this.battleStats.get(creature.getName());
                    found.faction = creature.getFaction(); // update just in case
                }
            }
        }
        return this;
    }

    public boolean contains(String creatureName) {
        if (this.lastAttakerName.isPresent() && this.lastAttakerName.get().equals(creatureName)) {
            return true;
        }
        return this.battleStats.containsKey(creatureName);
    }

    public BattleMemories remove(String creatureName) {
        this.battleStats.remove(creatureName);
        return this;
    }

    public Map<String, BattleStatRecord> getBattleStats() {
        return Collections.unmodifiableMap(this.battleStats);
    }

    public Optional<String> getLastAttakerName() {
        return lastAttakerName;
    }

    public int getLastAggroDamage() {
        return lastAggroDamage;
    }

    public CommandContext.Reply launchCommand(String command) {
        return this.basicAI.ProcessString(command);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BattleMemories [lastAttaker=").append(lastAttakerName).append(", lastAggroDamage=")
                .append(lastAggroDamage).append(", battleStats=").append(battleStats).append("]");
        return builder.toString();
    }

}