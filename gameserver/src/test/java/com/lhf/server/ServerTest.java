package com.lhf.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.Game;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.map.DungeonBuilder;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.UserLeftMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.ClientManager;
import com.lhf.server.client.ComBundle;
import com.lhf.server.client.user.UserManager;

@ExtendWith(MockitoExtension.class)
public class ServerTest {

    private class ServerClientComBundle extends ComBundle {
        public Client client;
        public String name;

        public ServerClientComBundle(Server server) throws IOException {
            super();
            this.client = server.clientManager.newClient(server);
            this.client.SetOut(this.sssb);
            server.startClient(this.client);
        }

        public String create(String name, String vocation, Boolean expectUnique) {
            String command = "create " + name + " with " + name + (vocation != null ? " as " + vocation : "");
            String result = this.handleCommand(command,
                    expectUnique ? OutMessageType.SEE : OutMessageType.DUPLICATE_USER);
            OutMessage outMessage = this.outCaptor.getValue();
            if (expectUnique && outMessage != null
                    && outMessage.getOutType() != OutMessageType.DUPLICATE_USER
                    && outMessage.getOutType() != OutMessageType.BAD_MESSAGE) {
                this.name = name;
            }
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
                Truth.assertWithMessage("Message is: %s", response).that(outMessage.getOutType())
                        .isEqualTo(outMessageType);
            }
            return response;
        }

        public String handleCommand(String command) {
            return this.handleCommand(command, null);
        }

