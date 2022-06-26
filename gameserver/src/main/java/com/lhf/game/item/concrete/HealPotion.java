package com.lhf.game.item.concrete;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.*;
import com.lhf.game.enums.HealType;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Usable;
import com.lhf.game.item.interfaces.UseAction;

public class HealPotion extends Usable {

    private HealType healtype;

    private void setUp() {
        UseAction useAction = (object) -> {
            if (object == null) {
                return "That is not a valid target at all!";
            } else if (object instanceof Creature) {
                Integer healed = this.use();
                ((Creature) object).updateHitpoints(healed);
                return "You drank a " + this.getName() + ".  You now have "
                        + ((Creature) object).getStats().get(Stats.CURRENTHP) + " health points.";
            }
            return "You cannot use a " + this.getName() + " on that.";
        };
        this.setUseAction(Creature.class.getName(), useAction);
    }

    public HealPotion(boolean isVisible) {
        super(HealType.Regular.toString() + " Potion of Healing", isVisible);
        this.healtype = HealType.Regular;
        setUp();
    }

    public HealPotion(HealType type) {
        super(type.toString() + " Potion of Healing", true);
        this.healtype = type;
        setUp();
    }

    public HealPotion(HealType healtype, boolean isVisible) {
        super(healtype.toString() + " Potion of Healing", isVisible);
        this.healtype = healtype;
        setUp();
    }

    public Integer use() {
        DiceRoller die = DiceRoller.getInstance();
        switch (this.healtype) {
            case Regular:
                return die.d4(1) + 1;
            case Greater:
                return die.d6(1) + 1;
            case Critical:
                return die.d8(1) + 1;
        }
        return 0;
    }

    @Override
    public String printDescription() {
        return "This is a bottle of " + this.getName();
    }
}
