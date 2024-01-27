package com.lhf.game.lewd;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;

public class AfterGlow extends LewdProduct {
    private final CreatureEffectSource afterglow = new CreatureEffectSource("Afterglow",
            new EffectPersistence(3, TickType.ROOM), null, "Bathing in the afterglow of what you have done.", false)
            .addAttributeBonusChange(Attributes.CHA, 1).addAttributeScoreChange(Attributes.CHA, 1);

    public AfterGlow() {
    }

    public void onLewd(Area room, VrijPartij party) {
        for (ICreature participant : party.getParticipants()) {
            participant.applyEffect(new CreatureEffect(this.afterglow, participant, room));
        }
    }
}
