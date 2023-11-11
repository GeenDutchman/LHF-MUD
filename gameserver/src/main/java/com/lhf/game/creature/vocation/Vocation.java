package com.lhf.game.creature.vocation;

import java.util.Objects;

import com.lhf.Taggable;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.ResourceCost;

public abstract class Vocation implements Taggable, Comparable<Vocation> {
    // TODO: redesign Vocation vs VocationName (decorator pattern?)

    public enum VocationName implements Taggable {
        FIGHTER, MAGE, DUNGEON_MASTER, HEALER;

        public static VocationName getVocationName(String value) {
            for (VocationName vName : values()) {
                if (vName.toString().equalsIgnoreCase(value)) {
                    return vName;
                }
            }
            return null;
        }

        public static boolean isVocationName(String value) {
            return VocationName.getVocationName(value) != null;
        }

        public boolean isCubeHolder() {
            return this != FIGHTER;
        }

        @Override
        public String getStartTag() {
            return "<vocation>";
        }

        @Override
        public String getEndTag() {
            return "</vocation>";
        }

        @Override
        public String getColorTaggedName() {
            return this.getStartTag() + this.toString() + this.getEndTag();
        }
    }

    protected interface ResourcePool {

        /** Checks to see if the cost can be paid from the pool */
        public default boolean checkCost(ResourceCost costNeeded) {
            return false;
        }

        /**
         * Pays *at least* the cost from the pool.
         * Will return either `costNeeded` or a higher magnitude of it was necessary.
         * If `{@link #checkCost(ResourceCost)}` would return false, this method will
         * return {@link com.lhf.game.enums.ResourceCost#NO_COST}.
         * 
         * @param costNeeded how much power is needed to pay
         * @return how much was actually paid
         */
        public default ResourceCost payCost(ResourceCost costNeeded) {
            return ResourceCost.NO_COST;
        }

        /**
         * Pays *at least* the cost from the pool.
         * Will return either `costNeeded` or a higher magnitude of it was necessary.
         * If `{@link #checkCost(ResourceCost)}` would return false, this method will
         * return {@link com.lhf.game.enums.ResourceCost#NO_COST}.
         * 
         * @param costNeeded how much power is needed to pay
         * @return how much was actually paid
         */
        public default ResourceCost payCost(int costNeeded) {
            return this.payCost(ResourceCost.fromInt(costNeeded));
        }

        /**
         * Fills the resource pool back up all the way, with all the way defined by
         * level. `level` < 0 will be ignored.
         */
        public void refresh(int level);

        /**
         * Adds `refill` amount of the resource back to the pool.
         * Cannot overflow the pool.
         * 
         * @param refill
         * @return true if amount was accepted, false if not
         */
        public boolean reload(ResourceCost refill);

        /**
         * Adds `refill` amount of the resource back to the pool.
         * Cannot overflow the pool. `refil` <= 0 will be ignored.
         * 
         * @param refill
         */
        public default void reload(int refill) {
            while (refill > 0) {
                ResourceCost toRefill = ResourceCost.fromInt(refill);
                refill = refill - toRefill.toInt();
                this.reload(toRefill);
            }
        }

        public String print();
    }

    protected int level;
    protected int experiencePoints;
    protected final VocationName name;

    public abstract Statblock createNewDefaultStatblock(String creatureRace);

    public abstract Vocation onLevel();

    public abstract Vocation onRestTick();

    protected Vocation(VocationName name) {
        this.name = name;
        this.level = 1;
        this.experiencePoints = 0;
    }

    public int getLevel() {
        return level;
    }

    public int getExperiencePoints() {
        return this.experiencePoints;
    }

    public int addExperience(int xpGain) {
        this.experiencePoints += xpGain >= 0 ? xpGain : -1 * xpGain;
        return this.experiencePoints;
    }

    public String getName() {
        return name.toString();
    }

    public VocationName getVocationName() {
        return this.name;
    }

    public Attack modifyAttack(Attack attack) {
        return attack;
    }

    public int numberOfMeleeTargets() {
        return 1;
    }

    @Override
    public String getColorTaggedName() {
        return this.name.getColorTaggedName();
    }

    @Override
    public String getEndTag() {
        return "</vocation>";
    }

    @Override
    public String getStartTag() {
        return "<vocation>";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vocation)) {
            return false;
        }
        Vocation other = (Vocation) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int compareTo(Vocation o) {
        if (o == null) {
            return 1;
        }
        int levelCmp = this.level - o.getLevel();
        if (levelCmp != 0) {
            return levelCmp;
        }
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Vocation [name=").append(name).append(", level=").append(level).append("]");
        return builder.toString();
    }

}
