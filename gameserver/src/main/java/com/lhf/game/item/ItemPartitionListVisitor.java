package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringJoiner;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.concrete.InteractDoor;
import com.lhf.game.item.concrete.Item;

public class ItemPartitionListVisitor implements ItemVisitor, ItemContainer {

    private final List<InteractObject> interactObjects = new ArrayList<>();
    private final List<InteractDoor> interactDoors = new ArrayList<>();
    private final List<Item> notes = new ArrayList<>();
    private final List<Takeable> takeables = new ArrayList<>();
    private final List<Usable> usables = new ArrayList<>();
    private final List<EquipableHiddenEffect> equipablesWithHiddenEffects = new ArrayList<>();
    private final List<Equipable> equipables = new ArrayList<>();
    private final List<Weapon> weapons = new ArrayList<>();

    @Override
    public void visit(InteractObject interactObject) {
        if (interactObject == null) {
            return;
        }
        this.interactObjects.add(interactObject);
    }

    @Override
    public void visit(InteractDoor door) {
        if (door == null) {
            return;
        }
        this.interactDoors.add(door);
    }

    @Override
    public void visit(Item note) {
        if (note == null) {
            return;
        }
        this.notes.add(note);
    }

    @Override
    public void visit(Takeable takeable) {
        if (takeable == null) {
            return;
        }
        this.takeables.add(takeable);
    }

    @Override
    public void visit(Usable usable) {
        if (usable == null) {
            return;
        }
        this.usables.add(usable);
    }

    @Override
    public void visit(Equipable equipable) {
        if (equipable == null) {
            return;
        }
        this.equipables.add(equipable);
    }

    @Override
    public void visit(Weapon weapon) {
        if (weapon == null) {
            return;
        }
        this.weapons.add(weapon);
    }

    @Override
    public void visit(EquipableHiddenEffect equipableHiddenEffect) {
        if (equipableHiddenEffect == null) {
            return;
        }
        this.equipablesWithHiddenEffects.add(equipableHiddenEffect);
    }

    public Collection<IItem> getItems() {
        ArrayList<IItem> consolidated = new ArrayList<>(this.takeables);
        consolidated.addAll(this.getInteractObjects());
        consolidated.addAll(this.notes);
        return Collections.unmodifiableCollection(consolidated);
    }

    public List<AItem> getItemsList() {
        ArrayList<AItem> consolidated = new ArrayList<>(this.getTakeables());
        consolidated.addAll(this.getInteractObjects());
        consolidated.addAll(this.notes);
        return Collections.unmodifiableList(consolidated);
    }

    public List<InteractObject> getInteractObjects() {
        ArrayList<InteractObject> consolidated = new ArrayList<>(this.interactObjects);
        consolidated.addAll(this.interactDoors);
        return Collections.unmodifiableList(consolidated);
    }

    public List<InteractDoor> getInteractDoors() {
        return Collections.unmodifiableList(this.interactDoors);
    }

    public List<Item> getNotes() {
        return Collections.unmodifiableList(this.notes);
    }

    public List<Takeable> getTakeables() {
        List<Takeable> consolidated = new ArrayList<>(this.takeables);
        consolidated.addAll(this.getUsables());
        return Collections.unmodifiableList(consolidated);
    }

    public List<Usable> getUsables() {
        List<Usable> consolidated = new ArrayList<>(this.usables);
        consolidated.addAll(this.getEquipables());
        return Collections.unmodifiableList(consolidated);
    }

    public List<Equipable> getEquipables() {
        List<Equipable> consolidated = new ArrayList<>(this.equipables);
        consolidated.addAll(this.weapons);
        consolidated.addAll(this.equipablesWithHiddenEffects);
        return Collections.unmodifiableList(consolidated);
    }

    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public List<EquipableHiddenEffect> getEquipablesWithHiddenEffects() {
        return Collections.unmodifiableList(equipablesWithHiddenEffects);
    }

    public List<InteractObject> editInteractObjects() {
        return this.interactObjects;
    }

