package com.lhf.messages.grammar;

import com.lhf.messages.CommandMessage;
import com.lhf.messages.GrammarStateMachine;

public class CommandWord implements GrammarStateMachine {
    protected CommandMessage theCommand = null;
    protected Boolean dupedEntry = false;

    public CommandMessage getCommand() {
        return this.theCommand;
    }

    @Override
    public Boolean parse(String token) {
        if (this.theCommand != null) {
            this.dupedEntry = true;
            return false;
        }
        this.theCommand = CommandMessage.valueOf(token);
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
