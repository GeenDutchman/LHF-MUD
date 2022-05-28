package com.lhf.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import com.lhf.server.client.Client;
import com.lhf.server.client.StringBufferSendStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerTest {

    private class ComBundle {
        public Client client;
        public StringBufferSendStrategy sssb;
        public String name;

        public ComBundle(Server server) throws IOException {
            this.client = server.clientManager.newClient(server);
            this.sssb = new StringBufferSendStrategy();
            this.client.SetOut(this.sssb);
            server.startClient(this.client);
        }

        public String create(String name) {
            this.name = name;
            return this.handleCommand("create " + name + " with " + name);
        }

        public String handleCommand(String command) {
            this.client.ProcessString(command);
            String response = this.read();
            assertFalse(response.toLowerCase().contains("was not handled"));
            assertFalse(response.toLowerCase().contains("was not recognized"));
            return response;
        }

        public String read() {
            String buffer = this.sssb.read();
            String tempname = "No name";
            if (this.name != null) {
                tempname = this.name;
            }
            System.out.println("***********************" + tempname + "**********************");
            System.out.println(buffer);
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            return buffer;
        }

        public void clear() {
            this.sssb.clear();
        }
    }

    protected Server server;
    protected ComBundle comm;

    @BeforeEach
    public void initEach() {
        try {
            this.server = new Server();
            this.comm = new ComBundle(this.server);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testServerInitialMessage() {
        String message = this.comm.read();
        System.out.println(message);
        assertTrue(message.contains("Welcome"));
    }

    @Test
    void testCharacterCreation() {
        String message = this.comm.read();
        assertTrue(message.toLowerCase().contains("create"));
        message = this.comm.create("Tester");
        assertTrue(message.toLowerCase().contains("room"));
        assertTrue(message.contains(this.comm.name));

    }

    @Test
    void testLook() {
        this.comm.create("Tester");
        String room = this.comm.handleCommand("see");
        assertTrue(room.contains("room"));
    }

    @Test
    void testGo() {
        this.comm.clear();
        String room1 = this.comm.create("Tester");
        assertTrue(room1.contains("east"));
        String room2 = this.comm.handleCommand("go east");
        System.out.println(room2);
        assertTrue(room2.contains("room"));
        assertNotEquals(room1, room2);

    }

    @Test
    void testSpeaking() throws IOException {
        this.comm.create("Tester");
        ComBundle listener1 = new ComBundle(this.server);
        listener1.create("Listener1");
        ComBundle listener2 = new ComBundle(this.server);
        listener2.create("Listener2");
        String room = this.comm.handleCommand("see");
        assertTrue(room.contains(this.comm.name));
        assertTrue(room.contains(listener1.name));
        assertTrue(room.contains(listener2.name));
        this.comm.handleCommand("say this is a unique string");
        String heard1 = listener1.read();
        assertTrue(heard1.contains(this.comm.name));
        assertTrue(heard1.contains("this is a unique string"));
        String heard2 = listener2.read();
        assertTrue(heard2.contains(this.comm.name));
        assertTrue(heard2.contains("this is a unique string"));

        this.comm.handleCommand("say hey you man to Listener1");
        heard1 = listener1.read();
        assertTrue(heard1.contains(this.comm.name));
        assertTrue(heard1.contains("hey you man"));
        heard2 = listener2.read();
        assertFalse(heard2.contains(this.comm.name));
        assertFalse(heard2.contains("hey you man"));

        this.comm.handleCommand("shout hello world");
        heard1 = listener1.read();
        assertTrue(heard1.contains(this.comm.name));
        assertTrue(heard1.contains("hello world"));
        heard2 = listener2.read();
        assertTrue(heard2.contains(this.comm.name));
        assertTrue(heard2.contains("hello world"));

        // Test from different room
        listener2.handleCommand("go east");

        this.comm.handleCommand("say zaboomafoo");
        heard1 = listener1.read();
        assertTrue(heard1.contains(this.comm.name));
        assertTrue(heard1.contains("zaboomafoo"));
        heard2 = listener2.read();
        assertFalse(heard2.contains(this.comm.name));
        assertFalse(heard2.contains("zaboomafoo"));

        this.comm.handleCommand("say lil dip sauce to Listener1");
        heard1 = listener1.read();
        assertTrue(heard1.contains(this.comm.name));
        assertTrue(heard1.contains("lil dip sauce"));
        heard2 = listener2.read();
        assertFalse(heard2.contains(this.comm.name));
        assertFalse(heard2.contains("lil dip sauce"));

        this.comm.handleCommand("shout I like yelling");
        heard1 = listener1.read();
        assertTrue(heard1.contains(this.comm.name));
        assertTrue(heard1.contains("I like yelling"));
        heard2 = listener2.read();
        assertTrue(heard2.contains(this.comm.name));
        assertTrue(heard2.contains("I like yelling"));

    }

    @Test
    void testAttack() {
        this.comm.create("Tester");
        String extract = this.comm.handleCommand("go east");
        int creature_index = extract.indexOf("<creature>");
        int endcreature_index = extract.indexOf("</creature>");
        extract = extract.substring(creature_index + "<creature>".length(), endcreature_index);
        System.out.println(extract);
        String room = this.comm.handleCommand("see");
        int i = 0;
        while (room.contains(extract) && i < 15) {
            this.comm.handleCommand("attack " + extract);

            room = this.comm.handleCommand("see");
            i += 1;
        }
        assertTrue(i < 15);
        assertFalse(room.contains(extract));
    }
}
