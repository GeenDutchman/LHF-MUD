package com.lhf.game.item.concrete;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

public class Shield extends Item implements Equipable {
    private int AC = 2;
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        types = Collections.singletonList(EquipmentTypes.SHIELD);
        slots = Collections.singletonList(EquipmentSlots.SHIELD);
    }

    @Override
    public List<EquipmentTypes> getTypes() {
        return types;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        return slots;
    }

    @Override
    public Map<String, Integer> onEquippedBy(EquipmentOwner newOwner) {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", this.AC);
        return result;
    }

    @Override
    public Map<String, Integer> onUnequippedBy(EquipmentOwner disowner) {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", -1 * this.AC);
        return result;
    }

    @Override
    public String getDescription() {
        // TODO: tell how much it boosts player?
        return "This is a simple shield, it should protect you a little bit. \n"
                + this.printStats();
    }
}
