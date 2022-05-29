package com.lhf.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import com.lhf.server.client.Client;
import com.lhf.server.client.StringBufferSendStrategy;

import org.junit.jupiter.api.Assertions;
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

        public String create(String name, Boolean expectUnique) {
            String result = this.handleCommand("create " + name + " with " + name);
            Boolean alreadyExists = result.toLowerCase().contains("already exists");
            if (expectUnique) {
                assertFalse(alreadyExists);
                this.name = name;
            } else {
                assertTrue(alreadyExists);
            }
            return result;
        }

        public String create(String name) {
            return this.create(name, true);
        }

        public String handleCommand(String command, Boolean expectRecognized, Boolean expectHandled) {
            this.client.ProcessString(command);
            String response = this.read();
            Boolean notRecognized = response.toLowerCase().contains("was not recognized");
            if (expectRecognized) {
                assertFalse(notRecognized);
            } else {
                assertTrue(notRecognized);
            }
            Boolean notHandled = response.toLowerCase().contains("was not handled");
            if (expectHandled) {
                assertFalse(notHandled);
            } else {
                assertTrue(notHandled);
            }
            return response;
        }

        public String handleCommand(String command) {
            return this.handleCommand(command, true, true);
        }

        public String read() {
            String buffer = this.sssb.read();
            String tempname = String.valueOf(this.hashCode());
            if (this.name != null) {
                tempname += ' ' + this.name;
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
        assertTrue(message.contains("Welcome"));
    }

    @Test
    void testFreshExit() {
        this.comm.read();
        String message = this.comm.handleCommand("exit");
        assertTrue(message.toLowerCase().contains("goodbye"));
    }

    @Test
    void testExitFinality() {

        this.comm.read();
        String message = this.comm.handleCommand("exit");
        assertTrue(message.toLowerCase().contains("goodbye"));
        this.comm.handleCommand("see", true, false);
        Assertions.assertThrows(NullPointerException.class, () -> {
            this.comm.handleCommand("create Tester with Tester");
        });
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
    void testCreatedExit() {
        this.comm.create("Tester");
        String message = this.comm.handleCommand("exit");
        assertTrue(message.toLowerCase().contains("goodbye"));
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
        assertTrue(room2.contains("room"));
        assertNotEquals(room1, room2);
        String origRoom = this.comm.handleCommand("go west");
        assertNotEquals(room2, origRoom);
        assertEquals(room1, origRoom);

    }

    @Test
    void testDropTake() {
        this.comm.create("Tester");
        this.comm.handleCommand("take longsword", true, false);
        this.comm.handleCommand("drop longsword");
        this.comm.handleCommand("drop longsword");
        this.comm.handleCommand("take longsword");
    }

    @Test
    void testPlayers() throws IOException {
        this.comm.create("Tester");
        ComBundle dude1 = new ComBundle(this.server);
        dude1.create("dude1");
        ComBundle dude2 = new ComBundle(this.server);
        dude2.create("dude2");
        this.comm.read();
        String findEm = this.comm.handleCommand("players");
        assertTrue(findEm.contains(this.comm.name));
        assertTrue(findEm.contains(dude1.name));
        assertTrue(findEm.contains(dude2.name));
        dude2.handleCommand("go east");
        findEm = this.comm.handleCommand("players");
        assertTrue(findEm.contains(this.comm.name));
        assertTrue(findEm.contains(dude1.name));
        assertTrue(findEm.contains(dude2.name));
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
    void testNameCollision() throws IOException {
        this.comm.create("Tester");
        this.comm.handleCommand("create Tester with password", true, false);
        ComBundle twin1 = new ComBundle(this.server);
        twin1.create(this.comm.name, false); // would have failed making twin
        assertNotEquals(this.comm.name, twin1.name);

        // // extract creature name from next room
        // String creatureName = this.comm.handleCommand("go east");
        // int creature_index = creatureName.indexOf("<creature>");
        // int endcreature_index = creatureName.indexOf("</creature>");
        // creatureName = creatureName.substring(creature_index + "<creature>".length(),
        // endcreature_index);
        // System.out.println(creatureName);

        // twin1.create(creatureName);
        // String room2 = twin1.handleCommand("go east");
        // assertTrue(room2.contains(this.comm.name));
        // assertEquals(room2.indexOf(twin1.name), room2.lastIndexOf(twin1.name));

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
        while (room.contains("<creature>" + extract + "</creature>") && i < 15) {
            this.comm.handleCommand("attack " + extract);

            room = this.comm.handleCommand("see");
            i += 1;
        }
        assertTrue(i < 15);
        assertFalse(room.contains("<creature>" + extract + "</creature>"));
    }

    @Test
    void testEquipment() {
        this.comm.create("Tester");
        String status1 = this.comm.handleCommand("status");
        String inventory1 = this.comm.handleCommand("inventory");
        int slotindex = inventory1.indexOf("SHIELD");
        int shieldIndex = inventory1.indexOf("Shield", slotindex);
        assertTrue(slotindex < shieldIndex);
        this.comm.handleCommand("unequip shield");
        assertNotEquals(inventory1, this.comm.handleCommand("inventory"));
        assertNotEquals(status1, this.comm.handleCommand("status"));
        this.comm.handleCommand("equip shield");
        assertEquals(inventory1, this.comm.handleCommand("inventory"));
        assertEquals(status1, this.comm.handleCommand("status"));

    }

    @Test
    void testReincarnation() throws IOException {
        this.comm.create("Tester");
        String status = this.comm.handleCommand("status");
        String inventory = this.comm.handleCommand("inventory");
        this.comm.handleCommand("unequip armor");
        this.comm.handleCommand("unequip shield");
        this.comm.handleCommand("unequip weapon");

        ComBundle attacker = new ComBundle(this.server);
        attacker.create("Attacker");

        String message = new String();
        int i = 0;
        while (!message.contains("reborn") && i < 30) { // until Tester dies
            i++;
            attacker.handleCommand("attack Tester");
            message = this.comm.read();
            if (message.contains("reborn")) {
                break;
            }
            message = this.comm.handleCommand("attack Attacker");
            if (message.contains("reborn")) {
                break;
            }
        }
        assertNotEquals(30, i);
        assertEquals(inventory, this.comm.handleCommand("inventory"));
        assertEquals(status, this.comm.handleCommand("status"));
    }
}
