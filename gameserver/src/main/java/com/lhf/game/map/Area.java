package com.lhf.game.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainerMessageHandler;
import com.lhf.game.ItemContainer;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.Player;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public interface Area
        extends ItemContainer, CreatureContainerMessageHandler, Comparable<Area>, AffectableEntity<RoomEffect> {

    public interface AreaBuilder {
        public abstract String getName();

        public abstract String getDescription();

        public abstract Land getLand();

        public abstract Collection<Item> getItems();

        public abstract Collection<Creature> getCreatures();

        public abstract MessageHandler getSuccessor();

        public abstract Area build();
    }

    public abstract UUID getUuid();

    public abstract Creature removeCreature(Creature c, Directions dir);

    public abstract Land getLand();

    public abstract void setLand(Land land);

    @Override
    default SeeOutMessage produceMessage() {
        return this.produceMessage(false, true);
    }

    default SeeOutMessage produceMessage(boolean seeInvisible, boolean seeDirections) {
        SeeOutMessage seen = new SeeOutMessage(this);
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
        for (Creature c : this.getCreatures()) {
            if (c instanceof Player) {
                seen.addSeen(SeeCategory.PLAYER, c);
            } else if (c instanceof Monster) {
                seen.addSeen(SeeCategory.MONSTER, c);
            } else if (c instanceof NonPlayerCharacter) {
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
        return seen;
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