package com.lhf.game.item;

import java.util.Map;
import java.util.logging.Logger;

import com.lhf.game.creature.ICreature;
import com.lhf.game.item.concrete.Dispenser;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.game.map.Room;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.GameEvent;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;

public class DispenserAction implements InteractAction {
    @Override
    public GameEvent doAction(ICreature creature, InteractObject triggerObject, Map<String, Object> args) {
        InteractOutMessage.Builder interactOutMessage = InteractOutMessage.getBuilder().setTaggable(triggerObject);
        Object o1 = args.get("room");
        if (!(o1 instanceof Room)) {
            Logger.getLogger(triggerObject.getClassName()).warning("Room not found");
            return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
        }
        Room r = (Room) o1;
        Object o2 = args.get("disp");
        if (!(o2 instanceof Dispenser)) {
            Logger.getLogger(triggerObject.getClassName()).warning("Dispenser not found");
            return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
        }
        Dispenser d = (Dispenser) o2;
        Object o3 = args.get("item");
        if (!(o3 instanceof Item)) {
            Logger.getLogger(triggerObject.getClassName()).warning("Dispensed item not found");
            return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
        }
        Item i = (Item) o3;
        Object o4 = args.get("message");
        if (!(o4 instanceof String)) {
            Logger.getLogger(triggerObject.getClassName()).warning("No message to print");
            return interactOutMessage.setSubType(InteractOutMessageType.ERROR).Build();
        }
        String s = (String) o4;
        r.addItem(i);
        d.incrementCount();
        return interactOutMessage.setDescription(s).setPerformed().Build();
    }
}
