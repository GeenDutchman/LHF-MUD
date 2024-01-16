package com.lhf.game.creature.vocation;

import java.util.EnumSet;
import java.util.Objects;

import com.lhf.Taggable;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.StatblockBuilder;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;

public abstract class Vocation implements Taggable, Comparable<Vocation> {
    public enum VocationName implements Taggable {
        FIGHTER {
            @Override
            public StatblockBuilder createNewDefaultStatblock(String creatureRace) {
                StatblockBuilder builder = Statblock.getBuilder().setCreatureRace(creatureRace);

                builder.addProficiency(EquipmentTypes.LIGHTARMOR);
                builder.addProficiency(EquipmentTypes.MEDIUMARMOR);
                builder.addProficiency(EquipmentTypes.SHIELD);
                builder.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS);
                builder.addProficiency(EquipmentTypes.MARTIALWEAPONS);

                builder.addItemToInventory(new Longsword());
                builder.addItemToInventory(new LeatherArmor());
                builder.addItemToInventory(new HealPotion());
                builder.addItemToInventory(new Shield());

                // Set default stats
                builder.setStat(Stats.MAXHP, 12);
                builder.setStat(Stats.CURRENTHP, 12);
                builder.setStat(Stats.AC, 11);
                builder.setStat(Stats.XPWORTH, 500);

                builder.setAttributeBlock(16, 12, 14, 8, 10, 12);

                return builder;
            }
        },
        MAGE {
            @Override
            public StatblockBuilder createNewDefaultStatblock(String creatureRace) {
                StatblockBuilder builder = Statblock.getBuilder().setCreatureRace(creatureRace);
                builder.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS);
                builder.addProficiency(EquipmentTypes.LIGHTARMOR);

                builder.addItemToInventory(new LeatherArmor());
                builder.addItemToInventory(new HealPotion());

                // Set default stats
                builder.setStat(Stats.MAXHP, 9);
                builder.setStat(Stats.CURRENTHP, 9);
                builder.setStat(Stats.AC, 11);
                builder.setStat(Stats.XPWORTH, 500);

                builder.setAttributeBlock(8, 12, 10, 16, 14, 12);

                return builder;
            }
        },
        DUNGEON_MASTER {
            @Override
            public StatblockBuilder createNewDefaultStatblock(String creatureRace) {
                StatblockBuilder builder = Statblock.getBuilder().setCreatureRace(creatureRace);

                builder.addProficiencies(EnumSet.allOf(EquipmentTypes.class));

                builder.addItemToInventory(new HealPotion());

                // Set default stats
                builder.setStat(Stats.MAXHP, Integer.MAX_VALUE / 3);
                builder.setStat(Stats.CURRENTHP, Integer.MAX_VALUE / 3);
                builder.setStat(Stats.AC, Integer.MAX_VALUE / 3);
                builder.setStat(Stats.XPWORTH, Integer.MAX_VALUE / 3);
                builder.setStat(Stats.PROFICIENCYBONUS, Integer.MAX_VALUE / 3);

                builder.setAttributeBlock(100, 100, 100, 100, 100, 100);

                return builder;
            }
        },
        HEALER {
            @Override
            public StatblockBuilder createNewDefaultStatblock(String creatureRace) {
                StatblockBuilder builder = Statblock.getBuilder().setCreatureRace(creatureRace);
                builder.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS);
                builder.addProficiency(EquipmentTypes.LIGHTARMOR);

                builder.addItemToInventory(new LeatherArmor());
                builder.addItemToInventory(new HealPotion());

                // Set default stats
                builder.setStat(Stats.MAXHP, 9);
                builder.setStat(Stats.CURRENTHP, 9);
                builder.setStat(Stats.AC, 11);
                builder.setStat(Stats.XPWORTH, 500);

                builder.setAttributeBlock(8, 10, 12, 14, 16, 12);

                return builder;
            }
        };

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

        public abstract StatblockBuilder createNewDefaultStatblock(String creatureRace);

    }

    protected int level;
    protected int experiencePoints;
    protected final VocationName name;
    protected ResourcePool resourcePool;

    public Vocation onLevel() {
        this.level++;
        this.experiencePoints = 0;
        this.getResourcePool().refresh();
        return this;
    }

    public abstract Vocation onRestTick();

    protected abstract ResourcePool initPool();

    public abstract Vocation copy();

    protected Vocation(VocationName name) {
        this.name = name;
        this.resourcePool = this.initPool();
        this.level = 1;
        this.experiencePoints = 0;
    }

    protected Vocation(VocationName name, Integer level) {
        this.name = name;
        this.level = level == null || level < 1 ? 1 : level;
        this.resourcePool = this.initPool();
        this.experiencePoints = 0;
    }

    public Vocation resetLevel() {
        this.level = 1;
        return this;
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

    /**
     * Adds experience to the Vocation.
     * 
     * @param xpGain
     * @return true if levelled up, false otherwise
     */
    public boolean addExperience(int xpGain) {
        this.experiencePoints += xpGain >= 0 ? xpGain : -1 * xpGain;
        if (this.experiencePoints >= (this.level + 1) * 100) {
            this.onLevel();
            return true;
        }
        return false;
    }

    public String getName() {
        return this.name.toString();
    }

    public VocationName getVocationName() {
        return this.name;
    }

    @Override
    public String getColorTaggedName() {
        return String.format("%s%s %s%s", this.getStartTag(), this.name, this.level, this.getEndTag());
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
