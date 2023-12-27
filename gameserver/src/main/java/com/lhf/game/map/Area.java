package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public interface Area
        extends ItemContainer, CreatureContainer, CommandChainHandler, Comparable<Area>, AffectableEntity<RoomEffect> {

    public interface AreaBuilder extends Serializable {
        public class AreaBuilderID implements Comparable<AreaBuilderID> {
            private final UUID id = UUID.randomUUID();

            public UUID getId() {
                return id;
            }

            @Override
            public int hashCode() {
                return Objects.hash(id);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (!(obj instanceof AreaBuilderID))
                    return false;
                AreaBuilderID other = (AreaBuilderID) obj;
                return Objects.equals(id, other.id);
            }

            @Override
            public int compareTo(AreaBuilderID arg0) {
                return this.id.compareTo(arg0.id);
            }

        }

        public abstract AreaBuilderID getAreaBuilderID();

        public abstract String getName();

        public abstract String getDescription();

        public abstract Collection<Item> getItems();

        public abstract Collection<INonPlayerCharacter.AbstractNPCBuilder<?, ?>> getNPCsToBuild();

        public abstract Collection<INonPlayerCharacter> getPrebuiltNPCs();

        public abstract Area build(Land land, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException;

        public abstract Area build(CommandChainHandler successor, Land land, AIRunner aiRunner,
                StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException;
    }

    public abstract UUID getUuid();

    public abstract boolean removeCreature(ICreature c, Directions dir);

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
                Set<Directions> exits = land.getAreaExits(this);
                if (exits != null) {
                    for (Directions dir : exits) {
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
    public default Collection<GameEventProcessor> getGameEventProcessors() {
        TreeSet<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());

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
            this.announceDirect(event, this.getGameEventProcessors());
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
