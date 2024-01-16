package com.lhf.game.item;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.game.item.IItem.ItemID;
import com.lhf.game.item.concrete.NotableFixture;

public final class ItemSaverVisitor implements ItemVisitor {
    private final Map<ItemID, IItem> itemMap = new TreeMap<>();

    public ItemSaverVisitor() {
    }

    public Map<ItemID, IItem> getItemMap() {
        return Collections.unmodifiableMap(this.itemMap);
    }

    @Override
    public void visit(InteractObject interactObject) {
        if (interactObject == null) {
            return;
        }
        this.itemMap.put(interactObject.getItemID(), interactObject);
    }

    @Override
    public void visit(NotableFixture note) {
        if (note == null) {
            return;
        }
        this.itemMap.put(note.getItemID(), note);
    }

    @Override
    public void visit(Takeable takeable) {
        if (takeable == null) {
            return;
        }
        this.itemMap.put(takeable.getItemID(), takeable);
    }

    @Override
    public void visit(Usable usable) {
        if (usable == null) {
            return;
        }
        this.itemMap.put(usable.getItemID(), usable);
    }

    @Override
    public void visit(Equipable equipable) {
        if (equipable == null) {
            return;
        }
        this.itemMap.put(equipable.getItemID(), equipable);
    }

    @Override
    public void visit(Weapon weapon) {
        if (weapon == null) {
            return;
        }
        this.itemMap.put(weapon.getItemID(), weapon);
    }

    @Override
    public void visit(EquipableHiddenEffect equipableHiddenEffect) {
        if (equipableHiddenEffect == null) {
            return;
        }
        this.itemMap.put(equipableHiddenEffect.getItemID(), equipableHiddenEffect);
    }

}
