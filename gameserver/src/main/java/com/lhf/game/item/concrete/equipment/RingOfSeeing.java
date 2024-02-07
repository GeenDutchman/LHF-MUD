package com.lhf.game.item.concrete.equipment;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;

public class RingOfSeeing extends Equipable {
    private final static String descriptor = "This ring can help you see things that are not visible to the naked eye. "
            + "It can only be used so many times though, and then the ring itself disappears... \n";

    public RingOfSeeing() {
        super("Ring of Seeing", descriptor, 3);

        this.slots.add(EquipmentSlots.LEFTHAND);
        this.slots.add(EquipmentSlots.RIGHTHAND);
        this.equipEffects.add(new CreatureEffectSource("Seeing wisdom", new EffectPersistence(TickType.CONDITIONAL),
                null, "If you can see, then you are wise.", new Deltas().setAttributeBonusChange(Attributes.WIS, 2)));
    }

    @Override
    public RingOfSeeing makeCopy() {
        return new RingOfSeeing();
    }

    @Override
    public boolean useOn(CommandContext ctx, Area area) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                .setUsable(this);
        if (area == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("That is not a valid target at all!").Build());
            return true;
        }
        ctx.receive(area.produceMessage(true, true));

        return true;
    }

}
