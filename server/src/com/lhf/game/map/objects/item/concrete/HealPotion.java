package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.Dice;
import com.lhf.game.map.objects.item.StackableItem;
import com.lhf.game.map.objects.item.interfaces.IUsable;
import com.lhf.game.map.objects.sharedinterfaces.Examinable;

public class HealPotion extends StackableItem implements Examinable, IUsable {

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

    int use() throws StackableItemException {
        if (this.getCount() > 0) {
            super.take();
            switch (this.healtype) {
                case Regular:
                    return Dice.roll(1, 4);
                case Greater:
                    return Dice.roll(1, 6);
                case Critical:
                    return Dice.roll(1, 8);
            }
        } else {
            throw new StackableItemException("You don't have any to use!");
        }
        return 0;
    }

    @Override
    public String performUsage() {
        return "You drop an empty bottle to the ground.";
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("This is a stack of ");
        sb.append(this.getCount()).append(" bottle");
        if (this.getCount() > 1) {
            sb.append('s');
        }
        sb.append(" of ").append(this.getName());
        return sb.toString();
    }
}
