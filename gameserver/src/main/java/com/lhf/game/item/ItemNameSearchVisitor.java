package com.lhf.game.item;

import java.util.Optional;

import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.ItemContainer.ItemFilters;
import com.lhf.game.item.concrete.Item;

public class ItemNameSearchVisitor extends ItemPartitionCollectionVisitor {
    protected final ItemFilterQuery query;

    public ItemNameSearchVisitor(String searchName) {
        this.query = new ItemFilterQuery();
        this.query.objectName = searchName;
        this.query.filters.add(ItemFilters.OBJECT_NAME);
    }

    public ItemNameSearchVisitor(String searchName, Integer regexLength) {
        this.query = new ItemFilterQuery();
        this.query.objectName = searchName;
        this.query.objectNameRegexLen = regexLength;
        this.query.filters.add(ItemFilters.OBJECT_NAME);
    }

    public ItemNameSearchVisitor(ItemFilterQuery query) {
        this.query = query != null ? query : new ItemFilterQuery();
    }

    public void copyFrom(ItemPartitionCollectionVisitor partitioner) {
        if (partitioner == null) {
            return;
        }
        partitioner.getItems().stream().filter(item -> item != null)
                .forEachOrdered(item -> item.acceptItemVisitor(this));
    }

    private boolean testItem(IItem item) {
        if (item == null) {
            return false;
        }
        return this.query != null ? this.query.test(item) : true;
    }

    @Override
    public void visit(InteractObject interactObject) {
        if (this.testItem(interactObject)) {
            super.visit(interactObject);
        }
    }

    @Override
    public void visit(Item note) {
        if (this.testItem(note)) {
            super.visit(note);
        }
    }

    @Override
    public void visit(Takeable takeable) {
        if (this.testItem(takeable)) {
            super.visit(takeable);
        }
    }

    @Override
    public void visit(Usable usable) {
        if (this.testItem(usable)) {
            super.visit(usable);
        }
    }

    @Override
    public void visit(Equipable equipable) {
        if (this.testItem(equipable)) {
            super.visit(equipable);
        }
    }

    @Override
    public void visit(Weapon weapon) {
        if (this.testItem(weapon)) {
            super.visit(weapon);
        }
    }

    @Override
    public void visit(EquipableHiddenEffect equipableHiddenEffect) {
        if (this.testItem(equipableHiddenEffect)) {
            super.visit(equipableHiddenEffect);
        }
    }

    public Optional<InteractObject> getInteractObject() {
        return super.getInteractObjects().stream().findFirst();
    }

    public Optional<Item> getNote() {
        return super.getNotes().stream().findFirst();
    }

    public Optional<Takeable> getTakeable() {
        return super.getTakeables().stream().findFirst();
    }

    public Optional<Usable> getUsable() {
        return super.getUsables().stream().findFirst();
    }

    public Optional<Equipable> getEquipable() {
        return super.getEquipables().stream().findFirst();
    }

    public Optional<Weapon> getWeapon() {
        return super.getWeapons().stream().findFirst();
    }

    public Optional<EquipableHiddenEffect> getEquipableHiddenEffect() {
        return super.getEquipablesWithHiddenEffects().stream().findFirst();
    }

}
