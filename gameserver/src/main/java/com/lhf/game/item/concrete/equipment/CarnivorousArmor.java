package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.EquipableHiddenEffect;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;

public class CarnivorousArmor extends EquipableHiddenEffect {
    private static final CreatureEffectSource eatingACResults = new CreatureEffectSource.Builder("Protect the Meal")
            .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
            .setDescription("Must protect the next meal...you!")
            .setOnApplication(new Deltas().setStatChange(Stats.AC, 3))
            .build();

    private static final CreatureEffectSource lastBite = new CreatureEffectSource.Builder("Last Bite")
            .instantPersistence()
            .setDescription("As you tear it off, one last bite!")
            .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, CarnivorousArmor.eatsHealthTo)).build();

    private static final int AC = 2;
    private static final int eatsHealthTo = 5;
    private boolean equipped = false;
    private boolean equippedAndUsed = false;

    public CarnivorousArmor() {
        super("Carnivorous Armor", "This is some simple leather armor. There is plenty of blood on it...\n");

        this.slots = List.of(EquipmentSlots.ARMOR);
        this.types = List.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        this.equipEffects = List.of(new CreatureEffectSource.Builder("It is Armor.")
                .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
                .setDescription("You are now wearing armor.")
                .setOnApplication(new Deltas().setStatChange(Stats.AC, CarnivorousArmor.AC)).build());
    }

    @Override
    public CarnivorousArmor makeCopy() {
        return new CarnivorousArmor();
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
    public void onEquippedBy(ICreature newOwner) {
        this.equipped = true;
        this.equippedAndUsed = false;

        super.onEquippedBy(newOwner);
    }

    @Override
    public boolean useOn(CommandContext ctx, ICreature creature) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature()).setUsable(this);

        if (!equipped) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).Build());
            return true;
        }
        if (creature == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return true;
        } else if (!creature.equals(ctx.getCreature())) {
            String notUse = "You cannot use this on that!  You can only use it on yourself.";
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).setMessage(notUse).Build());
            return true;
        }

        useOutMessage.setTarget(creature);
        if (equippedAndUsed) {
            String snuggle = "The " + this.getColorTaggedName()
                    + " snuggles around you as you poke at it, but otherwise does nothing.";
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).setMessage(snuggle).Build());
            return true;
        }
        Integer currHealth = creature.getStats().getOrDefault(Stats.CURRENTHP, 0);
        if (currHealth > eatsHealthTo) {
            int diff = currHealth - eatsHealthTo;
            final CreatureEffectSource eatingResults = new CreatureEffectSource.Builder("Eaten Alive")
                    .instantPersistence()
                    .setDescription("You are eaten alive...just a bite.")
                    .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, diff * -1)).build();
            String eatDescription = "A thousand teeth sink into your body, and you feel life force ripped out of you.  "
                    +
                    "Once it is sated, you feel the " + this.getColorTaggedName() +
                    " tighten up around its most recent, precious meal.  It leaves the rest for later.";
            equippedAndUsed = true;
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.OK).setMessage(eatDescription).Build());
            ctx.receive(
                    creature.applyEffect(new CreatureEffect(eatingResults, ctx.getCreature(), this)));
            ctx.receive(creature
                    .applyEffect(new CreatureEffect(CarnivorousArmor.eatingACResults, ctx.getCreature(), this)));
            return true;
        } else {
            String moreNeeded = "You need more health to use this item.";
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).setMessage(moreNeeded).Build());
            return true;
        }
    }

    @Override
    public void onUnequippedBy(ICreature disowner) {
        super.onUnequippedBy(disowner);
        if (equippedAndUsed) {
            ICreature.eventAccepter.accept(disowner,
                    disowner.applyEffect(new CreatureEffect(CarnivorousArmor.lastBite, disowner, this)));
            ICreature.eventAccepter.accept(disowner, disowner.repealEffect(eatingACResults.getName()));
        }
        equipped = false;
        equippedAndUsed = false;
    }

}