    public List<InteractDoor> editInteractDoors() {
        return this.interactDoors;
    }

    public List<Item> editNotes() {
        return this.notes;
    }

    public List<Takeable> editTakeables() {
        return this.takeables;
    }

    public List<Usable> editUsables() {
        return this.usables;
    }

    public List<EquipableHiddenEffect> editEquipableHiddenEffects() {
        return this.equipablesWithHiddenEffects;
    }

    public List<Equipable> editEquipables() {
        return this.equipables;
    }

    public List<Weapon> editWeapons() {
        return this.weapons;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return "A set of partitioned lists of items.";
    }

    @Override
    public boolean addItem(IItem item) {
        if (item == null) {
            return false;
        }
        this.accept(item);
        return true;
    }

    @Override
    public Optional<IItem> removeItem(String name) {
        if (name == null) {
            return Optional.empty();
        }
        for (List<? extends IItem> sublist : List.of(this.weapons, this.equipables, this.equipablesWithHiddenEffects,
                this.usables, this.takeables, this.notes, this.interactDoors, this.interactObjects)) {
            Iterator<? extends IItem> iter = sublist.iterator();
            while (iter.hasNext()) {
                IItem item = iter.next();
                if (item == null) {
                    iter.remove();
                    continue;
                }
                if (item.CheckNameRegex(name, 3)) {
                    return Optional.of(item);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeItem(IItem item) {
        if (item == null) {
            return false;
        }
        for (List<? extends IItem> sublist : List.of(this.weapons, this.equipables, this.equipablesWithHiddenEffects,
                this.usables, this.takeables, this.notes, this.interactDoors, this.interactObjects)) {
            Iterator<? extends IItem> iter = sublist.iterator();
            while (iter.hasNext()) {
                IItem iterItem = iter.next();
                if (iterItem == null) {
                    iter.remove();
                    continue;
                }
                if (iterItem.equals(item)) {
                    iter.remove();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<? extends IItem> itemIterator() {
        List<Iterator<? extends IItem>> iterators = List.of(this.weapons.iterator(), this.equipables.iterator(),
                this.equipablesWithHiddenEffects.iterator(), this.usables.iterator(), this.takeables.iterator(),
                this.notes.iterator(), this.interactDoors.iterator(), this.interactObjects.iterator());
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                Iterator<Iterator<? extends IItem>> metaIterator = iterators.iterator();
                while (metaIterator.hasNext()) {
                    Iterator<? extends IItem> subIter = metaIterator.next();
                    if (subIter.hasNext()) {
                        return true;
                    }
                    metaIterator.remove();
                }
                return false;
            }

            @Override
            public IItem next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException("hasNext(): false");
                }
                Iterator<? extends IItem> nextIter = iterators.get(0);
                if (nextIter == null) {
                    throw new NoSuchElementException("No iters left");
                }
                return nextIter.next();
            }

            @Override
            public void remove() {
                Iterator<? extends IItem> nextIter = iterators.get(0);
                if (nextIter != null) {
                    nextIter.remove();
                }
            }

        };
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        StringJoiner sj = new StringJoiner(", ", " [", "]");
        if (interactObjects.size() > 0) {
            sj.add("interactObjects=" + interactObjects.toString());
        }
        if (interactDoors.size() > 0) {
            sj.add("interactDoors=" + interactDoors.toString());
        }
        if (notes.size() > 0) {
            sj.add("notes=" + notes.toString());
        }
        if (takeables.size() > 0) {
            sj.add("takeables=" + takeables.toString());
        }
        if (usables.size() > 0) {
            sj.add("usables=" + usables.toString());
        }
        if (equipablesWithHiddenEffects.size() > 0) {
            sj.add("equipablesWithHiddenEffects=" + equipablesWithHiddenEffects.toString());
        }
        if (equipables.size() > 0) {
            sj.add("equipables=" + equipables.toString());
        }
        if (weapons.size() > 0) {
            sj.add("weapons=" + weapons.toString());
        }
        builder.append(sj.toString());
        return builder.toString();
    }

}