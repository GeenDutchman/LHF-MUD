package com.lhf.game.item.concrete;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.item.interfaces.Usable;

import java.util.*;

public class CarnivorousArmor extends Usable implements Equipable {
    private int AC = 2;
    private int eatsHealthTo = 5;
    private int rewardAC = 3;
    private boolean equipped = false;
    private boolean equippedAndUsed = false;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public CarnivorousArmor(boolean isVisible) {
        // deceptive name
        super(" Leather Armor ", isVisible, -1);

        slots = Collections.singletonList(EquipmentSlots.ARMOR);
        types = Arrays.asList(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);

        setUseAction(Creature.class.getName(), (object) -> {
            if (!equipped) {
                return "You need to equip this item in order to use it.";
            }
            if (object == null) {
                return "That is not a valid target at all!";
            } else if (object instanceof Creature) {
                if (equippedAndUsed) {
                    return "The " + getStartTagName() + "Carnivorous Armor" + getEndTagName()
                            + " snuggles around you as you poke at it, but otherwise does nothing.";
                }
                Integer currHealth = ((Creature) object).getStats().get(Stats.CURRENTHP);
                if (currHealth > eatsHealthTo) {
                    int diff = currHealth - eatsHealthTo;
                    ((Creature) object).updateHitpoints(-1 * diff);
                    ((Creature) object).updateAc(rewardAC);
                    equippedAndUsed = true;
                    return "A thousand teeth sink into your body, and you feel life force ripped out of you.  " +
                            "Once it is sated, you feel the " + getStartTagName() + "Carnivorous Armor"
                            + getEndTagName() +
                            " tighten up around its most recent, precious meal.  It leaves the rest for later.";
                } else {
                    return "You need more health to use this item.";
                }
            }
            return "You cannot use this on that!  You can only use it on yourself.";
        });
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
    public Map<String, Integer> equip() {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", this.AC);
        this.equipped = true;
        this.equippedAndUsed = false;
        return result;
    }

    @Override
    public Map<String, Integer> unequip() {
        Map<String, Integer> result = new HashMap<>();
        if (!equippedAndUsed) {
            result.put("AC", -1 * this.AC);
        } else {
            result.put("AC", -1 * (this.AC + this.rewardAC));
            result.put("CURRENTHP", -2);
        }
        equipped = false;
        equippedAndUsed = false;
        return result;
    }

    @Override
    public String getDescription() {
        return "This is some simple leather armor. " + "There is plenty of blood on it...\n" +
                this.printStats();
    }
}
