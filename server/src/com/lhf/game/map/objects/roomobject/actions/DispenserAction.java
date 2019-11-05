package com.lhf.game.map.objects.roomobject.actions;

import com.lhf.game.creature.Player;
import com.lhf.game.map.Room;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.roomobject.Dispenser;
import com.lhf.game.map.objects.roomobject.interfaces.InteractAction;

import java.util.Map;

public class DispenserAction implements InteractAction {
    @Override
    public String doAction(Player player, Map<String, Object> args) {
        Object o1 = args.get("room");
        if (!(o1 instanceof Room)) {
            return "Switch error 1.";
        }
        Room r = (Room) o1;
        Object o2 = args.get("disp");
        if (!(o2 instanceof Dispenser)) {
            return "Switch error 2.";
        }
        Dispenser d = (Dispenser) o2;
        Object o3 = args.get("item");
        if (!(o3 instanceof Item)) {
            return "Switch error 2.";
        }
        Item i = (Item) o3;
        Object o4 = args.get("message");
        if (!(o4 instanceof String)) {
            return "Switch error 2.";
        }
        String s = (String) o4;
        r.addItem(i);
        d.incrementCount();
        return s;
    }
}
