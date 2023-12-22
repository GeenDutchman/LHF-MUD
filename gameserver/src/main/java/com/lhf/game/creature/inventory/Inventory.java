package com.lhf.game.creature.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.lhf.game.ItemContainer;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.events.InventoryRequestedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class Inventory implements ItemContainer {
    private List<Takeable> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    @Override
    public Collection<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    @Override
    public String getName() {
        return "Inventory";
    }

    @Override
    public boolean addItem(Item i) {
        if (i instanceof Takeable) {
            this.items.add((Takeable) i);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        return this.items.iterator();
    }

    @Override
    public String toString() {
        return this.items.stream().map(item -> item.getColorTaggedName()).collect(Collectors.joining(", "));
    }

    public InventoryRequestedEvent getInventoryOutMessage() {
        return this.getInventoryOutMessage(null);
    }

    public InventoryRequestedEvent getInventoryOutMessage(Map<EquipmentSlots, Equipable> equipment) {
        return InventoryRequestedEvent.getBuilder().setItems(this.items).setEquipment(equipment).Build();
    }

    public String toStoreString() {
        return this.items.stream().map(Takeable::getName).collect(Collectors.joining(", "));
    }

    public List<String> getItemList() {
        List<String> names = new ArrayList<>();
        for (Takeable t : items) {
            names.add(t.getName());
        }
        return names;
    }

    @Override
    public boolean removeItem(Item item) {
        return this.items.remove(item);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        for (Item exact : this.items) {
            if (exact.CheckNameRegex(name, 3)) {
                this.items.remove(exact);
                return Optional.of(exact);
            }
        }
        return Optional.empty();
    }

    @Override
    public String printDescription() {
        return "This is your inventory.";
    }

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        for (Takeable thing : this.items) {
            seeOutMessage.addSeen(SeeCategory.TAKEABLE, thing);
        }
        return seeOutMessage.Build();
    }

}
