package com.lhf.game.creature.vocation;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.dice.DiceD8;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;

public abstract class Vocation implements Taggable, Comparable<Vocation> {

    /** Stateless copy of Longsword */
    private final static Longsword longsword = new Longsword();
    /** Stateless copy of leather armor */
    private final static LeatherArmor leatherArmor = new LeatherArmor();
    /** Stateless copy of shield */
    private final static Shield shield = new Shield();

    public enum VocationName implements Taggable {
        FIGHTER {

            @Override
            public Dice getLevelingDice() {
                return new DiceD8(1);
            }

            @Override
            public Set<EquipmentTypes> defaultProficiencies() {
                return EnumSet.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.MEDIUMARMOR, EquipmentTypes.SHIELD,
                        EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MARTIALWEAPONS);
            }

            @Override
            public List<Takeable> defaultInventory() {
                return List.of(Vocation.longsword, Vocation.leatherArmor, new HealPotion(), Vocation.shield);
            }

            @Override
            public Map<Stats, Integer> defaultStats() {
                return Map.of(Stats.MAXHP, 12, Stats.CURRENTHP, 12, Stats.AC, 11, Stats.XPWORTH, 500);
            }

            @Override
            public AttributeBlock defaultAttributes() {
                return new AttributeBlock(16, 12, 14, 8, 10, 12);
            }
        },
        MAGE {
            @Override
            public Dice getLevelingDice() {
                return new DiceD4(1);
            }

            @Override
            public Set<EquipmentTypes> defaultProficiencies() {
                return EnumSet.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.SIMPLEMELEEWEAPONS);
            }

            @Override
            public List<Takeable> defaultInventory() {
                return List.of(Vocation.leatherArmor, new HealPotion());
            }

            @Override
            public Map<Stats, Integer> defaultStats() {
                return Map.of(Stats.MAXHP, 9, Stats.CURRENTHP, 9, Stats.AC, 11, Stats.XPWORTH, 500);
            }

            @Override
            public AttributeBlock defaultAttributes() {
                return new AttributeBlock(8, 12, 10, 16, 14, 12);
            }
        },
        DUNGEON_MASTER {
            @Override
            public Dice getLevelingDice() {
                return null; // no point
            }

            @Override
            public Set<EquipmentTypes> defaultProficiencies() {
                return EnumSet.allOf(EquipmentTypes.class);
            }

            @Override
            public List<Takeable> defaultInventory() {
                return List.of(new HealPotion());
            }

            @Override
            public Map<Stats, Integer> defaultStats() {
                return Map.of(Stats.MAXHP, Integer.MAX_VALUE / 3, Stats.CURRENTHP, Integer.MAX_VALUE / 3, Stats.AC,
                        Integer.MAX_VALUE / 3, Stats.XPWORTH, Integer.MAX_VALUE / 3, Stats.PROFICIENCYBONUS,
                        Integer.MAX_VALUE / 3);
            }

            @Override
            public AttributeBlock defaultAttributes() {
                return new AttributeBlock(100, 100, 100, 100, 100, 100);
            }

        },
        HEALER {
            @Override
            public Dice getLevelingDice() {
                return new DiceD4(1);
            }

            @Override
            public Set<EquipmentTypes> defaultProficiencies() {
                return EnumSet.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.SIMPLEMELEEWEAPONS);
            }

            @Override
            public List<Takeable> defaultInventory() {
                return List.of(Vocation.leatherArmor, new HealPotion(), new HealPotion());
            }

            @Override
            public Map<Stats, Integer> defaultStats() {
                return Map.of(Stats.MAXHP, 9, Stats.CURRENTHP, 9, Stats.AC, 11, Stats.XPWORTH, 500);
            }

            @Override
            public AttributeBlock defaultAttributes() {
                return new AttributeBlock(8, 10, 12, 14, 16, 12);
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

        public abstract Dice getLevelingDice();

        public abstract Set<EquipmentTypes> defaultProficiencies();

        public abstract List<Takeable> defaultInventory();

        public abstract Map<Stats, Integer> defaultStats();

        public abstract AttributeBlock defaultAttributes();

    }

    protected int level;
    protected int experiencePoints;
    protected final VocationName name;
    protected ResourcePool resourcePool;

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

    public static final void onExperienceGain(final ICreature creature, final Vocation vocation, final int xpGain) {
        if (creature == null || vocation == null) {
            return;
        }
        synchronized (vocation) {
            vocation.experiencePoints += xpGain >= 0 ? xpGain : -1 * xpGain;
            if (vocation.experiencePoints >= (vocation.level + 1) * 100) {
                ++vocation.level;
                vocation.experiencePoints = 0;
                vocation.getResourcePool().refresh();
                Deltas deltas = new Deltas();
                deltas.setStatChange(Stats.PROFICIENCYBONUS, 1);
                final AttributeBlock block = creature.getAttributes();
                MultiRollResult.Builder resultBuilder = new MultiRollResult.Builder().addBonuses(1);
                if (block != null) {
                    if (vocation.level % 4 == 0) {
                        final TreeMap<Integer, Attributes> topFinder = new TreeMap<>(Comparator.reverseOrder());
                        for (Attributes attr : Attributes.values()) {
                            topFinder.put(block.getScore(attr), attr);
                        }
                        for (int i = 0; i < 2; i++) {
                            Entry<Integer, Attributes> entry = topFinder.pollFirstEntry();
                            if (entry != null) {
                                deltas.setAttributeScoreChange(entry.getValue(), 1);
                            }
                        }
                    }
                    resultBuilder.addBonuses(block.getMod(Attributes.CON));
                    Dice toRoll = vocation.getVocationName().getLevelingDice();
                    if (toRoll != null) {
                        resultBuilder.addRollResults(toRoll.rollDice());
                    }
                }
                final MultiRollResult total = resultBuilder.Build();
                deltas.setStatChange(Stats.MAXHP, total.getRoll()).setStatChange(Stats.CURRENTHP, total.getRoll());
                CreatureEffect levelUp = new CreatureEffect(
                        CreatureEffectSource.getCreatureEffectBuilder(String.format("Level up to %d!", vocation.level))
                                .withoutReversedApplication()
                                .setOnApplication(deltas).instantPersistence()
                                .setDescription(String.format("Level up to %d!", vocation.level)).build(),
                        creature, vocation);
                ICreature.eventAccepter.accept(creature, creature.applyEffect(levelUp));
            }
        }
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
