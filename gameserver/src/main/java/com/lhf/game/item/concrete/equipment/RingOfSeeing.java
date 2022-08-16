package com.lhf.game.item.concrete.equipment;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.map.Room;
import com.lhf.messages.out.UseOutMessage;
import com.lhf.messages.out.UseOutMessage.UseOutMessageOption;

public class RingOfSeeing extends Equipable {

    public RingOfSeeing(boolean isVisible) {
        super("Ring of Seeing", isVisible, 3);
        this.setUseAction(Room.class.getName(), (ctx, object) -> {
            if (object == null) {
                ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES,
                        ctx.getCreature(), this, null, "That is not a valid target at all!"));
                return true;
            } else if (object instanceof Room) {
                Room seenRoom = (Room) object;
                ctx.sendMsg(seenRoom.produceMessage(true));
                return true;
            }
            ctx.sendMsg(new UseOutMessage(UseOutMessageOption.NO_USES, ctx.getCreature(), this, null,
                    "You cannot use a " + this.getName() + " on that."));
            return true;
        });

        this.slots.add(EquipmentSlots.LEFTHAND);
        this.slots.add(EquipmentSlots.RIGHTHAND);
        this.equipEffects.add(new BasicCreatureEffector(null, this, new EffectPersistence(TickType.CONDITIONAL))
                .addAttributeBonusChange(Attributes.WIS, 2));
        this.descriptionString = "This ring can help you see things that are not visible to the naked eye. ";
        this.descriptionString += "It can only be used so many times though, and then the ring itself disappears... \n";
    }

}
