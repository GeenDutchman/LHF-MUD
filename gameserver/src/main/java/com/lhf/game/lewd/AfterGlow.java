package com.lhf.game.lewd;

import java.util.function.Consumer;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.Attributes;
import com.lhf.game.map.Area;

public class AfterGlow extends LewdProduct {
    private final CreatureEffectSource afterglow = new CreatureEffectSource.Builder("Afterglow")
            .setPersistence(new EffectPersistence(3, TickType.ROOM))
            .setDescription("Bathing in the afterglow of what you have done.")
            .setOnApplication(new Deltas()
                    .setAttributeBonusChange(Attributes.CHA, 1).setAttributeScoreChange(Attributes.CHA, 1))
            .build();

    public AfterGlow() {
    }

    @Override
    public Consumer<Area> onLewdAreaChanges(VrijPartij party) {
        if (party != null) {
            for (ICreature participant : party.getParticipants()) {
                participant.applyEffect(new CreatureEffect(this.afterglow, participant, party.getInitiator()));
            }
        }
        return new Consumer<Area>() {

            @Override
            public void accept(Area arg0) {
                return;
            }

        };
    }
}
