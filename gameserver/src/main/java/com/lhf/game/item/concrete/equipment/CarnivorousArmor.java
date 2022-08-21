package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.messages.out.UseOutMessage;
import com.lhf.messages.out.UseOutMessage.UseOutMessageOption;

public class CarnivorousArmor extends Equipable {
    private final CreatureEffectSource eatingResults = new CreatureEffectSource("Eaten Alive",
            new EffectPersistence(TickType.INSTANT), "You are eaten alive...just a bite.", false);

    private final CreatureEffectSource eatingACResults = new CreatureEffectSource("Protect the Meal",
            new EffectPersistence(TickType.CONDITIONAL), "Must protect the next meal...you!", false)
            .addStatChange(Stats.AC, 3);

    private final CreatureEffectSource lastBite = new CreatureEffectSource("Last Bite",
            new EffectPersistence(TickType.INSTANT), "As you tear it off, one last bite!", false)
            .addStatChange(Stats.AC, -2);

    private final int AC = 2;
    private final int eatsHealthTo = 5;
    private boolean equipped = false;
    private boolean equippedAndUsed = false;

    public CarnivorousArmor(boolean isVisible) {
        super("Carnivorous Armor", isVisible, -1);

        this.slots = List.of(EquipmentSlots.ARMOR);
        this.types = List.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        this.equipEffects = List.of(new CreatureEffectSource("It is Armor.",
                new EffectPersistence(TickType.CONDITIONAL), "You are now wearing armor.", false)
                .addStatChange(Stats.AC, this.AC));
        this.descriptionString = "This is some simple leather armor. " + "There is plenty of blood on it...\n";
    }

    @Override
    public String getName() {
        if (this.equippedAndUsed) {
            return "Carnivorous Armor";
        }
        // deceptive name
        return "Leather Armor";
    }

    @Override
    public void onEquippedBy(Creature newOwner) {
        this.equipped = true;
        this.equippedAndUsed = false;

        setUseAction(newOwner.getName(), (ctx, object) -> {
            if (!equipped) {
                ctx.sendMsg(new UseOutMessage(UseOutMessageOption.REQUIRE_EQUIPPED, ctx.getCreature(), this, null));
                return true;
            }
            if (object == null) {
                ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES, ctx.getCreature(), this, null));
                return true;
            } else if (object instanceof Creature) {
                Creature target = (Creature) object;
                if (equippedAndUsed) {
                    String snuggle = "The " + this.getColorTaggedName()
                            + " snuggles around you as you poke at it, but otherwise does nothing.";
                    ctx.sendMsg(new UseOutMessage(UseOutMessageOption.OK, ctx.getCreature(), this, target, snuggle));
                    return true;
                }
                Integer currHealth = target.getStats().get(Stats.CURRENTHP);
                if (currHealth > eatsHealthTo) {
                    int diff = currHealth - eatsHealthTo;
                    this.eatingResults.addStatChange(Stats.CURRENTHP, -1 * diff);
                    String eatDescription = "A thousand teeth sink into your body, and you feel life force ripped out of you.  "
                            +
                            "Once it is sated, you feel the " + this.getColorTaggedName() +
                            " tighten up around its most recent, precious meal.  It leaves the rest for later.";
                    equippedAndUsed = true;
                    ctx.sendMsg(
                            new UseOutMessage(UseOutMessageOption.OK, ctx.getCreature(), this, target, eatDescription));
                    ctx.sendMsg(target.applyEffect(new CreatureEffect(this.eatingResults, ctx.getCreature(), this)));
                    ctx.sendMsg(target.applyEffect(new CreatureEffect(this.eatingACResults, ctx.getCreature(), this)));
                    return true;
                } else {
                    String moreNeeded = "You need more health to use this item.";
                    ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES, ctx.getCreature(), this, target,
                            moreNeeded));
                    return true;
                }
            }
            String notUse = "You cannot use this on that!  You can only use it on yourself.";
            ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES, ctx.getCreature(), this, null, notUse));
            return true;
        });

        super.onEquippedBy(newOwner);
    }

    @Override
    public void onUnequippedBy(Creature disowner) {
        super.onUnequippedBy(disowner);
        if (equippedAndUsed) {
            disowner.sendMsg(disowner.applyEffect(new CreatureEffect(this.lastBite, disowner, this)));
        }
        equipped = false;
        equippedAndUsed = false;
        removeUseAction(disowner.getName());
    }

}
