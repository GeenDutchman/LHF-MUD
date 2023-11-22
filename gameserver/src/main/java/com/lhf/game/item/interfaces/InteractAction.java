package com.lhf.game.item.interfaces;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.InteractObject;
import com.lhf.messages.out.OutMessage;

import java.util.Map;

public interface InteractAction {
    OutMessage doAction(Creature creature, InteractObject triggerObject, Map<String, Object> args); // TODO: this
                                                                                                    // shouldn't be
                                                                                                    // Object perhaps?
}
