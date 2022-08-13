package com.lhf.game.item.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.lhf.game.EntityEffector;
import com.lhf.messages.CommandContext;
import com.lhf.messages.OutMessagePair;

public interface UseAction {
    public class UseageResult {
        public OutMessagePair messages;
        public List<EntityEffector> effectors;

        public UseageResult(OutMessagePair messages, EntityEffector effector) {
            this.messages = messages;
            this.effectors = new ArrayList<>();
            if (effector != null) {
                this.effectors.add(effector);
            }
        }

        public UseageResult addEffect(EntityEffector effector) {
            this.effectors.add(effector);
            return this;
        }

    }

    UseageResult useAction(CommandContext ctx, Object useOn);
}
