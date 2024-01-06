package com.lhf.messages.grammar;

import com.lhf.messages.GrammarStateMachine;
import com.lhf.messages.in.AMessageType;

public class CommandWord implements GrammarStateMachine {
    protected AMessageType theCommand = null;
    protected Boolean dupedEntry = false;

    public AMessageType getCommand() {
        return this.theCommand;
    }

    @Override
    public Boolean parse(String token) {
        if (this.theCommand != null) {
            this.dupedEntry = true;
            return false;
        }
        this.theCommand = AMessageType.getCommandMessage(token);
        return this.theCommand != null;
    }

    @Override
    public Boolean isValid() {
        return (this.theCommand != null) && !this.dupedEntry;
    }

    @Override
    public String getResult() {
        if (this.theCommand == null) {
            return "";
        }
        return this.theCommand.toString();
    }

}
