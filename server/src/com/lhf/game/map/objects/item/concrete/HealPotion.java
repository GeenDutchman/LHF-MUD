package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.objects.item.interfaces.Consumable;
import com.lhf.game.map.objects.item.interfaces.Takeable;
import com.lhf.game.map.objects.item.interfaces.Usable;
import com.lhf.game.map.objects.item.interfaces.UseAction;
import com.lhf.game.shared.dice.Dice;
import com.lhf.game.shared.enums.Stats;

public class HealPotion extends Usable implements Consumable, Takeable {

    public enum HEALTYPE {
        Regular,
        Greater,
        Critical
    }

    private HEALTYPE healtype;

    private void setUp() {
        UseAction useAction = (object) -> {
            if (object == null) {
                return "That is not a valid target at all!";
            } else if (object instanceof Creature) {
                Integer healed = this.use();
                ((Creature) object).updateHitpoints(healed);
                return "You drank a " + this.getName() + ".  You now have " + ((Creature) object).getStats().get(Stats.CURRENTHP) + " health points.";
            }
            return "You cannot use a " + this.getName() + " on that.";
        };
        this.setUseAction(Creature.class.getName(), useAction);
    }


    public HealPotion(boolean isVisible) {
        super(HEALTYPE.Regular.toString() + " Potion of Healing", isVisible);
        this.healtype = HEALTYPE.Regular;
        setUp();
    }

    public HealPotion(HEALTYPE healtype, boolean isVisible) {
        super(healtype.toString() + " Potion of Healing", isVisible);
        this.healtype = healtype;
        setUp();
    }

    public Integer use() {
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
    public String getDescription() {
        StringBuilder sb = new StringBuilder("This is a bottle of ");
        sb.append(this.getName());
        return sb.toString();
    }
}
