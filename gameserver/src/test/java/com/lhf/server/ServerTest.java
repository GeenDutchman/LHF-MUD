package com.lhf.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.server.client.Client;
import com.lhf.server.client.StringBufferSendStrategy;

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
                Truth.assertThat(alreadyExists).isFalse();
                this.name = name;
            } else {
                Truth.assertThat(alreadyExists).isTrue();
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
                Truth.assertThat(notRecognized).isFalse();
            } else {
                Truth.assertThat(notRecognized).isTrue();
            }
            Boolean notHandled = response.toLowerCase().contains("was not handled");
            if (expectHandled) {
                Truth.assertThat(notHandled).isFalse();
                ;
            } else {
                Truth.assertThat(notHandled).isTrue();
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
        Truth.assertThat(message).ignoringCase().contains("Welcome");
    }

    @Test
    void testFreshExit() {
        this.comm.read();
        String message = this.comm.handleCommand("exit");
        Truth.assertThat(message).ignoringCase().contains("goodbye");
    }

    @Test
    void testExitFinality() {

        this.comm.read();
        String message = this.comm.handleCommand("exit");
        Truth.assertThat(message).ignoringCase().contains("goodbye");
        this.comm.handleCommand("see", true, false);
        Assertions.assertThrows(NullPointerException.class, () -> {
            this.comm.handleCommand("create Tester with Tester");
        });
    }

    @Test
    void testCharacterCreation() {
        String message = this.comm.read();
        Truth.assertThat(message).ignoringCase().contains("create");
        message = this.comm.create("Tester");
        Truth.assertThat(message).ignoringCase().contains("room");
        Truth.assertThat(message).contains(this.comm.name);
    }

    @Test
    void testCreatedExit() {
        this.comm.create("Tester");
        String message = this.comm.handleCommand("exit");
        Truth.assertThat(message).ignoringCase().contains("goodbye");
    }

    @Test
    void testLook() {
        this.comm.create("Tester");
        String room = this.comm.handleCommand("see");
        Truth.assertThat(room).contains("room");
    }

    @Test
    void testGo() {
        this.comm.clear();
        String room1 = this.comm.create("Tester");
        Truth.assertThat(room1).contains("east");
        String room2 = this.comm.handleCommand("go east");
        Truth.assertThat(room2).contains("room");
        Truth.assertThat(room1).isNotEqualTo(room2);
        String origRoom = this.comm.handleCommand("go west");
        Truth.assertThat(room2).isNotEqualTo(origRoom);
        Truth.assertThat(room1).isEqualTo(origRoom);
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
        Truth.assertThat(findEm).contains(this.comm.name);
        Truth.assertThat(findEm).contains(dude1.name);
        Truth.assertThat(findEm).contains(dude2.name);
        dude2.handleCommand("go east");
        findEm = this.comm.handleCommand("players");
        Truth.assertThat(findEm).contains(this.comm.name);
        Truth.assertThat(findEm).contains(dude1.name);
        Truth.assertThat(findEm).contains(dude2.name);
        dude2.handleCommand("exit");
        findEm = this.comm.handleCommand("players");
        Truth.assertThat(findEm).contains(this.comm.name);
        Truth.assertThat(findEm).contains(dude1.name);
        Truth.assertThat(findEm).doesNotContain(dude2.name);
    }

    @Test
    void testSpeaking() throws IOException {
        this.comm.create("Tester");
        ComBundle listener1 = new ComBundle(this.server);
        listener1.create("Listener1");
        ComBundle listener2 = new ComBundle(this.server);
        listener2.create("Listener2");
        String room = this.comm.handleCommand("see");
        Truth.assertThat(room).contains(this.comm.name);
        Truth.assertThat(room).contains(listener1.name);
        Truth.assertThat(room).contains(listener2.name);
        this.comm.handleCommand("say this is a unique string");
        String heard1 = listener1.read();
        Truth.assertThat(heard1).contains(this.comm.name);
        Truth.assertThat(heard1).contains("this is a unique string");
        String heard2 = listener2.read();
        Truth.assertThat(heard2).contains(this.comm.name);
        Truth.assertThat(heard2).contains("this is a unique string");

        this.comm.handleCommand("say hey you man to Listener1");
        heard1 = listener1.read();
        Truth.assertThat(heard1).contains(this.comm.name);
        Truth.assertThat(heard1).contains("hey you man");
        heard2 = listener2.read();
        Truth.assertThat(heard2).doesNotContain(this.comm.name);
        Truth.assertThat(heard2).doesNotContain("hey you man");

        this.comm.handleCommand("shout hello world");
        heard1 = listener1.read();
        Truth.assertThat(heard1).contains(this.comm.name);
        Truth.assertThat(heard1).contains("hello world");
        heard2 = listener2.read();
        Truth.assertThat(heard2).contains(this.comm.name);
        Truth.assertThat(heard2).contains("hello world");

        // Test from different room
        listener2.handleCommand("go east");

        this.comm.handleCommand("say zaboomafoo");
        heard1 = listener1.read();
        Truth.assertThat(heard1).contains(this.comm.name);
        Truth.assertThat(heard1).contains("zaboomafoo");
        heard2 = listener2.read();
        Truth.assertThat(heard2).doesNotContain(this.comm.name);
        Truth.assertThat(heard2).doesNotContain("zaboomafoo");

        this.comm.handleCommand("say lil dip sauce to Listener1");
        heard1 = listener1.read();
        Truth.assertThat(heard1).contains(this.comm.name);
        Truth.assertThat(heard1).contains("lil dip sauce");
        heard2 = listener2.read();
        Truth.assertThat(heard2).doesNotContain(this.comm.name);
        Truth.assertThat(heard2).doesNotContain("lil dip sauce");

        this.comm.handleCommand("shout I like yelling");
        heard1 = listener1.read();
        Truth.assertThat(heard1).contains(this.comm.name);
        Truth.assertThat(heard1).contains("I like yelling");
        heard2 = listener2.read();
        Truth.assertThat(heard2).contains(this.comm.name);
        Truth.assertThat(heard2).contains("I like yelling");

    }

    @Test
    void testNameCollision() throws IOException {
        this.comm.create("Tester");
        this.comm.handleCommand("create Tester with password", true, false);
        ComBundle twin1 = new ComBundle(this.server);
        twin1.create(this.comm.name, false); // would have failed making twin
        Truth.assertThat(twin1.name).isNotEqualTo(this.comm.name);

        // // extract creature name from next room
        // String creatureName = this.comm.handleCommand("go east");
        // int creature_index = creatureName.indexOf("<monster>");
        // int endcreature_index = creatureName.indexOf("</monster>");
        // creatureName = creatureName.substring(creature_index + "<monster>".length(),
        // endcreature_index);
        // System.out.println(creatureName);

        // twin1.create(creatureName);
        // String room2 = twin1.handleCommand("go east");
        // Truth.assertThat(room2).contains(this.comm.name);
        // Truth.assertThat(room2.indexOf(twin1.name)).isEqualTo(room2.lastIndexOf(twin1.name));

    }

    @Test
    void testAttack() {
        this.comm.create("Tester");
        String extract = this.comm.handleCommand("go east");
        int creature_index = extract.indexOf("<monster>");
        int endcreature_index = extract.indexOf("</monster>");
        extract = extract.substring(creature_index + "<monster>".length(), endcreature_index);
        System.out.println(extract);
        String room = this.comm.handleCommand("see");
        int i = 0;
        while (room.contains("<monster>" + extract + "</monster>") && i < 15) {
            this.comm.handleCommand("attack " + extract);

            room = this.comm.handleCommand("see");
            i += 1;
        }
        Truth.assertThat(i).isLessThan(15);
        Truth.assertThat(room).doesNotContain("<monster>" + extract + "</monster>");
    }

    @Test
    void testEquipment() {
        this.comm.create("Tester");
        String status1 = this.comm.handleCommand("status");
        String inventory1 = this.comm.handleCommand("inventory");
        int slotindex = inventory1.indexOf("SHIELD");
        int shieldIndex = inventory1.indexOf("Shield", slotindex);
        Truth.assertThat(slotindex).isLessThan(shieldIndex);
        this.comm.handleCommand("unequip shield");
        Truth.assertThat(this.comm.handleCommand("inventory")).isNotEqualTo(inventory1);
        Truth.assertThat(this.comm.handleCommand("status")).isNotEqualTo(status1);
        this.comm.handleCommand("equip shield");
        Truth.assertThat(this.comm.handleCommand("inventory")).isEqualTo(inventory1);
        Truth.assertThat(this.comm.handleCommand("status")).isEqualTo(status1);
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
        Truth.assertThat(i).isNotEqualTo(30);
        Truth.assertThat(this.comm.handleCommand("inventory")).isEqualTo(inventory);
        Truth.assertThat(this.comm.handleCommand("status")).isEqualTo(status);
    }

    @Test
    void testReinforcements() throws IOException {
        this.comm.create("Tester");
        ComBundle second = new ComBundle(this.server);
        second.create("second");
        ComBundle bystander = new ComBundle(this.server);
        bystander.create("bystander");

        String attack = this.comm.handleCommand("attack " + second.name);
        Truth.assertThat(attack).contains("RENEGADE");
        String seen = bystander.read();
        Truth.assertThat(seen).contains("RENEGADE");
        Truth.assertThat(seen).contains("joined");
        Truth.assertThat(seen).contains("battle!");
    }

    @Test
    void testCasting() throws IOException {
        this.comm.create("Tester");
        ComBundle victim = new ComBundle(this.server);
        victim.create("victim");
        victim.read();
        String spellResult = this.comm.handleCommand("cast zarmamoo"); // Thaumaturgy
        // because we know it's thaumaturgy
        // TODO: make a test with a caster type
        // Truth.assertThat(spellResult).contains(this.comm.name);
        Truth.assertThat(spellResult).ignoringCase().contains("not a caster");
        // Truth.assertThat(victim.read()).contains(this.comm.name);
        Truth.assertThat(victim.read()).ignoringCase().contains("nothing spectacular happens");

        spellResult = this.comm.handleCommand("cast Astra Horeb at " + victim.name); // attack spell
        // Truth.assertThat(spellResult).ignoringCase().contains("fight");
        Truth.assertThat(spellResult).ignoringCase().contains("not a caster");
    }
}
