package com.lhf.game.map;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation;

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

    public Map<String, Vocation> getUsernamesToEnsoul() {
        return Collections.unmodifiableMap(this.getSource().getUsernamesToEnsoul());
    }

    public Set<String> getNamesToSendOff() {
        return Collections.unmodifiableSet(this.getSource().getNamesToSendOff());
    }
}