        @Override
        protected String getName() {
            if (this.name != null) {
                return String.valueOf(this.client.getClientID().hashCode()) + ' ' + this.name;
            }
            return String.valueOf(this.client.getClientID().hashCode());
        }

    }

    @InjectMocks
    protected Server server;
    protected ServerClientComBundle comm;

    UserManager userManager;
    ClientManager clientManager;

    @BeforeEach
    public void initEach() {
        try {
            this.userManager = new UserManager();
            this.clientManager = new ClientManager();
            AIRunner aiRunner = new GroupAIRunner(true, 2, 250, TimeUnit.MILLISECONDS);
            Game game = new Game(null, this.userManager, aiRunner, DungeonBuilder.buildStaticDungeon(null, aiRunner));
            this.server = new Server(this.userManager, this.clientManager, game);
            this.comm = new ServerClientComBundle(this.server);
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
        this.comm.handleCommand("CREATE Tester with Tester", null); // we won't see anything, just be
                                                                    // greeted
        Mockito.verify(this.comm.sssb, Mockito.timeout(2000))
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SPEAKING, "to make a character you need")));
        this.comm.handleCommand("say hi to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(2000))
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SPEAKING, "intro lore placeholder here")));
        this.comm.handleCommand("say ok to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(2000))
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SPEAKING, "MAGE")));
        this.comm.handleCommand("say mage to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(2000))
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SPEAKING, "You have selected MAGE")));
        this.comm.handleCommand("say ready to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(2000))
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.SEE)));
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
        this.comm.handleCommand("take longsword", OutMessageType.BAD_MESSAGE);
        this.comm.handleCommand("drop longsword");
        this.comm.handleCommand("drop longsword");
        this.comm.handleCommand("take longsword");
    }

    @Test
    void testPlayers() throws IOException {
        this.comm.create("Tester");
        ServerClientComBundle dude1 = new ServerClientComBundle(this.server);
        dude1.create("dude1");
        ServerClientComBundle dude2 = new ServerClientComBundle(this.server);
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

        protected ServerClientComBundle listener1;
        protected ServerClientComBundle listener2;
        protected MessageMatcher matcher;

        @BeforeEach
        public void initEach() throws IOException {
            ServerTest.this.comm.create("Tester");
            this.listener1 = new ServerClientComBundle(ServerTest.this.server);
            listener1.create("Listener1");
            this.listener2 = new ServerClientComBundle(ServerTest.this.server);
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
        ServerClientComBundle twin1 = new ServerClientComBundle(this.server);
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
        this.comm.create("AttackTester");
        String extract = this.comm.handleCommand("go east", OutMessageType.SEE);
        Truth.assertThat(extract).ignoringCase().contains("<monster>");
        int creature_index = extract.indexOf("<monster>");
        int endcreature_index = extract.indexOf("</monster>");
        extract = extract.substring(creature_index + "<monster>".length(), endcreature_index);
        System.out.println(extract);
        String room = this.comm.handleCommand("see", OutMessageType.SEE);
        ArgumentMatcher<OutMessage> battleTurn = new MessageMatcher(OutMessageType.BATTLE_ROUND,
                "should enter an action to take for the round");
        ArgumentMatcher<OutMessage> battleTurnAccepted = new MessageMatcher(OutMessageType.BATTLE_ROUND,
                "action has been submitted for the round");
        ArgumentMatcher<OutMessage> fightOver = new MessageMatcher(OutMessageType.FIGHT_OVER);
        ArgumentMatcher<OutMessage> reincarnated = new MessageMatcher(OutMessageType.REINCARNATION);
        for (int i = 1; i < 15 && room.contains("<monster>" + extract + "</monster>"); i++) {
            this.comm.handleCommand("attack " + extract);
            Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(i))
                    .send(Mockito.argThat(battleTurnAccepted));

            Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(i))
                    .send(Mockito.argThat((outMessage) -> {
                        return battleTurn.matches(outMessage) || fightOver.matches(outMessage)
                                || reincarnated.matches(outMessage);
                    }));
            room = this.comm.handleCommand("see");

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
        this.comm.handleCommand("go east");
        String status = this.comm.handleCommand("status");
        String inventory = this.comm.handleCommand("inventory");

        // divest ourselves of everything, just in case
        this.comm.handleCommand("unequip armor");
        this.comm.handleCommand("unequip shield");
        this.comm.handleCommand("unequip weapon");

        ServerClientComBundle attacker = new ServerClientComBundle(this.server);
        attacker.create("Attacker");
        attacker.handleCommand("go east");
        attacker.handleCommand("equip armor");
        attacker.handleCommand("equip sword");
        attacker.handleCommand("equip shield");
        attacker.handleCommand("status");

        ArgumentMatcher<OutMessage> battleTurn = new MessageMatcher(OutMessageType.BATTLE_ROUND,
                "should enter an action to take for the round");
        ArgumentMatcher<OutMessage> battleTurnAccepted = new MessageMatcher(OutMessageType.BATTLE_ROUND,
                "action has been submitted for the round");

        attacker.handleCommand("attack Tester");
        this.comm.handleCommand("PASS");
        for (int i = 1; i < 30 && this.comm.outCaptor.getAllValues().stream()
                .noneMatch(outy -> outy != null && OutMessageType.REINCARNATION.equals(outy.getOutType())); i++) {
            Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(i))
                    .send(Mockito.argThat(battleTurn));
            Mockito.verify(attacker.sssb, Mockito.timeout(500).atLeast(i - 1)) // minus one because the first one is
                                                                               // already submitted
                    .send(Mockito.argThat(battleTurn));
            attacker.handleCommand("attack Tester");
            this.comm.handleCommand("PASS");
            Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(i))
                    .send(Mockito.argThat(battleTurnAccepted));
            Mockito.verify(attacker.sssb, Mockito.timeout(500).atLeast(i))
                    .send(Mockito.argThat(battleTurnAccepted));

        }
        Truth.assertThat(attacker.handleCommand("SEE")).doesNotContain("Tester");
        Truth.assertThat(this.comm.handleCommand("inventory")).isEqualTo(inventory);
        Truth.assertThat(this.comm.handleCommand("status")).isEqualTo(status);
    }

    @Test
    void testReinforcements() throws IOException {
        this.comm.create("Tester");
        ServerClientComBundle second = new ServerClientComBundle(this.server);
        second.create("second");
        ServerClientComBundle bystander = new ServerClientComBundle(this.server);
        bystander.create("bystander");

        this.comm.handleCommand("attack " + second.name);
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce())
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.RENEGADE_ANNOUNCEMENT, "RENEGADE")));
        Mockito.verify(bystander.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito
                .argThat(new MessageMatcher(OutMessageType.RENEGADE_ANNOUNCEMENT)));
        Mockito.verify(bystander.sssb, Mockito.timeout(500).atLeastOnce()).send(
                Mockito.argThat(new MessageMatcher(OutMessageType.JOIN_BATTLE)));
    }

    @Test
    void testCasting() throws IOException {
        this.comm.create("Tester");
        ServerClientComBundle victim = new ServerClientComBundle(this.server);
        victim.create("victim");
        ServerClientComBundle caster = new ServerClientComBundle(this.server);
        caster.create("Caster", "MAGE", true);

        String spellResult = this.comm.handleCommand("cast zarmamoo"); // Thaumaturgy
        // because we know it's thaumaturgy
        // Truth.assertThat(spellResult).contains(this.comm.name);
        Truth.assertThat(spellResult).ignoringCase()
                .contains("was not handled");
        // Truth.assertThat(victim.read()).contains(this.comm.name);

        spellResult = this.comm.handleCommand("cast Astra Horeb at " + victim.name); // attack spell
        // Truth.assertThat(spellResult).ignoringCase().contains("fight");
        Truth.assertThat(spellResult).ignoringCase()
                .contains("was not handled");

        spellResult = caster.handleCommand("cast zarmamoo");
        Truth.assertThat(spellResult).ignoringCase().contains("used");
        Truth.assertThat(spellResult).ignoringCase().contains("Thaumaturgy");

    }

    @Test
    void testSpellbook() throws IOException {
        this.comm.create("Notmage");
        ServerClientComBundle mage = new ServerClientComBundle(this.server);
        mage.create("mage", "MAGE", true);

        this.comm.handleCommand("spellbook");
        Mockito.verify(this.comm.sssb, Mockito.timeout(500))
                .send(Mockito.argThat(new MessageMatcher(OutMessageType.BAD_MESSAGE, "was not handled")));
        mage.handleCommand("spellbook");
        Mockito.verify(mage.sssb, Mockito.timeout(500)).send(
                Mockito.argThat(new MessageMatcher(OutMessageType.SPELL_ENTRY, "Thaumaturgy")));
    }
}
