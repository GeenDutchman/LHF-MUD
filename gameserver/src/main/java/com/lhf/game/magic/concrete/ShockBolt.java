package com.lhf.game.magic.concrete;

import java.util.Arrays;
import java.util.List;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.messages.out.CastingMessage;

public class ShockBolt extends CreatureTargetingSpellEntry {
    public ShockBolt() {
        super(0, "Shock Bolt", "Astra Horeb", new EffectPersistence(TickType.INSTANT),
                "A small spark of electricity shocks a creature you choose as a target", true, false);
        this.damages = Arrays.asList(new DamageDice(1, DieType.FOUR, DamageFlavor.LIGHTNING));
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        StringBuilder sb = new StringBuilder();
        for (Taggable target : targets) {
            sb.append("A small spark zips from").append(caster.getColorTaggedName())
                    .append("'s finger and flies toward").append(target.getColorTaggedName()).append("!");
        }
        return new CastingMessage(caster, this, sb.toString());
    }

}
