package com.lhf.game.item;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.game.item.IItem.ItemID;
import com.lhf.game.item.concrete.Item;

public class ItemSaverVisitor implements ItemVisitor {
    private final Map<ItemID, InteractObject> interactObjects = new LinkedHashMap<>();
    private final Map<ItemID, Item> notes = new LinkedHashMap<>();
    private final Map<ItemID, Takeable> takeables = new LinkedHashMap<>();
    private final Map<ItemID, Usable> usables = new LinkedHashMap<>();
    private final Map<ItemID, EquipableHiddenEffect> equipablesWithHiddenEffects = new LinkedHashMap<>();
    private final Map<ItemID, Equipable> equipables = new LinkedHashMap<>();
    private final Map<ItemID, Weapon> weapons = new LinkedHashMap<>();

    @Override
    public void visit(InteractObject interactObject) {
        if (interactObject == null) {
            return;
        }
        this.interactObjects.put(interactObject.getItemID(), interactObject);
    }

    @Override
    public void visit(Item note) {
        if (note == null) {
            return;
        }
        this.notes.put(note.getItemID(), note);
    }

    @Override
    public void visit(Takeable takeable) {
        if (takeable == null) {
            return;
        }
        this.takeables.put(takeable.getItemID(), takeable);
    }

    @Override
    public void visit(Usable usable) {
        if (usable == null) {
            return;
        }
        this.usables.put(usable.getItemID(), usable);
    }

    @Override
    public void visit(Equipable equipable) {
        if (equipable == null) {
            return;
        }
        this.equipables.put(equipable.getItemID(), equipable);
    }

    @Override
    public void visit(Weapon weapon) {
        if (weapon == null) {
            return;
        }
        this.weapons.put(weapon.getItemID(), weapon);
    }

    @Override
    public void visit(EquipableHiddenEffect equipableHiddenEffect) {
        if (equipableHiddenEffect == null) {
            return;
        }
        this.equipablesWithHiddenEffects.put(equipableHiddenEffect.getItemID(), equipableHiddenEffect);
    }

    public Map<ItemID, AItem> getItemsMap() {
        return Collections.unmodifiableMap(Stream
                .concat(this.getTakeablesMap().entrySet().stream(),
                        Stream.concat(this.getInteractObjectsMap().entrySet().stream(),
                                this.getNotesMap().entrySet().stream()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new)));
    }

    public Map<ItemID, InteractObject> getInteractObjectsMap() {
        return Collections.unmodifiableMap(interactObjects);
    }

    public Map<ItemID, Item> getNotesMap() {
        return Collections.unmodifiableMap(notes);
    }

    public Map<ItemID, Takeable> getTakeablesMap() {
        return Collections.unmodifiableMap(
                Stream.concat(takeables.entrySet().stream(), this.getUsablesMap().entrySet().stream()).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new)));
    }

    public Map<ItemID, Usable> getUsablesMap() {
        return Collections.unmodifiableMap(
                Stream.concat(usables.entrySet().stream(), this.getEquipablesMap().entrySet().stream()).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new)));
    }

    public Map<ItemID, Equipable> getEquipablesMap() {
        return Collections.unmodifiableMap(Stream
                .concat(Stream.concat(equipables.entrySet().stream(), this.getWeaponsMap().entrySet().stream()),
                        this.getEquipablesWithHiddenEffectsMap().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new)));
    }

    public Map<ItemID, Weapon> getWeaponsMap() {
        return Collections.unmodifiableMap(weapons);
    }

    public Map<ItemID, EquipableHiddenEffect> getEquipablesWithHiddenEffectsMap() {
        return Collections.unmodifiableMap(equipablesWithHiddenEffects);
    }

}
