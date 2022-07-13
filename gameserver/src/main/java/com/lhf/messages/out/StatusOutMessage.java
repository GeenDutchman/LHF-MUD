package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.enums.Stats;

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

    public StatusOutMessage(Creature creature, boolean full) {
        this.full = full;
        this.name = creature.getName();
        this.colorTaggedName = creature.getColorTaggedName();
        this.race = creature.getCreatureRace();
        this.faction = creature.getFaction();
        this.healthBucket = HealthBuckets.calcualte(creature.getStats().get(Stats.CURRENTHP),
                creature.getStats().get(Stats.MAXHP));
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
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name:").append(this.colorTaggedName).append("\r\n");
        sb.append("Race:").append(this.race).append("\r\n");
        sb.append("Faction:").append(this.faction.toString()).append("\r\n");
        sb.append("Health: ").append(this.healthBucket.getColorTaggedName());
        if (this.full && this.currentHealth != null && this.maxHealth != null) {
            sb.append("(").append(this.currentHealth).append("/").append(this.maxHealth).append(")");
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

}
