package com.lhf.game.magic.concrete;

import java.util.Arrays;
import java.util.List;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.DamageSpell;

public class ShockBolt extends CreatureAffector implements DamageSpell {
    private List<DamageDice> damages;

    public ShockBolt() {
        super(0, "Shock Bolt", "A small spark of electricity shocks a creature you choose as a target", true);
        this.setInvocation("Astra Horeb");
        this.damages = Arrays.asList(new DamageDice(1, DieType.FOUR, this.getMainFlavor()));
    }

    @Override
    public String Cast() {
        StringBuilder sb = new StringBuilder();
        for (Creature target : this.getTargets()) {
            sb.append("A small spark zips from ").append(this.getCaster().getColorTaggedName())
                    .append("'s finger and flies toward ").append(target.getColorTaggedName()).append("!");
        }
        return sb.toString();
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.LIGHTNING;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.printDescription()).append(this.printStats());
        return sb.toString();
    }

}
