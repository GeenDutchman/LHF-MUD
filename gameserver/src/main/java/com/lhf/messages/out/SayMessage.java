package com.lhf.messages.out;

import com.lhf.messages.ClientMessenger;

public class SayMessage extends OutMessage {
    private String message;
    private ClientMessenger sayer;
    private ClientMessenger hearer;

    public SayMessage(ClientMessenger sayer, String message) {
        this.sayer = sayer;
        this.message = message;
    }

    public SayMessage(ClientMessenger sayer, String message, ClientMessenger hearer) {
        this.sayer = sayer;
        this.message = message;
        this.hearer = hearer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.sayer.getColorTaggedName());
        if (this.hearer != null) {
            sb.append(" to ").append(this.hearer.getColorTaggedName());
        }
        sb.append(":").append(this.message);
        return sb.toString();
    }

    public String getMessage() {
        return message;
    }

    public ClientMessenger getSayer() {
        return sayer;
    }

    public ClientMessenger getHearer() {
        return hearer;
    }
}
