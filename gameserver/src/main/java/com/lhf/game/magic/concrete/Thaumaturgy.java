package com.lhf.game.magic.concrete;

import java.util.List;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.magic.RoomTargetingSpellEntry;
import com.lhf.messages.out.CastingMessage;

public class Thaumaturgy extends RoomTargetingSpellEntry {

    public Thaumaturgy() {
        super(0, "Thaumaturgy", "zarmamoo", new EffectPersistence(TickType.INSTANT),
                "A way to magically announce your presence",
                false, false);
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
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
        return new CastingMessage(caster, this, sb.toString());
    }

}
