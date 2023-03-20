package com.lhf.game.item.concrete.equipment;

import static org.mockito.ArgumentMatchers.booleanThat;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
import com.lhf.game.map.Room;
import com.lhf.messages.out.UseOutMessage;
import com.lhf.messages.out.UseOutMessage.UseOutMessageOption;

public class RingOfSeeing extends Equipable {

    public RingOfSeeing(boolean isVisible) {
        super("Ring of Seeing", isVisible, 3);
        this.setUseAction(Room.class.getName(), (ctx, object) -> {
            UseOutMessage.Builder useOutMessage = UseOutMessage.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this);
            if (object == null) {
                ctx.sendMsg(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                        .setMessage("That is not a valid target at all!").Build());
                return true;
            } else if (object instanceof Room) {
                Room seenRoom = (Room) object;
                ctx.sendMsg(seenRoom.produceMessage(true, true));
                return true;
            }
            ctx.sendMsg(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use a " + this.getName() + " on that.").Build());
            return true;
        });

        this.slots.add(EquipmentSlots.LEFTHAND);
        this.slots.add(EquipmentSlots.RIGHTHAND);
        this.equipEffects.add(new CreatureEffectSource("Seeing wisdom", new EffectPersistence(TickType.CONDITIONAL),
                null, "If you can see, then you are wise.", false)
                .addAttributeBonusChange(Attributes.WIS, 2));
        this.descriptionString = "This ring can help you see things that are not visible to the naked eye. ";
        this.descriptionString += "It can only be used so many times though, and then the ring itself disappears... \n";
    }

}
