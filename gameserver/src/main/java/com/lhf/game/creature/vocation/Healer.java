package com.lhf.game.creature.vocation;

import java.util.EnumSet;

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

public class Healer extends Vocation implements CubeHolder {
    private static class SpellPoints {
        private int available;
        private int levelmax;
        private final int max = 22;

        public String print() {
            return String.format("%d/%d", this.available, this.levelmax);
        }

        public SpellPoints use(int amount) {
            int toUse = Integer.max(0, amount);
            if (toUse <= this.available) {
                this.available -= toUse;
            }
            return this;
        }
    }

    private SpellPoints spellPoints;

    public Healer() {
        super(VocationName.HEALER);
        this.spellPoints = this.initSpellPoints();
    }

    private SpellPoints initSpellPoints() {
        SpellPoints constructed = new SpellPoints();
        if (this.level > 0) {
            for (int i = 1; i <= this.level; i++) {
                constructed.levelmax += 1;
                if (i < 7 && i % 2 != 0) {
                    constructed.levelmax += 1;
                }
            }
        }
        if (constructed.levelmax > constructed.max) {
            constructed.levelmax = constructed.max;
        }
        constructed.available = constructed.levelmax;
        return constructed;
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
        built.getAttributes().setScore(Attributes.DEX, 10);
        built.getAttributes().setScore(Attributes.CON, 12);
        built.getAttributes().setScore(Attributes.INT, 14);
        built.getAttributes().setScore(Attributes.WIS, 16);
        built.getAttributes().setScore(Attributes.CHA, 12);

        return built;
    }

    @Override
    public String printMagnitudes() {
        return String.format("You have %s spell points.\n", this.spellPoints.print());
    }

    @Override
    public boolean useMagnitude(SpellLevel level) {
        if (level == null) {
            return false;
        } else if (level.toInt() > this.spellPoints.available) {
            return false;
        }
        this.spellPoints.use(level.toInt());
        return true;
    }

    @Override
    public EnumSet<SpellLevel> availableMagnitudes() {
        int count = (this.level / 2) + (this.level % 2 != 0 ? 1 : 0);
        EnumSet<SpellLevel> available = EnumSet.of(SpellLevel.CANTRIP);
        for (SpellLevel sl : SpellLevel.values()) {
            if (sl.toInt() <= count && this.spellPoints.available >= sl.toInt()) {
                available.add(sl);
            }
        }
        return available;
    }

    @Override
    public Vocation onLevel() {
        this.level += 1;
        this.spellPoints = this.initSpellPoints();
        return this;
    }

    @Override
    public Vocation onRestTick() {
        this.spellPoints.available += 1;
        if (this.spellPoints.available > this.spellPoints.levelmax) {
            this.spellPoints.available = this.spellPoints.levelmax;
        }
        return this;
    }

}
