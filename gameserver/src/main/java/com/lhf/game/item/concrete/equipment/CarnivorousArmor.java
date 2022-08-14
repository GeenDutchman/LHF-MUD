package com.lhf.game.item.concrete.equipment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.item.interfaces.UseAction.UseageResult;
import com.lhf.messages.OutMessagePair;
import com.lhf.messages.out.UseOutMessage;
import com.lhf.messages.out.UseOutMessage.UseOutMessageOption;

public class CarnivorousArmor extends Equipable {
    private class EatingResults extends BasicCreatureEffector {
        private int eatsHealth;

        EatingResults(Creature causer, int eatsHealth) {
            super(causer, CarnivorousArmor.this, new EffectPersistence(TickType.INSTANT));
            this.eatsHealth = eatsHealth;
        }

        @Override
        public Map<Stats, Integer> getStatChanges() {
            return Map.of(Stats.CURRENTHP, -1 * eatsHealth);
        }

    }

    private class EatingACResults extends BasicCreatureEffector {
        EatingACResults(Creature creatureResponsible) {
            super(creatureResponsible, CarnivorousArmor.this, new EffectPersistence(TickType.INSTANT));
        }

        @Override
        public Map<Stats, Integer> getStatChanges() {
            return Map.of(Stats.AC, 3);
        }
    }

    private int AC = 2;
    private int eatsHealthTo = 5;
    private boolean equipped = false;
    private boolean equippedAndUsed = false;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private Map<String, Integer> equippingChanges;

    public CarnivorousArmor(boolean isVisible) {
        super("Carnivorous Armor", isVisible, -1);

        slots = Collections.singletonList(EquipmentSlots.ARMOR);
        types = Arrays.asList(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        equippingChanges = new HashMap<>();
        equippingChanges.put(Stats.AC.toString(), this.AC);
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
    public List<EquipmentTypes> getTypes() {
        return types;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        return slots;
    }

    @Override
    public Map<String, Integer> onEquippedBy(EquipmentOwner newOwner) {
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
                    EatingResults useResults = new EatingResults(ctx.getCreature(), diff);
                    EatingACResults eatingACResults = new EatingACResults(ctx.getCreature());
                    String eatDescription = "A thousand teeth sink into your body, and you feel life force ripped out of you.  "
                            +
                            "Once it is sated, you feel the " + this.getColorTaggedName() +
                            " tighten up around its most recent, precious meal.  It leaves the rest for later.";
                    equippedAndUsed = true;
                    ctx.sendMsg(
                            new UseOutMessage(UseOutMessageOption.OK, ctx.getCreature(), this, target, eatDescription));
                    ctx.sendMsg(target.applyAffects(useResults));
                    ctx.sendMsg(target.applyAffects(eatingACResults));
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

        return super.onEquippedBy(newOwner);
    }

    @Override
    public Map<String, Integer> onUnequippedBy(EquipmentOwner disowner) {
        Map<String, Integer> result = super.onUnequippedBy(disowner);
        if (equippedAndUsed) {
            result.put(Stats.CURRENTHP.toString(), -2);
        }
        equipped = false;
        equippedAndUsed = false;
        removeUseAction(disowner.getName());
        return result;
    }

    @Override
    public Map<String, Integer> getEquippingChanges() {
        return this.equippingChanges;
    }

    @Override
    public String printDescription() {
        return "This is some simple leather armor. " + "There is plenty of blood on it...\n" +
                this.printStats();
    }
}
