package com.lhf.game.map;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;
import com.lhf.server.interfaces.NotNull;

public class RoomEffect extends EntityEffect {

    public RoomEffect(@NotNull RoomEffect other) {
        super(other.source, other.creatureResponsible, other.generatedBy);
    }

    public RoomEffect(RoomEffectSource source, Creature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
    }

    public RoomEffectSource getSource() {
        return (RoomEffectSource) this.source;
    }

    public List<Item> getItemsToSummon() {
        return Collections.unmodifiableList(this.getSource().getItemsToSummon());
    }

    public List<Item> getItemsToBanish() {
        return Collections.unmodifiableList(this.getSource().getItemsToBanish());

    }

    public Set<Creature> getCreaturesToSummon() {
        return Collections.unmodifiableSet(this.getSource().getCreaturesToSummon());
    }

    public Set<Creature> getCreaturesToBanish() {
        return Collections.unmodifiableSet(this.getSource().getCreaturesToBanish());

    }
}
