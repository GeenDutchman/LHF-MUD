package com.lhf.game.item;

import java.util.Optional;

import com.lhf.game.item.concrete.NotableFixture;

public class ItemNameSearchVisitor extends ItemPartitionListVisitor {
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

    public void copyFrom(ItemPartitionListVisitor partitioner) {
        if (partitioner == null) {
            return;
        }
        partitioner.getItems().stream().filter(item -> item != null)
                .forEachOrdered(item -> item.acceptItemVisitor(this));
    }

    private boolean checkItemName(IItem item) {
        if (item == null) {
            return false;
        }
        if (this.searchName == null || this.searchName.isEmpty()) {
            return true;
        }
        return this.regexLength != null ? item.CheckNameRegex(searchName, regexLength) : item.checkName(searchName);
    }

    @Override
    public void visit(InteractObject interactObject) {
        if (this.checkItemName(interactObject)) {
            super.visit(interactObject);
        }
    }

    @Override
    public void visit(NotableFixture note) {
        if (this.checkItemName(note)) {
            super.visit(note);
        }
    }

    @Override
    public void visit(Takeable takeable) {
        if (this.checkItemName(takeable)) {
            super.visit(takeable);
        }
    }

    @Override
    public void visit(Usable usable) {
        if (this.checkItemName(usable)) {
            super.visit(usable);
        }
    }

    @Override
    public void visit(Equipable equipable) {
        if (this.checkItemName(equipable)) {
            super.visit(equipable);
        }
    }

    @Override
    public void visit(Weapon weapon) {
        if (this.checkItemName(weapon)) {
            super.visit(weapon);
        }
    }

    @Override
    public void visit(EquipableHiddenEffect equipableHiddenEffect) {
        if (this.checkItemName(equipableHiddenEffect)) {
            super.visit(equipableHiddenEffect);
        }
    }

    public Optional<InteractObject> getInteractObject() {
        return super.getInteractObjects().stream().findFirst();
    }

    public Optional<NotableFixture> getNote() {
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
