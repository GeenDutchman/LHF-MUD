package com.lhf.game.item.concrete;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealType;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Usable;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.BattleRoundEvent;
import com.lhf.messages.events.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;

public class HealPotion extends Usable {

    private final HealType healtype;

    public HealPotion() {
        super(HealType.Regular.toString() + " Potion of Healing", null);
        this.healtype = HealType.Regular;
    }

    public HealPotion(HealType type) {
        super(type.toString() + " Potion of Healing", null);
        this.healtype = type;
    }

    public HealPotion(HealType healtype, CreatureVisitor visitor) {
        super(healtype.toString() + " Potion of Healing", visitor);
        this.healtype = healtype;
    }

    @Override
    public HealPotion makeCopy() {
        return this;
    }

    private CreatureEffectSource setHealing(CreatureEffectSource effect) {
        if (this.healtype == null) {
            effect.addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING));
        } else {
            switch (this.healtype) {
                case Critical:
                    effect.addDamage(new DamageDice(1, DieType.EIGHT, DamageFlavor.HEALING));
                case Greater:
                    effect.addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.HEALING));
                case Regular:
                    effect.addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING));
                default:
                    effect.addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING));

            }
        }

        effect.addStatChange(Stats.CURRENTHP, 1);
        return effect;
    }

    @Override
    public boolean useOn(CommandContext ctx, ICreature target) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature()).setUsable(this);
        if (target == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        if (!hasUsesLeft()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        useOutMessage.setTarget(target);
        CreatureEffectSource bce = new CreatureEffectSource(this.healtype.toString() + " healing",
                new EffectPersistence(TickType.INSTANT), null, "Heals you a little bit", false);
        bce = this.setHealing(bce);
        if (ctx.getSubAreaForSort(SubAreaSort.BATTLE) != null) {
            SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            if (bm.hasCreature(target) && !bm.hasCreature(ctx.getCreature())) {
                // give out of turn message
                bm.addCreature(ctx.getCreature());
                ctx.receive(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.REJECTED)
                        .setNotBroadcast().Build());
                return false;
            }
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).Build());
            GameEvent results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
            bm.announce(results);
        } else if (ctx.getArea() != null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).Build());
            GameEvent results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
            ctx.getArea().announce(results);
        } else {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).Build());
            GameEvent results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
            ctx.receive(results);
            if (ctx.getCreature() != target) {
                ICreature.eventAccepter.accept(target, results);
            }
        }
        if (this.itemVisitor != null) {
            target.acceptCreatureVisitor(creatureVisitor);
        }
        return true;

    }

    @Override
    public String printDescription() {
        return "This is a bottle of " + this.getName();
    }
}
