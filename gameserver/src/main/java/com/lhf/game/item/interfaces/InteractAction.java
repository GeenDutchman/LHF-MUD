package com.lhf.game.item.interfaces;

import com.lhf.game.creature.ICreature;
import com.lhf.game.item.InteractObject;
import com.lhf.messages.events.GameEvent;

import java.util.Map;

public interface InteractAction {
    GameEvent doAction(ICreature creature, InteractObject triggerObject, Map<String, Object> args); // TODO: this
                                                                                                    // shouldn't be
                                                                                                    // Object perhaps?
}
