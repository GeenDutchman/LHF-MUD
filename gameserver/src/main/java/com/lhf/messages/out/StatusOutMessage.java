package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.messages.OutMessageType;

public class StatusOutMessage extends OutMessage {
    private final boolean full;
    private final String name;
    private final String colorTaggedName;
    private final String race;
    private final CreatureFaction faction;
    private final HealthBuckets healthBucket;
    private final Integer currentHealth;
    private final Integer maxHealth;
    private final Integer armorClass;
    private final AttributeBlock attributes;
    private final VocationName vocationName;
    private final Integer vocationLevel;

    public static class Builder extends OutMessage.Builder<Builder> {
        private boolean full;
        private String name;
        private String colorTaggedName;
        private String race;
        private CreatureFaction faction;
        private HealthBuckets healthBucket;
        private Integer currentHealth;
        private Integer maxHealth;
        private Integer armorClass;
        private AttributeBlock attributes;
        private VocationName vocationName;
        private Integer vocationLevel;

        protected Builder() {
            super(OutMessageType.STATUS);
        }

        public Builder setFromCreature(Creature creature, boolean full) {
            this.full = full;
            this.name = creature.getName();
            this.colorTaggedName = creature.getColorTaggedName();
            this.race = creature.getCreatureRace();
            this.faction = creature.getFaction();
            this.healthBucket = HealthBuckets.calculate(creature.getStats().getOrDefault(Stats.CURRENTHP, 1),
                    creature.getStats().getOrDefault(Stats.MAXHP, 0));
            this.vocationName = creature.getVocation() != null ? creature.getVocation().getVocationName() : null;
            this.vocationLevel = creature.getVocation() != null ? creature.getVocation().getLevel() : null;
            if (this.full) {
                this.currentHealth = creature.getStats().get(Stats.CURRENTHP);
                this.maxHealth = creature.getStats().get(Stats.MAXHP);
                this.armorClass = creature.getStats().get(Stats.AC);
                this.attributes = creature.getAttributes();
            } else {
                this.currentHealth = null;
                this.maxHealth = null;
                this.armorClass = null;
                this.attributes = null;
            }
            return this;
        }

        public boolean isFull() {
            return full && !this.isBroadcast(); // full cannot be broadcasted
        }

        public String getName() {
            return name;
        }

        public String getColorTaggedName() {
            return colorTaggedName;
        }

        public String getRace() {
            return race;
        }

        public CreatureFaction getFaction() {
            return faction;
        }

        public HealthBuckets getHealthBucket() {
            return healthBucket;
        }

        public Integer getCurrentHealth() {
            return currentHealth;
        }

        public Integer getMaxHealth() {
            return maxHealth;
        }

        public Integer getArmorClass() {
            return armorClass;
        }

        public AttributeBlock getAttributes() {
            return attributes;
        }

        public VocationName getVocationName() {
            return vocationName;
        }

        public Integer getVocationLevel() {
            return vocationLevel;
        }

        /*
         * public Builder setFull(boolean full) {
         * this.full = full;
         * return this;
         * }
         * 
         * public Builder setName(String name) {
         * this.name = name;
         * return this;
         * }
         * 
         * public Builder setColorTaggedName(String colorTaggedName) {
         * this.colorTaggedName = colorTaggedName;
         * return this;
         * }
         * 
         * public Builder setRace(String race) {
         * this.race = race;
         * return this;
         * }
         * 
         * public Builder setFaction(CreatureFaction faction) {
         * this.faction = faction;
         * return this;
         * }
         * 
         * public Builder setHealthBucket(HealthBuckets healthBucket) {
         * this.healthBucket = healthBucket;
         * return this;
         * }
         * 
         * public Builder setCurrentHealth(Integer currentHealth) {
         * this.currentHealth = currentHealth;
         * return this;
         * }
         * 
         * public Builder setMaxHealth(Integer maxHealth) {
         * this.maxHealth = maxHealth;
         * return this;
         * }
         * 
         * public Builder setArmorClass(Integer armorClass) {
         * this.armorClass = armorClass;
         * return this;
         * }
         * 
         * public Builder setAttributes(AttributeBlock attributes) {
         * this.attributes = attributes;
         * return this;
         * }
         * 
         * public Builder setVocationName(VocationName vocationName) {
         * this.vocationName = vocationName;
         * return this;
         * }
         */

        @Override
        public StatusOutMessage Build() {
            return new StatusOutMessage(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public StatusOutMessage(Builder builder) {
        super(builder);
        this.full = builder.isFull();
        this.name = builder.getName();
        this.colorTaggedName = builder.getColorTaggedName();
        this.race = builder.getRace();
        this.faction = builder.getFaction();
        this.healthBucket = builder.getHealthBucket();
        this.vocationName = builder.getVocationName();
        this.vocationLevel = builder.getVocationLevel();
        this.currentHealth = builder.getCurrentHealth();
        this.maxHealth = builder.getMaxHealth();
        this.armorClass = builder.getArmorClass();
        this.attributes = builder.getAttributes();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.colorTaggedName != null) {
            sb.append("Name: ").append(this.colorTaggedName).append("\r\n");
        } else if (this.name != null) {
            sb.append("Name: ").append(this.name).append("\r\n");
        }
        if (this.race != null) {
            sb.append("Race: ").append(this.race).append("\r\n");
        }
        if (this.faction != null) {
            sb.append("Faction: ").append(this.faction.toString()).append("\r\n");
        }
        if (this.vocationName != null) {
            sb.append("Vocation: ").append(this.vocationName.getColorTaggedName());
            if (this.vocationLevel != null) {
                sb.append(" ").append(this.vocationLevel);
            }
            sb.append("\r\n");
        }
        if (this.healthBucket != null) {
            sb.append("Health: ").append(this.healthBucket.getColorTaggedName());
        }
        if (this.full && this.currentHealth != null && this.maxHealth != null) {
            sb.append(" (").append(this.currentHealth).append("/").append(this.maxHealth).append(")");
        }
        sb.append("\r\n");
        if (this.full && this.armorClass != null) {
            sb.append("Armor: ").append(this.armorClass).append("\r\n");
        }
        if (this.full && this.attributes != null) {
            sb.append("Attributes:\r\n").append(this.attributes.toString());
        }
        return sb.toString();
    }

    public boolean isFull() {
        return full;
    }

    public String getName() {
        return name;
    }

    public String getColorTaggedName() {
        return colorTaggedName;
    }

    public String getRace() {
        return race;
    }

    public CreatureFaction getFaction() {
        return faction;
    }

    public HealthBuckets getHealthBucket() {
        return healthBucket;
    }

    public Integer getCurrentHealth() {
        return currentHealth;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public Integer getArmorClass() {
        return armorClass;
    }

    public AttributeBlock getAttributes() {
        return attributes;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
