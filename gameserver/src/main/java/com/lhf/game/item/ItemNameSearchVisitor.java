package com.lhf.game.item;

import java.util.Optional;

import com.lhf.game.item.concrete.Note;

public class ItemNameSearchVisitor implements ItemVisitor {
    protected final ItemPartitionListVisitor partitions = new ItemPartitionListVisitor();
    protected final String searchName;
    protected final Integer regexLength;

    public ItemNameSearchVisitor(String searchName) {
        this.searchName = searchName;
        this.regexLength = null;
    }

    public ItemNameSearchVisitor(String searchName, Integer regexLength) {
        this.searchName = searchName;
        this.regexLength = regexLength;
    }

    private boolean checkItemName(Item item) {
        if (item == null) {
            return false;
        }
        if (this.searchName == null) {
            return true;
        }
        return this.regexLength != null ? item.CheckNameRegex(searchName, regexLength) : item.checkName(searchName);
    }

    public void visit(InteractObject interactObject) {
        if (this.checkItemName(interactObject)) {
            partitions.visit(interactObject);
        }
    }

    public void visit(Note note) {
        if (this.checkItemName(note)) {
            partitions.visit(note);
        }
    }

    public void visit(Takeable takeable) {
        if (this.checkItemName(takeable)) {
            partitions.visit(takeable);
        }
    }

    public void visit(Usable usable) {
        if (this.checkItemName(usable)) {
            partitions.visit(usable);
        }
    }

    public void visit(Equipable equipable) {
        if (this.checkItemName(equipable)) {
            partitions.visit(equipable);
        }
    }

    public void visit(Weapon weapon) {
        if (this.checkItemName(weapon)) {
            partitions.visit(weapon);
        }
    }

    public void visit(StackableItem stackableItem) {
        if (this.checkItemName(stackableItem)) {
            partitions.visit(stackableItem);
        }
    }

    public Optional<InteractObject> getInteractObject() {
        return Optional.ofNullable(partitions.getInteractObjects().get(0));
    }

    public Optional<Note> getNote() {
        return Optional.ofNullable(partitions.getNotes().get(0));
    }

    public Optional<Takeable> getTakeable() {
        return Optional.ofNullable(partitions.getTakeables().get(0));
    }

    public Optional<Usable> getUsable() {
        return Optional.ofNullable(partitions.getUsables().get(0));
    }

    public Optional<Equipable> getEquipable() {
        return Optional.ofNullable(partitions.getEquipables().get(0));
    }

    public Optional<Weapon> getWeapon() {
        return Optional.ofNullable(partitions.getWeapons().get(0));
    }

    public Optional<StackableItem> getStackableItem() {
        return Optional.ofNullable(partitions.getStackableItems().get(0));
    }

}
