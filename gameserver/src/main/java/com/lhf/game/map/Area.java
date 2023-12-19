package com.lhf.game.map;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.ItemContainer;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.Player;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.TickMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public interface Area
        extends ItemContainer, CreatureContainer, MessageChainHandler, Comparable<Area>, AffectableEntity<RoomEffect> {

    public interface AreaBuilder {
        public abstract String getName();

        public abstract String getDescription();

        public abstract Land getLand();

        public abstract Collection<Item> getItems();

        public abstract Collection<ICreature> getCreatures();

        public abstract MessageChainHandler getSuccessor();

        public abstract Area build();
    }

    public abstract UUID getUuid();

    public abstract ICreature removeCreature(ICreature c, Directions dir);

    public abstract Land getLand();

    public abstract void setLand(Land land);

    @Override
    default SeeOutMessage produceMessage() {
        return this.produceMessage(false, true);
    }

    default SeeOutMessage produceMessage(boolean seeInvisible, boolean seeDirections) {
        SeeOutMessage.Builder seen = SeeOutMessage.getBuilder().setExaminable(this);
        if (seeDirections) {
            if (this.getLand() == null) {
                seen.addExtraInfo("There is no apparent way out of here.");
            } else {
                Land land = this.getLand();
                Map<Directions, Doorway> exits = land.getAreaExits(this);
                if (exits != null) {
                    for (Directions dir : exits.keySet()) {
                        seen.addSeen(SeeCategory.DIRECTION, dir);
                    }
                }
            }
        }
        for (ICreature c : this.getCreatures()) {
            if (c instanceof Player) {
                seen.addSeen(SeeCategory.PLAYER, c);
            } else if (c instanceof Monster) {
                seen.addSeen(SeeCategory.MONSTER, c);
            } else if (c instanceof INonPlayerCharacter) {
                seen.addSeen(SeeCategory.NPC, c);
            } else {
                seen.addSeen(SeeCategory.CREATURE, c);
            }
        }
        for (Item item : this.getItems()) {
            if (!item.checkVisibility() && !seeInvisible) {
                continue;
            }
            if (item instanceof Takeable) {
                seen.addSeen(item.checkVisibility() ? SeeCategory.TAKEABLE : SeeCategory.INVISIBLE_TAKEABLE,
                        item);
            } else {
                seen.addSeen(item.checkVisibility() ? SeeCategory.ROOM_ITEM : SeeCategory.INVISIBLE_ROOM_ITEM,
                        item);
            }
        }
        return seen.Build();
    }

    @Override
    default void tick(TickType type) {
        AffectableEntity.super.tick(type);
        this.announce(TickMessage.getBuilder().setTickType(type).setBroacast());
    }

    @Override
    public default int compareTo(Area o) {
        if (this.equals(o)) {
            return 0;
        }
        int nameCompare = this.getName().compareTo(o.getName());
        if (nameCompare != 0) {
            return nameCompare;
        }
        return this.getUuid().compareTo(o.getUuid());
    }
}
