package com.lhf.game.map;

import java.util.Collections;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;

public class DMRoomEffect extends RoomEffect {
    public DMRoomEffect(DMRoomEffectSource source, Creature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
    }

    public DMRoomEffect(RoomEffect sub) {
        super(sub);
    }

    public DMRoomEffectSource getSource() {
        return (DMRoomEffectSource) this.source;
    }

    public Set<String> getUsernamesToEnsoul() {
        return Collections.unmodifiableSet(this.getSource().getUsernamesToEnsoul());
    }

    public Set<String> getNamesToSendOff() {
        return Collections.unmodifiableSet(this.getNamesToSendOff());
    }
}
