package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.game.item.concrete.InteractDoor;
import com.lhf.game.item.concrete.Item;

public class ItemPartitionListVisitor implements ItemVisitor {

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

    protected List<AItem> getItems() {
        ArrayList<AItem> consolidated = new ArrayList<>(this.getTakeables());
        consolidated.addAll(this.getInteractObjects());
        consolidated.addAll(this.notes);
        return Collections.unmodifiableList(consolidated);
    }

    public List<InteractObject> getInteractObjects() {
        ArrayList<InteractObject> consolidated = new ArrayList<>(this.interactDoors);
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

}