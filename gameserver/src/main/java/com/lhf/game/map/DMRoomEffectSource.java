package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.server.interfaces.NotNull;

public class DMRoomEffectSource extends RoomEffectSource {

    protected final boolean ensoulUserAndSend;

    public DMRoomEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description) {
        super(name, persistence, resistance, description);
        this.ensoulUserAndSend = false;
    }

    public DMRoomEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description, boolean ensoulUserAndSend) {
        super(name, persistence, resistance, description);
        this.ensoulUserAndSend = ensoulUserAndSend;
    }

    public DMRoomEffectSource(@NotNull DMRoomEffectSource other) {
        super(other.name, other.persistence, other.resistance, other.description);
        this.ensoulUserAndSend = other.ensoulUserAndSend;
    }

    public DMRoomEffectSource(@NotNull RoomEffectSource sub) {
        super(sub);
        this.ensoulUserAndSend = false;
    }

    public boolean isEnsoulsUserAndSend() {
        return ensoulUserAndSend;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        if (this.ensoulUserAndSend) {
            sb.append(" This spell will ensoul and send off users. ");
        }
        return sb.toString();
    }
}
