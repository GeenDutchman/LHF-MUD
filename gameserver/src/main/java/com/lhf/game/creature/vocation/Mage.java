package com.lhf.game.creature.vocation;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.SpellLevel;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.magic.CubeHolder;

public class Mage extends Vocation implements CubeHolder {
    private static class SpellSlot {
        private int available;
        private int levelMax;
        private final int max;

        public SpellSlot(int max) {
            this.max = max;
        }

        public String print() {
            if (Integer.MAX_VALUE == this.levelMax) {
                return "Infinite";
            }
            return String.format("%d/%d", this.available, this.levelMax);
        }

        public SpellSlot updateLevelMax() {
            return this.updateLevelMax(this.levelMax + 1);
        }

        public SpellSlot updateLevelMax(int nextmax) {
            this.levelMax = nextmax > this.max ? this.max : nextmax;
            this.available = levelMax;
            return this;
        }

        public SpellSlot useOne() {
            if (this.available > 0) {
                this.available--;
            }
            return this;
        }
    }

    private static EnumMap<SpellLevel, SpellSlot> getSpellSlotsMax() {
        EnumMap<SpellLevel, SpellSlot> maxes = new EnumMap<>(SpellLevel.class);
        maxes.put(SpellLevel.CANTRIP, new SpellSlot(Integer.MAX_VALUE));
        maxes.put(SpellLevel.FIRST_MAGNITUDE, new SpellSlot(4));
        maxes.put(SpellLevel.SECOND_MAGNITUDE, new SpellSlot(3));
        maxes.put(SpellLevel.THIRD_MAGNITUDE, new SpellSlot(3));
        maxes.put(SpellLevel.FOURTH_MAGNITUDE, new SpellSlot(3));
        maxes.put(SpellLevel.FIVTH_MAGNITUDE, new SpellSlot(3));
        maxes.put(SpellLevel.SIXTH_MAGNITUDE, new SpellSlot(2));
        maxes.put(SpellLevel.SEVENTH_MAGNITUDE, new SpellSlot(2));
        maxes.put(SpellLevel.EIGHTH_MAGNITUDE, new SpellSlot(1));
        maxes.put(SpellLevel.NINTH_MAGNITUDE, new SpellSlot(1));
        maxes.put(SpellLevel.TENTH_MAGNITUDE, new SpellSlot(0));
        return maxes;
    }

    private EnumMap<SpellLevel, SpellSlot> spellSlots;

    public Mage() {
        super(VocationName.MAGE);
        this.spellSlots = spellSlotsMaxForLevel();
    }

    private EnumMap<SpellLevel, SpellSlot> spellSlotsMaxForLevel() {
        EnumMap<SpellLevel, SpellSlot> Slots = Mage.getSpellSlotsMax();
        Slots.get(SpellLevel.CANTRIP).updateLevelMax(Integer.MAX_VALUE);
        if (this.level > 0) {
            level_iter: for (int i = 0; i <= this.level; i++) {
                for (SpellLevel sl : SpellLevel.values()) {
                    if (!SpellLevel.CANTRIP.equals(sl) && Slots.get(sl).max > 0) {
                        if (Slots.get(sl).levelMax == 0) {
                            if (SpellLevel.FOURTH_MAGNITUDE.compareTo(sl) >= 0) {
                                Slots.get(sl).updateLevelMax(2);
                            } else {
                                Slots.get(sl).updateLevelMax(1);
                            }
                            continue level_iter;
                        } else if (Slots.get(sl).levelMax < Slots.get(sl).max) {
                            Slots.get(sl).updateLevelMax();
                            continue level_iter;
                        }
                    }
                }
            }
        }
        return Slots;
    }

    @Override
    public String getCasterVocation() {
        return this.getName();
    }

    @Override
    public Integer getCasterDifficulty() {
        return 13; // TODO: actual difficulty
    }

    @Override
    public MultiRollResult spellAttack() {
        return new MultiRollResult.Builder().addRollResults(new DiceD20(1).rollDice()).Build(); // TODO: actual attack
    }

    @Override
    public Statblock createNewDefaultStatblock(String creatureRace) {
        Statblock built = new Statblock(creatureRace);
        built.getProficiencies().add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        built.getProficiencies().add(EquipmentTypes.LIGHTARMOR);

        built.getInventory().addItem(new LeatherArmor(false));
        built.getInventory().addItem(new HealPotion(true));

        // Set default stats
        built.getStats().put(Stats.MAXHP, 9);
        built.getStats().put(Stats.CURRENTHP, 9);
        built.getStats().put(Stats.AC, 11);
        built.getStats().put(Stats.XPWORTH, 500);

        built.getAttributes().setScore(Attributes.STR, 8);
        built.getAttributes().setScore(Attributes.DEX, 12);
        built.getAttributes().setScore(Attributes.CON, 10);
        built.getAttributes().setScore(Attributes.INT, 16);
        built.getAttributes().setScore(Attributes.WIS, 14);
        built.getAttributes().setScore(Attributes.CHA, 12);

        return built;
    }

    @Override
    public String printMagnitudes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Spell Magnitude: Slots").append("\n");
        for (SpellLevel sl : SpellLevel.values()) {
            sb.append(sl.toString()).append(":");
            sb.append(this.spellSlots.get(sl).print());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean useMagnitude(SpellLevel level) {
        if (level == null) {
            return false;
        } else if (SpellLevel.CANTRIP.equals(level)) {
            return true; // it's a cantrip
        }
        SpellSlot available = this.spellSlots.getOrDefault(level, new SpellSlot(0));
        if (available.available <= 0) {
            return false;
        }
        available.useOne();
        return true;
    }

    @Override
    public EnumSet<SpellLevel> availableMagnitudes() {
        return this.spellSlots.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null && entry.getValue().available >= 0)
                .map(entry -> entry.getKey()).collect(Collectors.toCollection(() -> EnumSet.noneOf(SpellLevel.class)));
    }

    @Override
    public Vocation onLevel() {
        this.level++;
        this.spellSlots = this.spellSlotsMaxForLevel();
        return this;
    }

    @Override
    public Vocation onRestTick() {
        for (Map.Entry<SpellLevel, SpellSlot> entry : this.spellSlots.entrySet()) {
            if (!SpellLevel.CANTRIP.equals(entry.getKey()) && entry.getValue().available < entry.getValue().levelMax) {
                entry.getValue().available++;
                break;
            }
        }
        return this;
    }

}
