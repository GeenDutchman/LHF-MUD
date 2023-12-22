package com.lhf.server.client;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.lhf.messages.events.GameEvent;

public class PrintWriterSendStrategy implements SendStrategy {
    private PrintWriter writer;

    public PrintWriterSendStrategy(OutputStream outputStream) {
        this.writer = new PrintWriter(outputStream, true);
    }

    @Override
    public void send(GameEvent toSend) {
        this.writer.println(toSend.toString());
        this.writer.flush();
    }

}
