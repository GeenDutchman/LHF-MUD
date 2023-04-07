package com.lhf.game.creature.vocation;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;

public class Fighter extends Vocation {

    public Fighter() {
        super(VocationName.FIGHTER);
    }

    @Override
    public Statblock createNewDefaultStatblock(String creatureRace) {
        Statblock built = new Statblock(creatureRace);

        built.getProficiencies().add(EquipmentTypes.LIGHTARMOR);
        built.getProficiencies().add(EquipmentTypes.MEDIUMARMOR);
        built.getProficiencies().add(EquipmentTypes.SHIELD);
        built.getProficiencies().add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        built.getProficiencies().add(EquipmentTypes.MARTIALWEAPONS);

        built.getInventory().addItem(new Longsword(true));
        built.getInventory().addItem(new LeatherArmor(false));
        built.getInventory().addItem(new HealPotion(true));
        built.getInventory().addItem(new Shield(true));

        // Set default stats
        built.getStats().put(Stats.MAXHP, 12);
        built.getStats().put(Stats.CURRENTHP, 12);
        built.getStats().put(Stats.AC, 11);
        built.getStats().put(Stats.XPWORTH, 500);

        built.getAttributes().setScore(Attributes.STR, 16);
        built.getAttributes().setScore(Attributes.DEX, 12);
        built.getAttributes().setScore(Attributes.CON, 14);
        built.getAttributes().setScore(Attributes.INT, 8);
        built.getAttributes().setScore(Attributes.WIS, 10);
        built.getAttributes().setScore(Attributes.CHA, 12);

        return built;
    }

    @Override
    public Attack modifyAttack(Attack attack) {
        // Attributes bestAttr =
        // this.getHighestAttributeBonus(EnumSet.of(Attributes.CHA));
        Creature attacker = attack.getAttacker();
        if (attacker != null) {
            int chaMod = attacker.getAttributes().getMod(Attributes.CHA);
            if (chaMod <= 0) {
                chaMod = 1;
            }
            DamageDice dd = new DamageDice(chaMod, DieType.TWELVE, DamageFlavor.AGGRO);
            for (CreatureEffect crEffect : attack) {
                if (crEffect.isOffensive()) {
                    MultiRollResult mrr = crEffect.getDamageResult();
                    MultiRollResult.Builder rebuilder = new MultiRollResult.Builder().addMultiRollResult(mrr);
                    rebuilder.addRollResults(dd.rollDice());
                    crEffect.updateDamageResult(rebuilder.Build());
                }
            }
        }
        return attack;

    }

}
