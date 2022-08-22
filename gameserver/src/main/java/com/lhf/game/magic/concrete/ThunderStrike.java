package com.lhf.game.magic.concrete;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.messages.out.CastingMessage;

public class ThunderStrike extends CreatureTargetingSpellEntry {

    private static final Set<CreatureEffectSource> spellEffects = Set.of(
            new CreatureEffectSource("Loud Zap", new EffectPersistence(TickType.INSTANT), "Zaps your target", false)
                    .addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.THUNDER))
                    .addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.LIGHTNING)));

    public ThunderStrike() {
        super(1, "Thunder Strike", "Bonearge Laarzen", spellEffects, Set.of(VocationName.MAGE),
                "A small but loud bolt of electricity shocks a creature you choose as a target",
                true);
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
