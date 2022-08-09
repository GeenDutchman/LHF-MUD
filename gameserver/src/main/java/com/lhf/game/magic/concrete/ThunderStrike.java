package com.lhf.game.magic.concrete;

import java.util.Arrays;
import java.util.List;

import com.lhf.Taggable;
import com.lhf.game.EntityEffector.EffectPersistence;
import com.lhf.game.creature.Creature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.magic.CreatureTargetingSpell;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.messages.out.CastingMessage;

public class ThunderStrike extends CreatureTargetingSpellEntry {

    public ThunderStrike(Integer level, String name, String description) {
        super(1, "Thunder Strike", "Bonearge Laarzen", EffectPersistence.INSTANT,
                "A small but loud bolt of electricity shocks a creature you choose as a target",
                true, false);
        this.damages = Arrays.asList(new DamageDice(1, DieType.SIX, DamageFlavor.THUNDER),
                new DamageDice(1, DieType.FOUR, DamageFlavor.LIGHTNING));
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        StringBuilder sb = new StringBuilder();
        for (Taggable target : targets) {
            sb.append("A large bolt zaps from ").append(caster.getColorTaggedName())
                    .append("'s hand and thunders toward ").append(target.getColorTaggedName()).append("!");
        }
        return new CastingMessage(caster, this, sb.toString());
    }

}
