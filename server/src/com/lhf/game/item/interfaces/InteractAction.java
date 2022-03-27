package com.lhf.game.item.interfaces;

import com.lhf.game.creature.Player;

import java.util.Map;

public interface InteractAction {
    String doAction(Player player, Map<String, Object> args);
}
