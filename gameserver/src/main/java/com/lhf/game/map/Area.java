package com.lhf.game.map;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.ItemContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.Player;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

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
    default SeeEvent produceMessage() {
        return this.produceMessage(false, true);
    }

    default SeeEvent produceMessage(boolean seeInvisible, boolean seeDirections) {
        SeeEvent.Builder seen = SeeEvent.getBuilder().setExaminable(this);
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
            } else if (c instanceof IMonster) {
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
    public default Collection<ClientMessenger> getClientMessengers() {
        TreeSet<ClientMessenger> messengers = new TreeSet<>(ClientMessenger.getComparator());

        this.getCreatures().stream()
                .filter(creature -> creature != null).forEach(messenger -> messengers.add(messenger));

        return Collections.unmodifiableCollection(messengers);
    }

    @Override
    public default Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }
            if (event instanceof ITickEvent tickEvent) {
                this.tick(tickEvent);
            }
            this.announceDirect(event, this.getClientMessengers());
        };
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

    @Override
    public default String getStartTag() {
        return "<area>";
    }

    @Override
    public default String getEndTag() {
        return "</area>";
    }

    @Override
    public default String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }
}
