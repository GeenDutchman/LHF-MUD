package com.lhf.game.map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.server.interfaces.NotNull;

public class DMRoomEffectSource extends RoomEffectSource {

    protected final boolean ensoulUserAndSend;

    public static class Builder extends RoomEffectSource.AbstractBuilder<Builder> {
        private boolean ensoulUserAndSend;

        public Builder(String name) {
            super(name);
        }

        public boolean isEnsoulUserAndSend() {
            return ensoulUserAndSend;
        }

        public Builder setEnsoulUserAndSend(boolean ensoulUserAndSend) {
            this.ensoulUserAndSend = ensoulUserAndSend;
            return getThis();
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public DMRoomEffectSource build() {
            return new DMRoomEffectSource(getThis());
        }

    }

    public DMRoomEffectSource(Builder builder) {
        super(builder);
        this.ensoulUserAndSend = builder.isEnsoulUserAndSend();
    }

    @Override
    public DMRoomEffectSource makeCopy() {
        return new DMRoomEffectSource(this);
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
