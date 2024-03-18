package com.lhf.game.item.concrete.equipment;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD6;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class Whimsystick extends Weapon {
    private static final String description = "This isn't quite a quarterstaff, but also not a club...it is hard to tell. "
            +
            "But what you can tell is it seems to have a laughing aura around it, like it doesn't "
            + "care about what it does to other people...it's a whimsystick. \n";
    private static final int acBonus = 1;

    public Whimsystick() {
        super("Whimsystick", Whimsystick.description,
                Set.of(new CreatureEffectSource.Builder("Bonk").instantPersistence()
                        .setResistance(new EffectResistance(EnumSet.of(Attributes.STR),
                                Stats.AC))
                        .setDescription("It is bonked.")
                        .setOnApplication(new Deltas().addDamage(
                                new DamageDice(1, DieType.SIX,
                                        DamageFlavor.MAGICAL_BLUDGEONING)))
                        .build()),
                DamageFlavor.MAGICAL_BLUDGEONING, WeaponSubtype.MARTIAL);

        this.slots = Collections.singletonList(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.QUARTERSTAFF,
                EquipmentTypes.CLUB);
        this.equipEffects = Collections
                .singletonList(new CreatureEffectSource.Builder("AC bonus")
                        .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
                        .setDescription("This will magically increase your AC")
                        .setOnApplication(new Deltas().setStatChange(Stats.AC,
                                Whimsystick.acBonus))
                        .build());
    }

    @Override
    public Whimsystick makeCopy() {
        return this;
    }

    @Override
    public Attack generateAttack(ICreature attacker) {
        Set<CreatureEffectSource> extraSources = new HashSet<>();
        Dice chooser = new DiceD6(1);
        if (chooser.rollDice().getRoll() <= 2) {
            DamageDice healDice = new DamageDice(1, DieType.SIX, DamageFlavor.HEALING);

            extraSources.add(new CreatureEffectSource.Builder("Whimsy healing").instantPersistence()
                    .setDescription("The whimsystick chose to heal")
                    .setOnApplication(new Deltas().addDamage(healDice)).build());
        }
        return super.generateAttack(attacker, extraSources);
    }

}
