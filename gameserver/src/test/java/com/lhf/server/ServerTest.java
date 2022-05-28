package com.lhf.server;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
            this.client = this.server.clientManager.newClient(this.server);
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

    @Test
    void testCharacterCreation() {
        String message = this.sssb.read();
        assertTrue(message.toLowerCase().contains("create"));
        this.client.ProcessString("create Tester with mana");
        message = this.sssb.read();
        System.out.println(message);
        assertTrue(message.toLowerCase().contains("room"));
        assertTrue(message.contains("Tester"));

    }

    @Test
    void testGo() {
        this.sssb.clear();
        this.client.ProcessString("create Tester with mana");
        String room1 = this.sssb.read();
        assertTrue(room1.contains("east"));
        this.client.ProcessString("go east");
        String room2 = this.sssb.read();
        System.out.println(room2);
        assertTrue(room2.contains("room"));
        assertNotEquals(room1, room2);

    }
}
