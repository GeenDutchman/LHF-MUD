package com.lhf.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import com.google.common.truth.Truth;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.SendStrategy;

public class ServerTest {

    public class MessageMatcher implements ArgumentMatcher<OutMessage> {

        protected OutMessageType type;
        protected List<String> contained;
        protected List<String> notContained;

        public MessageMatcher(OutMessageType type, List<String> containedWords, List<String> notContainedWords) {
            this.type = type;
            this.contained = containedWords;
            this.notContained = notContainedWords;
        }

        public MessageMatcher(OutMessageType type, String contained) {
            this.type = type;
            this.contained = List.of(contained);
            this.notContained = null;
        }

        public MessageMatcher(OutMessageType type) {
            this.type = type;
            this.contained = null;
            this.notContained = null;
        }

        public MessageMatcher(String contained) {
            this.contained = List.of(contained);
            this.notContained = null;
            this.type = null;
        }

        @Override
        public boolean matches(OutMessage argument) {
            if (argument == null) {
                return false;
            }
            if (this.type != null && this.type != argument.getOutType()) {
                return false;
            }
            String argumentAsString = argument.toString();

            if (this.contained != null) {
                for (String words : this.contained) {
                    if (!argumentAsString.contains(words)) {
                        return false;
                    }
                }
            }

            if (this.notContained != null) {
                for (String words : this.notContained) {
                    if (argumentAsString.contains(words)) {
                        return false;
                    }
                }
            }
            return true;
        }

    }

    private class ComBundle {
        public Client client;
        public SendStrategy sssb;
        public String name;
        @Captor
        public ArgumentCaptor<OutMessage> outCaptor;

        public ComBundle(Server server) throws IOException {
            this.client = server.clientManager.newClient(server);
            this.sssb = Mockito.mock(SendStrategy.class);
            Mockito.doAnswer(invocation -> {
                Object object = invocation.getArgument(0);
                System.out.print(object.getClass().getName());
                System.out.print(' ');
                System.out.print(Mockito.mockingDetails(this.sssb).getInvocations().size());
                this.print(object.toString(), false);
                return null;
            }).when(this.sssb).send(Mockito.any(OutMessage.class));
            this.client.SetOut(this.sssb);
            server.startClient(this.client);
        }

        public String create(String name, String vocation, Boolean expectUnique) {
            String result = this
                    .handleCommand("create " + name + " with " + name + (vocation != null ? " as " + vocation : ""),
                            expectUnique ? OutMessageType.SEE : OutMessageType.DUPLICATE_USER);
            this.name = name;
            return result;
        }

        public String create(String name) {
            return this.create(name, "fighter", true);
        }

        public String handleCommand(String command, OutMessageType outMessageType) {
            this.print(command, true);
            this.outCaptor = ArgumentCaptor.forClass(OutMessage.class);
            this.client.ProcessString(command);
            Mockito.verify(this.sssb, Mockito.atLeastOnce()).send(this.outCaptor.capture());
            OutMessage outMessage = outCaptor.getValue();
            Truth.assertThat(outMessage).isNotNull();
            String response = outMessage.toString();
            if (outMessageType != null) {
                Truth.assertThat(outMessage.getOutType()).isEqualTo(outMessageType);
            }
            return response;
        }

        public String handleCommand(String command) {
            return this.handleCommand(command, null);
        }

        private String getName() {
            String tempname = String.valueOf(this.hashCode());
            if (this.name != null) {
                tempname += ' ' + this.name;
            }
            return tempname;
        }

        private void print(String buffer, boolean sending) {
            System.out.println("***********************" + this.getName() + "**********************");
            for (String part : buffer.split("\n")) {
                System.out.print(sending ? ">>> " : "<<< ");
                System.out.println(part);
            }
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
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
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(WelcomeMessage.class));
    }

