package com.lhf.messages;

public interface GrammarStateMachine {
    Boolean parse(String token);

    Boolean isValid();

    String getResult();
}
