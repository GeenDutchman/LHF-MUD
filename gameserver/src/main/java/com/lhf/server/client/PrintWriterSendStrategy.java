package com.lhf.server.client;

import java.io.OutputStream;
import java.io.PrintWriter;

public class PrintWriterSendStrategy implements SendStrategy {
    private PrintWriter writer;

    public PrintWriterSendStrategy(OutputStream outputStream) {
        this.writer = new PrintWriter(outputStream, true);
    }

    @Override
    public void send(String toSend) {
        this.writer.println(toSend);
        this.writer.flush();
    }

}
