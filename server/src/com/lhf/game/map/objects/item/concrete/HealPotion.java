package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.Dice;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.Consumable;

public class HealPotion extends Item implements Consumable {

    public enum HEALTYPE {
        Regular,
        Greater,
        Critical
    }

    private HEALTYPE healtype;


    public HealPotion(boolean isVisible) {
        super(HEALTYPE.Regular.toString() + " Potion of Healing", isVisible);
        this.healtype = HEALTYPE.Regular;
    }

    public HealPotion(HEALTYPE healtype, boolean isVisible) {
        super(healtype.toString() + " Potion of Healing", isVisible);
        this.healtype = healtype;
    }

    int use() {
        switch (this.healtype) {
            case Regular:
                return Dice.roll(1, 4);
            case Greater:
                return Dice.roll(1, 6);
            case Critical:
                return Dice.roll(1, 8);
        }
        return 0;
    }

    @Override
    public String performUsage() {
        return "You drop an empty bottle to the ground.";
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("This is a bottle of ");
        sb.append(this.getName());
        return sb.toString();
    }
}
