package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.game.item.concrete.Item;

public class ItemPartitionListVisitor implements ItemVisitor {
    private final List<InteractObject> interactObjects = new ArrayList<>();
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
        return Stream
                .concat(this.getTakeables().stream(),
                        Stream.concat(this.getInteractObjects().stream(), this.getNotes().stream()))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<InteractObject> getInteractObjects() {
        return Collections.unmodifiableList(interactObjects);
    }

    public List<Item> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public List<Takeable> getTakeables() {
        return Stream.concat(takeables.stream(), this.getUsables().stream()).collect(Collectors.toUnmodifiableList());
    }

    public List<Usable> getUsables() {
        return Stream.concat(usables.stream(), this.getEquipables().stream()).collect(Collectors.toUnmodifiableList());
    }

    public List<Equipable> getEquipables() {
        return Stream.concat(Stream.concat(equipables.stream(), this.getWeapons().stream()),
                this.getEquipablesWithHiddenEffects().stream()).collect(Collectors.toUnmodifiableList());
    }

    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public List<EquipableHiddenEffect> getEquipablesWithHiddenEffects() {
        return Collections.unmodifiableList(equipablesWithHiddenEffects);
    }

}