package com.lhf.game.lewd;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;

public class AfterGlow extends CreatureEffectSource implements LewdProduct {

    public AfterGlow() {
        super("Afterglow", new EffectPersistence(3, TickType.ROOM), null,
                "Bathing in the afterglow of what you have done.", false);
        this.addAttributeBonusChange(Attributes.CHA, 1);
        this.addAttributeScoreChange(Attributes.CHA, 1);
    }

    @Override
    public AfterGlow makeCopy() {
        return new AfterGlow();
    }

    public void onLewd(Area room, VrijPartij party) {
        for (ICreature participant : party.getParticipants()) {
            participant.applyEffect(new CreatureEffect(this, participant, this));
        }
    }
}
