package com.lhf.game.item.concrete.equipment;

import java.util.Collections;
import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD6;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class Whimsystick extends Weapon {
    private List<DamageDice> damages;
    private final int acBonus = 1;

    public Whimsystick(boolean isVisible) {
        super("Whimsystick", isVisible);

        this.slots = Collections.singletonList(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.QUARTERSTAFF, EquipmentTypes.CLUB);
        this.damages = List.of(new DamageDice(1, DieType.SIX, this.getMainFlavor()));
        this.equipEffects = Collections
                .singletonList(new BasicCreatureEffector(null, this, new EffectPersistence(TickType.CONDITIONAL))
                        .addStatChange(Stats.AC, this.acBonus));
        this.descriptionString = "This isn't quite a quarterstaff, but also not a club...it is hard to tell. " +
                "But what you can tell is it seems to have a laughing aura around it, like it doesn't " +
                "care about what it does to other people...it's a whimsystick. \n";
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.MAGICAL_BLUDGEONING;
    }

    @Override
    public List<DamageDice> getDamages() {
        return damages;
    }

    @Override
    public Attack modifyAttack(Attack attack) {
        Dice chooser = new DiceD6(1);
        if (chooser.rollDice().getRoll() <= 2) {
            DamageDice healDice = new DamageDice(1, DieType.SIX, DamageFlavor.HEALING);
            attack.updateDamageResult(new MultiRollResult(healDice.rollDice()));
        }
        return attack;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.MARTIAL;
    }
}
