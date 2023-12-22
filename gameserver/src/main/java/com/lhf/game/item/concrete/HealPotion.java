package com.lhf.game.item.concrete;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealType;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Usable;
import com.lhf.game.item.interfaces.UseAction;
import com.lhf.messages.out.BattleRoundEvent;
import com.lhf.messages.out.GameEvent;
import com.lhf.messages.out.ItemUsedEvent;
import com.lhf.messages.out.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.out.ItemUsedEvent.UseOutMessageOption;

public class HealPotion extends Usable {

    private HealType healtype;

    private void setUp() {
        UseAction useAction = (ctx, object) -> {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this);
            if (object == null) {
                ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                        .setMessage("That is not a valid target at all!").Build());
                return true;
            } else if (object instanceof ICreature) {
                ICreature target = (ICreature) object;
                useOutMessage.setTarget(target);
                CreatureEffectSource bce = new CreatureEffectSource(this.healtype.toString() + " healing",
                        new EffectPersistence(TickType.INSTANT), null, "Heals you a little bit", false);
                bce = this.setHealing(bce);
                if (ctx.getBattleManager() != null) {
                    BattleManager bm = ctx.getBattleManager();
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
                } else if (ctx.getRoom() != null) {
                    ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).Build());
                    GameEvent results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
                    ctx.getRoom().announce(results);
                } else {
                    ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).Build());
                    GameEvent results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
                    ctx.receive(results);
                    if (ctx.getCreature() != target) {
                        ICreature.eventAccepter.accept(target, results);
                    }
                }
                return true;
            }
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use a " + this.getName() + " on that."));
            return true;
        };
        this.setUseAction(ICreature.class.getName(), useAction);
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
    public String printDescription() {
        return "This is a bottle of " + this.getName();
    }
}
