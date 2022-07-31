package com.lhf.game.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;
import com.lhf.messages.MessageHandler;

public class RoomBuilder {
    private String name;
    private String description;
    private List<Item> items;
    private Set<Creature> creatures;
    private Dungeon dungeon;
    private MessageHandler successor;

    public static RoomBuilder getInstance() {
        return new RoomBuilder();
    }

    private RoomBuilder() {
    }

    public RoomBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public RoomBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public RoomBuilder addItem(Item item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        return this;
    }

    public RoomBuilder addCreature(Creature creature) {
        if (this.creatures == null) {
            this.creatures = new HashSet<>();
        }
        this.creatures.add(creature);
        return this;
    }

    public RoomBuilder setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
        return this;
    }

    public RoomBuilder setSuccessor(MessageHandler successor) {
        this.successor = successor;
        return this;
    }

    public Room build() {
        Room room;
        if (this.description == null) {
            room = new Room(this.name);
        } else {
            room = new Room(this.name, this.description);
        }
        if (this.dungeon != null) {
            room.setDungeon(this.dungeon);
        }
        if (this.items != null) {
            for (Item i : this.items) {
                room.addItem(i);
            }
        }
        if (this.creatures != null) {
            for (Creature c : this.creatures) {
                room.addCreature(c);
            }
        }
        if (this.successor != null) {
            room.setSuccessor(this.successor);
        }
        return room;
    }
}
