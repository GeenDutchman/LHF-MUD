package com.lhf.game.creature.vocation;

import java.util.Objects;

import com.lhf.Taggable;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;

public abstract class Vocation implements Taggable, Comparable<Vocation> {
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

    protected int level;
    protected int experiencePoints;
    protected final VocationName name;
    protected ResourcePool resourcePool;

    public abstract Statblock createNewDefaultStatblock(String creatureRace);

    public Vocation onLevel() {
        this.level++;
        this.experiencePoints = 0;
        this.getResourcePool().refresh();
        return this;
    }

    public abstract Vocation onRestTick();

    protected abstract ResourcePool initPool();

    protected Vocation(VocationName name) {
        this.name = name;
        this.resourcePool = this.initPool();
        this.level = 1;
        this.experiencePoints = 0;
    }

    public int getLevel() {
        return level;
    }

    public int getExperiencePoints() {
        return this.experiencePoints;
    }

    public ResourcePool getResourcePool() {
        if (this.resourcePool == null) {
            this.resourcePool = this.initPool();
        }
        return this.resourcePool;
    }

    public int addExperience(int xpGain) {
        this.experiencePoints += xpGain >= 0 ? xpGain : -1 * xpGain;
        if (this.experiencePoints >= (this.level + 1) * 100) {
            this.onLevel();
        }
        return this.experiencePoints;
    }

    public String getName() {
        return name.toString();
    }

    public VocationName getVocationName() {
        return this.name;
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
