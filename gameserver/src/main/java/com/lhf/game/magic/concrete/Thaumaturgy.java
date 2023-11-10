package com.lhf.game.magic.concrete;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.SpellLevel;
import com.lhf.game.magic.RoomTargetingSpellEntry;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.messages.out.CastingMessage;

public class Thaumaturgy extends RoomTargetingSpellEntry {

    private static final Set<RoomEffectSource> spellEffects = Set.of(new RoomEffectSource("Announce yourself",
            new EffectPersistence(TickType.INSTANT),
            new EffectResistance(Attributes.CHA, 5, TargetResistAmount.ALL),
            "Announce yourself to the room!"));

    public Thaumaturgy() {
        super(SpellLevel.CANTRIP, "Thaumaturgy", "zarmamoo", spellEffects, new HashSet<VocationName>(),
                "A way to magically announce your presence", false, false);
    }

    @Override
    public CastingMessage Cast(Creature caster, SpellLevel castLevel, List<? extends Taggable> targets) {
        StringBuilder sb = new StringBuilder();
        String casterName = caster.getName();
        String[] splitname = casterName.split("( |_)");
        int longest = 0;
        for (String split : splitname) {
            if (split.length() > longest) {
                longest = split.length();
            }
        }
        sb.append(caster.getStartTag()).append("\\").append("|".repeat(longest)).append("/")
                .append(caster.getEndTag()).append("\n");
        for (String split : splitname) {
            sb.append(caster.getStartTag()).append("-").append(split)
                    .append(" ".repeat(longest - split.length())).append("-").append(caster.getEndTag())
                    .append("\n");
        }
        sb.append(caster.getStartTag()).append("/").append("|".repeat(longest)).append("\\")
                .append(caster.getEndTag()).append("\n");
        return CastingMessage.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sb.toString()).Build();
    }

}
