package com.lhf.game.map;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation;

public class DMRoomEffect extends RoomEffect {
    private final String ensoulUsername;
    private final Vocation vocation;

    public DMRoomEffect(DMRoomEffectSource source, Creature creatureResponsible, Taggable generatedBy,
            String ensoulUsername, Vocation vocation) {
        super(source, creatureResponsible, generatedBy);
        if (source.isEnsoulsUserAndSend()) {
            this.ensoulUsername = ensoulUsername;
            this.vocation = vocation;
        } else {
            this.ensoulUsername = null;
            this.vocation = null;
        }
    }

    public DMRoomEffect(RoomEffect sub) {
        super(sub);
        this.ensoulUsername = null;
        this.vocation = null;
    }

    public DMRoomEffectSource getSource() {
        return (DMRoomEffectSource) this.source;
    }

    public String getEnsoulUsername() {
        return ensoulUsername;
    }

    public Vocation getVocation() {
        return vocation;
    }

}
