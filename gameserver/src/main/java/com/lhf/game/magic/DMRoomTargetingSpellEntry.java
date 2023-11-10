package com.lhf.game.magic;

import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.SpellLevel;
import com.lhf.game.map.DMRoomEffectSource;

public class DMRoomTargetingSpellEntry extends RoomTargetingSpellEntry {
    private Boolean ensoulsUsers = null;

    public DMRoomTargetingSpellEntry(SpellLevel level, String name, String invocation,
            Set<DMRoomEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean banishesItems, boolean banishesCreatures) {
        super(level, name, invocation, effectSources, allowed, description, banishesItems, banishesCreatures);
    }

    public DMRoomTargetingSpellEntry(SpellLevel level, String name, Set<DMRoomEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean banishesItems, boolean banishesCreatures) {
        super(level, name, effectSources, allowed, description, banishesItems, banishesCreatures);
    }

    public boolean isEnsoulsUsers() {
        if (this.ensoulsUsers == null) {
            this.ensoulsUsers = false;
            for (EntityEffectSource source : this.getEffectSources()) {
                if (source instanceof DMRoomEffectSource) {
                    DMRoomEffectSource dmRoomEffectSource = (DMRoomEffectSource) source;
                    if (dmRoomEffectSource.isEnsoulsUserAndSend()) {
                        this.ensoulsUsers = true;
                        return this.ensoulsUsers;
                    }
                }
            }
        }
        return ensoulsUsers;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        if (this.isEnsoulsUsers()) {
            sb.append("This spell will ensoul and send off users. ").append("\r\n");
        }
        return sb.toString();
    }

}
