package com.lhf.game.map.objects.roomobject.interfaces;

import com.lhf.game.map.Player;

import java.util.Map;

public interface InteractAction {
    String doAction(Player player, Map<String, Object> args);
}
