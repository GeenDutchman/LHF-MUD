package com.lhf.messages.events;

import java.util.NavigableSet;

import com.lhf.Taggable.BasicTaggable;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;
import com.lhf.messages.GameEventType;

public class CreatureStatusRequestedEvent extends SeeEvent {
    private final static TickType tickType = TickType.ACTION;
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
    private final String vocationResources;

    public static class Builder extends SeeEvent.ABuilder<Builder> {
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
        private String vocationResources;

        protected Builder() {
            super(GameEventType.STATUS);
        }

        public Builder setFromCreature(ICreature creature) {
            return this.setFromCreature(creature, false);
        }

        public Builder setFromCreature(ICreature creature, boolean full) {
            this.setExaminable(creature);
            this.full = full;
            this.name = creature.getName();
            this.colorTaggedName = creature.getColorTaggedName();
            this.race = creature.getCreatureRace();
            this.addExtraInfo(String.format("Race:%s.", this.race != null ? this.race : "None"));
            this.faction = creature.getFaction();
            this.addExtraInfo(String.format("Faction:%s.", this.faction));
            this.healthBucket = HealthBuckets.calculate(creature.getStats().getOrDefault(Stats.CURRENTHP, 1),
                    creature.getStats().getOrDefault(Stats.MAXHP, 0));
            this.addSeen("Health", this.healthBucket);
            this.vocationName = creature.getVocation() != null ? creature.getVocation().getVocationName() : null;
            this.vocationLevel = creature.getVocation() != null ? creature.getVocation().getLevel() : null;
            if (this.vocationName != null) {
                this.addExtraInfo(String.format("Vocation:%s%s.", this.vocationName.getColorTaggedName(),
                        this.vocationLevel != null ? this.vocationLevel : ""));
            }
            if (this.full) {
                this.vocationResources = creature.getVocation() != null
                        && creature.getVocation().getResourcePool() != null
                                ? creature.getVocation().getResourcePool().print()
                                : null;
                if (this.vocationResources != null) {
                    this.addExtraInfo(this.vocationResources);
                }
                this.currentHealth = creature.getStats().get(Stats.CURRENTHP);
                this.maxHealth = creature.getStats().get(Stats.MAXHP);
                if (this.currentHealth != null && this.maxHealth != null) {
                    this.addSeen("Health", BasicTaggable.customTaggable("<health>",
                            String.format("%d/%d", this.currentHealth, this.maxHealth), "</health>"));
                }
                this.armorClass = creature.getStats().get(Stats.AC);
                if (this.armorClass != null) {
                    this.addExtraInfo(String.format("Armor:%s.", this.armorClass));
                }
                this.attributes = creature.getAttributes();
                if (this.attributes != null) {
                    this.addExtraInfo(String.format("Attributes:%s.", this.attributes.toString()));
                }
                final NavigableSet<CreatureEffect> effects = creature.getEffects();
                if (effects != null) {
                    effects.stream().filter(effect -> effect != null).map(effect -> effect.getSource())
                            .forEachOrdered(source -> this.addEffector(source));
                }
            } else {
                this.vocationResources = null;
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

        public String getVocationResources() {
            return vocationResources;
        }

        @Override
        public CreatureStatusRequestedEvent Build() {
            return new CreatureStatusRequestedEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getStatusBuilder() {
        return new Builder();
    }

    public CreatureStatusRequestedEvent(Builder builder) {
        super(builder);
        this.full = builder.isFull();
        this.name = builder.getName();
        this.colorTaggedName = builder.getColorTaggedName();
        this.race = builder.getRace();
        this.faction = builder.getFaction();
        this.healthBucket = builder.getHealthBucket();
        this.vocationName = builder.getVocationName();
        this.vocationLevel = builder.getVocationLevel();
        this.vocationResources = builder.getVocationResources();
        this.currentHealth = builder.getCurrentHealth();
        this.maxHealth = builder.getMaxHealth();
        this.armorClass = builder.getArmorClass();
        this.attributes = builder.getAttributes();
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

    public VocationName getVocationName() {
        return vocationName;
    }

    public Integer getVocationLevel() {
        return vocationLevel;
    }

    public String getVocationResources() {
        return vocationResources;
    }

    @Override
    public TickType getTickType() {
        return tickType;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
