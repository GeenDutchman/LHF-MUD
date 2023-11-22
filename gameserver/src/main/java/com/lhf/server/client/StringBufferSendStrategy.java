package com.lhf.server.client;

import com.lhf.game.events.messages.out.OutMessage;

public class StringBufferSendStrategy implements SendStrategy {
    private StringBuffer sBuffer;

    public StringBufferSendStrategy() {
        this.sBuffer = new StringBuffer();
    }

    @Override
    public void send(OutMessage toSend) {
        this.sBuffer.append(toSend.toString()).append("\n");
    }

    public void clear() {
        this.sBuffer.setLength(0);
    }

    public String read() {
        String toReturn = this.sBuffer.toString();
        this.sBuffer.setLength(0);
        return toReturn;
    }

}