    @Test
    void testFreshExit() {
        String message = this.comm.handleCommand("exit");
        Truth.assertThat(message).ignoringCase().contains("goodbye");
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(UserLeftMessage.class));
    }

    @Test
    void testExitFinality() {
        String message = this.comm.handleCommand("exit", OutMessageType.USER_LEFT);
        Truth.assertThat(message).ignoringCase().contains("goodbye");
        this.comm.handleCommand("see", OutMessageType.BAD_MESSAGE);
        Assertions.assertThrows(NullPointerException.class, () -> {
            this.comm.handleCommand("create Tester with Tester");
        });
    }

    @Test
    void testCharacterCreation() {
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(WelcomeMessage.class));
        String message = this.comm.create("Tester");
        Truth.assertThat(message).ignoringCase().contains("room");
        Truth.assertThat(message).contains(this.comm.name);
    }

    @Test
    void testComplexCharacterCreation() {
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(WelcomeMessage.class));
        String message = this.comm.create("Tester", null, true);
        Truth.assertThat(message).ignoringCase().contains("hi");
        message = this.comm.handleCommand("say hi to gary lovejax");
        message = this.comm.handleCommand("say ok to gary lovejax");
        message = this.comm.handleCommand("say mage to gary lovejax");
        message = this.comm.handleCommand("say thanks to gary lovejax");
        String room1 = this.comm.handleCommand("see");
        Truth.assertThat(room1).contains("east");
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
        this.comm.create("Tester");
        String room1 = this.comm.handleCommand("see", OutMessageType.SEE);
        Truth.assertThat(room1).contains("east");
        String room2 = this.comm.handleCommand("go east", OutMessageType.SEE);
        Truth.assertThat(room2).contains("hall");
        Truth.assertThat(room1).isNotEqualTo(room2);
        String origRoom = this.comm.handleCommand("go west", OutMessageType.SEE);
        Truth.assertThat(room2).isNotEqualTo(origRoom);
        Truth.assertThat(room1).isEqualTo(origRoom);
    }

    @Test
    void testDropTake() {
        this.comm.create("Tester");
        this.comm.handleCommand("inventory");
        this.comm.handleCommand("take longsword", OutMessageType.BAD_TARGET_SELECTED);
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
        String findEm = this.comm.handleCommand("players", OutMessageType.LIST_PLAYERS);
        Truth.assertThat(findEm).contains(this.comm.name);
        Truth.assertThat(findEm).contains(dude1.name);
        Truth.assertThat(findEm).contains(dude2.name);
        dude2.handleCommand("go east", OutMessageType.SEE);
        findEm = this.comm.handleCommand("players", OutMessageType.LIST_PLAYERS);
        Truth.assertThat(findEm).contains(this.comm.name);
        Truth.assertThat(findEm).contains(dude1.name);
        Truth.assertThat(findEm).contains(dude2.name);
        dude2.handleCommand("exit", OutMessageType.USER_LEFT);
        Mockito.verify(this.comm.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.any(UserLeftMessage.class));
        findEm = this.comm.handleCommand("players", OutMessageType.LIST_PLAYERS);
        Truth.assertThat(findEm).contains(this.comm.name);
        Truth.assertThat(findEm).contains(dude1.name);
        Truth.assertThat(findEm).doesNotContain(dude2.name);
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    public class SpeakingTests {

        protected ComBundle listener1;
        protected ComBundle listener2;
        protected MessageMatcher matcher;

        @BeforeEach
        public void initEach() throws IOException {
            ServerTest.this.comm.create("Tester");
            this.listener1 = new ComBundle(ServerTest.this.server);
            listener1.create("Listener1");
            this.listener2 = new ComBundle(ServerTest.this.server);
            listener2.create("Listener2");
            String room = ServerTest.this.comm.handleCommand("see", OutMessageType.SEE);
            Truth.assertThat(room).contains(ServerTest.this.comm.name);
            Truth.assertThat(room).contains(listener1.name);
            Truth.assertThat(room).contains(listener2.name);
        }

        @Test
        void testSpeakToRoom() {
            this.matcher = new MessageMatcher(OutMessageType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "this is a unique string"), null);
            ServerTest.this.comm.handleCommand("say this is a unique string");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
            Mockito.verify(listener2.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
        }

        @Test
        void testSpeakDirectly() {
            this.matcher = new MessageMatcher(OutMessageType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "hey you man"), null);
            ServerTest.this.comm.handleCommand("say hey you man to Listener1");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce())
                    .send(Mockito.argThat(matcher));
            Mockito.verify(listener2.sssb, Mockito.after(1000).never())
                    .send(Mockito.argThat(matcher));
        }

        @Test
        void testShoutSameRoom() {
            ServerTest.this.comm.handleCommand("shout hello world");
            this.matcher = new MessageMatcher(OutMessageType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "hello world"), null);
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(matcher));
            Mockito.verify(listener2.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(matcher));
        }

        @Test
        void testSpeakDifferentRoom() {
            listener2.handleCommand("go east", OutMessageType.SEE);
            this.matcher = new MessageMatcher(OutMessageType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "zaboomafoo"), null);
            ServerTest.this.comm.handleCommand("say zaboomafoo");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce())
                    .send(Mockito.argThat(matcher));
            Mockito.verify(listener2.sssb, Mockito.after(1000).never()).send(Mockito.argThat(matcher));
        }

        @Test
        void testSpeakDirectlyWithDifferentRoom() {
            listener2.handleCommand("go east", OutMessageType.SEE);

            this.matcher = new MessageMatcher(OutMessageType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "lil dip sauce"), null);
            ServerTest.this.comm.handleCommand("say lil dip sauce to Listener1");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
            Mockito.verify(listener2.sssb, Mockito.after(1000).never()).send(Mockito.argThat(this.matcher));

        }

        @Test
        void testShoutWithDifferentRoom() {
            listener2.handleCommand("go east", OutMessageType.SEE);

            this.matcher = new MessageMatcher(OutMessageType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "I like yelling"), null);
            ServerTest.this.comm.handleCommand("shout I like yelling");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
            Mockito.verify(listener2.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
        }

    }

    @Test
    void testNameCollision() throws IOException {
        this.comm.create("Tester");
        this.comm.handleCommand("create Tester with password", OutMessageType.BAD_MESSAGE);
        ComBundle twin1 = new ComBundle(this.server);
        twin1.create(this.comm.name, "fighter", false); // would have failed making twin
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
        String extract = this.comm.handleCommand("go east", OutMessageType.SEE);
        Truth.assertThat(extract).ignoringCase().contains("<monster>");
        int creature_index = extract.indexOf("<monster>");
        int endcreature_index = extract.indexOf("</monster>");
        extract = extract.substring(creature_index + "<monster>".length(), endcreature_index);
        System.out.println(extract);
        String room = this.comm.handleCommand("see", OutMessageType.SEE);
        for (int i = 1; i < 15; i++) {
            this.comm.handleCommand("attack " + extract);

            room = this.comm.handleCommand("see");

            if (room.contains("<monster>" + extract + "</monster>")) {
                Mockito.verify(this.comm.sssb, Mockito.timeout(1000).times(i))
                        .send(Mockito.argThat(message -> message != null
                                && message.getOutType() == OutMessageType.BATTLE_TURN
                                && message.toString().contains("It is your turn to fight!")));
            } else {
                break;
            }
        }
        Truth.assertThat(room).doesNotContain("<monster>" + extract + "</monster>");
    }

    @Test
    void testEquipment() {
        this.comm.create("Tester");
        String status1 = this.comm.handleCommand("status");
        String inventory1 = this.comm.handleCommand("inventory");
        Truth.assertThat(inventory1).contains("Shield");
        Truth.assertThat(inventory1).contains("nothing equipped");
        this.comm.handleCommand("equip shield");
        Truth.assertThat(this.comm.handleCommand("inventory")).isNotEqualTo(inventory1);
        Truth.assertThat(this.comm.handleCommand("status")).isNotEqualTo(status1);
        this.comm.handleCommand("unequip shield");
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

        OutMessage outMessage = null;
        for (int i = 0; i < 30 && outMessage == null; i++) {
            attacker.handleCommand("attack Tester");
            for (OutMessage outy : this.comm.outCaptor.getAllValues()) {
                if (outy != null && outy.getOutType() == OutMessageType.REINCARNATION) {
                    outMessage = outy;
                }
            }
            // message = this.comm.read();
            if (outMessage != null) {
                break;
            }
            this.comm.handleCommand("PASS");
            for (OutMessage outy : this.comm.outCaptor.getAllValues()) {
                if (outy != null && outy.getOutType() == OutMessageType.REINCARNATION) {
                    outMessage = outy;
                }
            }
        }
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
        Mockito.verify(bystander.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito
                .argThat(message -> message != null && message.getOutType() == OutMessageType.RENEGADE_ANNOUNCEMENT));
        Mockito.verify(bystander.sssb, Mockito.timeout(500).atLeastOnce()).send(
                Mockito.argThat(message -> message != null && message.getOutType() == OutMessageType.JOIN_BATTLE));
    }

    @Test
    void testCasting() throws IOException {
        this.comm.create("Tester");
        ComBundle victim = new ComBundle(this.server);
        victim.create("victim");

        String spellResult = this.comm.handleCommand("cast zarmamoo"); // Thaumaturgy
        // because we know it's thaumaturgy
        // TODO: make a test with a caster type
        // Truth.assertThat(spellResult).contains(this.comm.name);
        Truth.assertThat(spellResult).ignoringCase().contains("not a caster");
        // Truth.assertThat(victim.read()).contains(this.comm.name);
        Mockito.verify(victim.sssb, Mockito.timeout(500).atLeastOnce())
                .send(Mockito.argThat(message -> message != null && message.getOutType() == OutMessageType.FIZZLE));

        spellResult = this.comm.handleCommand("cast Astra Horeb at " + victim.name); // attack spell
        // Truth.assertThat(spellResult).ignoringCase().contains("fight");
        Truth.assertThat(spellResult).ignoringCase().contains("not a caster");
    }
}
