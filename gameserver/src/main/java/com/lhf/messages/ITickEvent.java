package com.lhf.messages;

import java.util.Optional;

import com.lhf.game.TickType;

public interface ITickEvent {
    public TickType getTickType();

    public default Optional<String> getTickSpecificity() {
        return Optional.empty();
    }
}
