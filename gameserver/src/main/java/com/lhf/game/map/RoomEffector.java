package com.lhf.game.map;

import java.util.List;
import java.util.Set;

import com.lhf.game.EntityEffect;
import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;

public interface RoomEffector extends EntityEffect {
    public List<Item> getItemsToSummon();

    public List<Item> getItemsToBanish();

    public Set<Creature> getCreaturesToSummon();

    public Set<Creature> getCreaturesToBanish();
}
