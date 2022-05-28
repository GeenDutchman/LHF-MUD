package com.lhf.server.client;

public class StringBufferSendStrategy implements SendStrategy {
    private StringBuffer sBuffer;

    public StringBufferSendStrategy() {
        this.sBuffer = new StringBuffer();
    }

    @Override
    public void send(String toSend) {
        this.sBuffer.append(toSend);
    }

    public String read() {
        String toReturn = this.sBuffer.toString();
        this.sBuffer.setLength(0);
        return toReturn;
    }

}
