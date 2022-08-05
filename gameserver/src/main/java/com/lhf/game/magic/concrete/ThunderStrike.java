package com.lhf.game.magic.concrete;

import java.util.Arrays;
import java.util.List;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.magic.interfaces.CreatureTargetingSpell;
import com.lhf.game.magic.interfaces.DamageSpell;
import com.lhf.messages.out.CastingMessage;

public class ThunderStrike extends CreatureTargetingSpell implements DamageSpell {
    private List<DamageDice> damages;

    public ThunderStrike(Integer level, String name, String description) {
        super(1, "Thunder Strike", "A small but loud bolt of electricity shocks a creature you choose as a target",
                true);
        this.setInvocation("Bonearge Laarzen");
        this.damages = Arrays.asList(new DamageDice(1, DieType.SIX, this.getMainFlavor()),
                new DamageDice(1, DieType.FOUR, DamageFlavor.LIGHTNING));
    }

    @Override
    public CastingMessage Cast() {
        StringBuilder sb = new StringBuilder();
        for (Creature target : this.getTargets()) {
            sb.append("A large bolt zaps from ").append(this.getCaster().getColorTaggedName())
                    .append("'s hand and thunders toward ").append(target.getColorTaggedName()).append("!");
        }
        return new CastingMessage(this.getName(), sb.toString());
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.printDescription()).append(this.printStats());
        return sb.toString();
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.THUNDER;
    }

}
