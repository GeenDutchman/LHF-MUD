package com.lhf.game.map;

import com.lhf.game.inventory.*;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.EquipType;
import com.lhf.game.map.objects.item.interfaces.Equipable;
import com.lhf.game.map.objects.item.interfaces.Takeable;
import com.lhf.user.UserID;

import java.util.Optional;

public class Player implements InventoryOwner, EquipmentOwner {
    private UserID id;
    private boolean inBattle;
    private Inventory inventory;
    private Equipment equipment;

    public Player(UserID id) {
        inventory = new Inventory();
        equipment = new Equipment();
        this.id = id;
        inBattle = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        Player p = (Player)obj;
        return p.getId().equals(getId());
    }

    public UserID getId() {
        return id;
    }

    @Override
    public void takeItem(Takeable item) {
        this.inventory.addItem(item);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void useItem(int itemIndex) {

    }

    @Override
    public boolean equipItem(String itemName) {
        Optional<Takeable> maybeItem = this.inventory.getItem(itemName);
        if (maybeItem.isPresent()) {
            Takeable item = maybeItem.get();
            if (item instanceof Equipable) {
                this.equipment.equipItem((Equipable) item);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void unequipItem(EquipType type) {
        this.equipment.unequip(type);
    }
}
