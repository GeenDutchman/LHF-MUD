package com.lhf.game;

import com.lhf.game.EffectPersistence.TickType;
import com.lhf.messages.out.OutMessage;

public interface AffectableEntity {
    OutMessage applyEffect(EntityEffect effect, boolean reverse);

    void tick(TickType type);
}
