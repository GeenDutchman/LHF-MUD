package com.lhf.game.magic.concrete;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.DMRoomTargetingSpellEntry;
import com.lhf.game.map.DMRoomEffectSource;
import com.lhf.messages.out.CastingMessage;

public class Ensouling extends DMRoomTargetingSpellEntry {
    private static final Set<DMRoomEffectSource> spellEffects = Set.of(new DMRoomEffectSource("Ensoul and send",
            new EffectPersistence(TickType.INSTANT), null, "Ensouls a user and sends them off into the dungeons!"));

    public Ensouling() {
        super(10, "Ensouling", "heresabodyandgo", spellEffects, Set.of(VocationName.DUNGEON_MASTER),
                "A way to create a player by ensouling them with a user.", false, false, true, true);
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        StringBuilder sb = new StringBuilder();
        if (targets != null && targets.size() > 0) {
            sb.append(caster.getColorTaggedName()).append(" will now ensoul ");
            StringJoiner sj = new StringJoiner(" and ");
            for (Taggable target : targets) {
                sj.add(target.getColorTaggedName());
            }
            sb.append(sj.toString()).append(".");
        }

        return new CastingMessage(caster, this, sb.toString());
    }

}
