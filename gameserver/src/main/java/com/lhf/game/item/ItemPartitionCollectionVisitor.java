package com.lhf.game.item;

import java.util.Collection;

import com.lhf.game.item.concrete.Item;

public class ItemPartitionCollectionVisitor extends ItemSaverVisitor {

    protected Collection<AItem> getItems() {
        return super.getItemsMap().values();
    }

    public Collection<InteractObject> getInteractObjects() {
        return super.getInteractObjectsMap().values();
    }

    public Collection<Item> getNotes() {
        return super.getNotesMap().values();
    }

    public Collection<Takeable> getTakeables() {
        return super.getTakeablesMap().values();
    }

    public Collection<Usable> getUsables() {
        return super.getUsablesMap().values();
    }

    public Collection<Equipable> getEquipables() {
        return super.getEquipablesMap().values();
    }

    public Collection<Weapon> getWeapons() {
        return super.getWeaponsMap().values();
    }

    public Collection<EquipableHiddenEffect> getEquipablesWithHiddenEffects() {
        return super.getEquipablesWithHiddenEffectsMap().values();
    }

}