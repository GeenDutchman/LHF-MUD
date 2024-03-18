package com.lhf.game.item.concrete;

import java.util.Set;

import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealType;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Usable;

public class HealPotion extends Usable {

    private static CreatureEffectSource sourceFromHealType(HealType type) {
        if (type == null) {
            type = HealType.Regular;
        }
        final CreatureEffectSource.Builder builder = CreatureEffectSource
                .getCreatureEffectBuilder(type.toString() + " Potion Healing");
        final Deltas deltas = new Deltas().setStatChange(Stats.CURRENTHP, 1);
        switch (type) {
            case Critical:
                deltas.addDamage(new DamageDice(1, DieType.EIGHT, DamageFlavor.HEALING));
            case Greater:
                deltas.addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.HEALING));
            case Regular:
                deltas.addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING));
            default:
                deltas.addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING));
        }
        builder.setOnApplication(deltas);
        return builder.build();
    }

    private final HealType healtype;

    public HealPotion() {
        super(HealType.Regular.toString() + " Potion of Healing",
                Set.of(HealPotion.sourceFromHealType(HealType.Regular)));
        this.healtype = HealType.Regular;
    }

    public HealPotion(HealType type) {
        super((type != null ? type.toString() + " " : "") + "Potion of Healing",
                Set.of(HealPotion.sourceFromHealType(type)));
        this.healtype = type;
    }

    @Override
    public HealPotion makeCopy() {
        if (this.numCanUseTimes < 0) {
            return this;
        }
        return new HealPotion(this.healtype);
    }

}
