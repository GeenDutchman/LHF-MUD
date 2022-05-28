package com.lhf.server;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import com.lhf.server.client.Client;
import com.lhf.server.client.StringBufferSendStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerTest {
    protected Server server;
    protected Client client;
    protected StringBufferSendStrategy sssb;

    @BeforeEach
    public void initEach() {
        try {
            this.server = new Server();
            this.client = new Client();
            this.sssb = new StringBufferSendStrategy();
            this.client.SetOut(this.sssb);
            this.server.startClient(this.client);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testServerInitialMessage() {
        String message = this.sssb.read();
        System.out.println(message);
        assertTrue(message.contains("Welcome"));

    }
}
