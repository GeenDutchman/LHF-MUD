package com.lhf.game.events;

public interface GameEvent {
    public interface GameEventType {
        // Used by enums
    }

    public GameEventType getGameEventType();
}
