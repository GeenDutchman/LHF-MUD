package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.Consumable;
import com.lhf.game.shared.dice.Dice;

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
        Dice die = Dice.getInstance();
        switch (this.healtype) {
            case Regular:
                return die.d4(1);
            case Greater:
                return die.d6(1);
            case Critical:
                return die.d8(1);
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
