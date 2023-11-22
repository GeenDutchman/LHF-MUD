package com.lhf.game.events;

public interface GameEvent {
    public interface GameEventType extends Comparable<GameEventType> {
        // Used by enums
        @Override
        default int compareTo(GameEventType arg0) {
            if (arg0 == null)
                return 1;
            int classCompare = this.getClass().getName().compareTo(arg0.getClass().getName());
            if (classCompare != 0) {
                return classCompare;
            }
            return this.toString().compareTo(arg0.toString());
        }

    }

    public GameEventType getGameEventType();
}
