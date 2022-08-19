package com.lhf.game.item.concrete;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.battle.BattleManager;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.BasicCreatureEffect;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.HealType;
import com.lhf.game.item.Usable;
import com.lhf.game.item.interfaces.UseAction;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.UseOutMessage;
import com.lhf.messages.out.UseOutMessage.UseOutMessageOption;

public class HealPotion extends Usable {

    private HealType healtype;

    private void setUp() {
        UseAction useAction = (ctx, object) -> {
            if (object == null) {
                ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES,
                        ctx.getCreature(), this, null, "That is not a valid target at all!"));
                return true;
            } else if (object instanceof Creature) {
                Creature target = (Creature) object;
                CreatureEffectSource bce = new CreatureEffectSource(this.healtype.toString() + " healing",
                        new EffectPersistence(TickType.INSTANT), "Heals you a little bit", false);
                bce = this.setHealing(bce);
                if (ctx.getBattleManager() != null) {
                    BattleManager bm = ctx.getBattleManager();
                    if (bm.isCreatureInBattle(target) && !bm.isCreatureInBattle(ctx.getCreature())) {
                        // give out of turn message
                        ctx.sendMsg(new BattleTurnMessage(ctx.getCreature(), false, true));
                        bm.addCreatureToBattle(ctx.getCreature());
                        return false;
                    }
                    ctx.sendMsg(new UseOutMessage(UseOutMessageOption.OK, ctx.getCreature(), this, target));
                    OutMessage results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
                    bm.sendMessageToAllParticipants(results);
                } else if (ctx.getRoom() != null) {
                    ctx.sendMsg(new UseOutMessage(UseOutMessageOption.OK, ctx.getCreature(), this, target));
                    OutMessage results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
                    ctx.getRoom().sendMessageToAll(results);
                } else {
                    ctx.sendMsg(new UseOutMessage(UseOutMessageOption.OK, ctx.getCreature(), this, target));
                    OutMessage results = target.applyEffect(new CreatureEffect(bce, ctx.getCreature(), this));
                    ctx.sendMsg(results);
                    target.sendMsg(results);
                }
                return true;
            }
            ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES, ctx.getCreature(), this, null,
                    "You cannot use a " + this.getName() + " on that."));
            return true;
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

    private CreatureEffectSource setHealing(CreatureEffectSource effect) {
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

        effect.addDamageBonus(-1);
        return effect;
    }

    @Override
    public String printDescription() {
        return "This is a bottle of " + this.getName();
    }
}
