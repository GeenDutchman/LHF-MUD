package com.lhf.game.creature.intelligence.handlers;

import java.util.function.BiPredicate;
import java.util.logging.Level;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.server.interfaces.NotNull;

public class SilencedHandler extends AIHandler {
    protected final AIHandler passThrough;
    protected final BiPredicate<BasicAI, GameEvent> allowPassthrough;

    public SilencedHandler(@NotNull GameEventType outMessageType) {
        super(outMessageType);
        this.passThrough = null;
        this.allowPassthrough = null;
    }

    public SilencedHandler(GameEventType outMessageType, AIHandler passThrough,
            BiPredicate<BasicAI, GameEvent> allowPassthrough) {
        super(outMessageType);
        this.passThrough = passThrough;
        this.allowPassthrough = allowPassthrough;
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (this.passThrough != null && this.allowPassthrough != null && this.allowPassthrough.test(bai, event)) {
            this.logger.log(Level.FINE,
                    () -> String.format("Passing through to %s", this.passThrough.getClass().getSimpleName()));
            this.passThrough.handle(bai, event);
        }
    }

}
