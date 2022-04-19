package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class CreateInMessage extends Command {
    private String username;
    private String password;

    CreateInMessage(String payload) {
        super(CommandMessage.CREATE, payload, true);
        this.addPreposition("with");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
