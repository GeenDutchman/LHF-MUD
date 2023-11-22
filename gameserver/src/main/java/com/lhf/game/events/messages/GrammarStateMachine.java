package com.lhf.game.events.messages;

public interface GrammarStateMachine {
    Boolean parse(String token);

    Boolean isValid();

    String getResult();
}
