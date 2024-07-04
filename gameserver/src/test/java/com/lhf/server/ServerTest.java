package com.lhf.server;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.lhf.game.Atlas.AtlasException;
import com.lhf.game.Game.GameBuilder;
import com.lhf.game.creature.BuildInfoManager;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.map.Dungeon.DungeonBuilder;
import com.lhf.game.map.StandardDungeonProducer;
import com.lhf.game.map.SubArea;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.GameEventType;
import com.lhf.messages.MessageMatcher;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventSubject;
import com.lhf.messages.events.UserLeftEvent;
import com.lhf.messages.events.WelcomeEvent;
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

        public GameEvent create(String name, String vocation, Boolean expectUnique) {
            String command = "create " + name + " with " + name + (vocation != null ? " as " + vocation : "");
            GameEvent gameEvent = this.handleCommand(command,
                    expectUnique ? GameEventType.SEE : GameEventType.DUPLICATE_USER);
            if (expectUnique && gameEvent != null && gameEvent.getXmlEventType() != GameEventType.DUPLICATE_USER
                    && gameEvent.getXmlEventType() != GameEventType.BAD_MESSAGE) {
                this.name = name;
            }
            return gameEvent;
        }

        public GameEvent create(String name) {
            return this.create(name, "fighter", true);
        }

        public GameEvent handleCommand(String command, GameEventType outMessageType) {
            this.print(command, true);
            this.outCaptor = ArgumentCaptor.forClass(GameEvent.class);
            Reply reply = this.client.ProcessString(command);
            Mockito.verify(this.sssb, Mockito.atLeastOnce()).send(this.outCaptor.capture());
            Truth.assertWithMessage("Command %s has no reply", command).that(reply).isNotNull();
            Truth.assertWithMessage("Command %s should have been handled, but reply was %s", command, reply)
                    .that(reply.isHandled()).isTrue();
            GameEvent gameEvent = outCaptor.getValue();
            Truth.assertThat(gameEvent).isNotNull();
            if (outMessageType != null) {
                Truth.assertWithMessage("Message is: %s", gameEvent).that(gameEvent.getXmlEventType())
                        .isEqualTo(outMessageType);
            }
            return gameEvent;
        }

        public GameEvent handleCommand(String command) {
            return this.handleCommand(command, null);
        }

        @Override
        protected String getName() {
            if (this.name != null) {
                return String.valueOf(this.client.getEventProcessorID().hashCode()) + ' ' + this.name;
            }
            return String.valueOf(this.client.getEventProcessorID().hashCode());
        }

    }

    @InjectMocks
    protected Server server;
    protected ServerClientComBundle comm;

    UserManager userManager;
    ClientManager clientManager;

    @BeforeEach
    public void initEach() throws JsonIOException, JsonSyntaxException, AtlasException {
        try {
            this.userManager = new UserManager();
            this.clientManager = new ClientManager();
            AIRunner aiRunner = new GroupAIRunner(true, 2, 250, TimeUnit.MILLISECONDS);
            ConversationManager conversationManager = new ConversationManager();
            BuildInfoManager statblockManager = new BuildInfoManager();
            final DungeonBuilder defaultDungeon = StandardDungeonProducer.buildStaticDungeonBuilder(statblockManager);
            GameBuilder gameBuilder = new GameBuilder().setAiRunner(aiRunner)
                    .setConversationManager(conversationManager).setStatblockManager(statblockManager)
                    .arrangeLandsInline(trawler -> {
                        if (trawler != null) {
                            trawler.plainAddMember(defaultDungeon);
                        }
                    });
            this.server = new Server(this.userManager, this.clientManager, gameBuilder);
            this.comm = new ServerClientComBundle(this.server);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void testServerInitialMessage() {
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(WelcomeEvent.class));
    }

    @Test
    void testFreshExit() {
        GameEventSubject.assertThat(this.comm.handleCommand("exit")).asString().ignoringCase().contains("goodbye");
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(UserLeftEvent.class));
    }

    @Test
    void testExitFinality() {
        GameEvent message = this.comm.handleCommand("exit", GameEventType.USER_LEFT);
        GameEventSubject.assertThat(message).asString().ignoringCase().contains("goodbye");
        this.comm.handleCommand("see", GameEventType.BAD_MESSAGE);
        Assertions.assertThrows(NullPointerException.class, () -> {
            this.comm.handleCommand("create Tester with Tester");
        });
    }

    @Test
    void testCharacterCreation() {
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(WelcomeEvent.class));
        GameEvent message = this.comm.create("Tester");
        GameEventSubject.assertThat(message).ignoringCase().contains("room");
        GameEventSubject.assertThat(message).contains(this.comm.name);
    }

    @Test
    void testComplexCharacterCreation() {
        final long waitMillis = 2000;
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.any(WelcomeEvent.class));
        this.comm.handleCommand("CREATE Tester with Tester", null); // we won't see anything, just be
                                                                    // greeted
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SPEAKING, "to make a character you need")));
        this.comm.handleCommand("say hi to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SPEAKING, "Intro lore placeholder here")));
        this.comm.handleCommand("say ok to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SPEAKING, "MAGE")));
        this.comm.handleCommand("say mage to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SPEAKING, "You have selected MAGE")));
        this.comm.handleCommand("say ready to gary lovejax");
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SEE)));
        GameEvent room1 = this.comm.handleCommand("see");
        GameEventSubject.assertThat(room1).contains("east");
    }

    @Test
    void testCreatedExit() {
        this.comm.create("Tester");
        GameEvent message = this.comm.handleCommand("exit");
        GameEventSubject.assertThat(message).ignoringCase().contains("goodbye");
    }

    @Test
    void testLook() {
        this.comm.create("Tester");
        GameEvent room = this.comm.handleCommand("see");
        GameEventSubject.assertThat(room).contains("room");
    }

    @Test
    void testGo() {
        this.comm.create("Tester");
        GameEvent room1 = this.comm.handleCommand("see", GameEventType.SEE);
        GameEventSubject.assertThat(room1).contains("east");
        GameEvent room2 = this.comm.handleCommand("go east", GameEventType.SEE);
        GameEventSubject.assertThat(room2).contains("hall");
        GameEventSubject.assertThat(room1).isNotEqualTo(room2);
        GameEvent origRoom = this.comm.handleCommand("go west", GameEventType.SEE);
        GameEventSubject.assertThat(room2).asString().isNotEqualTo(origRoom.printString());
        GameEventSubject.assertThat(room1).asString().isEqualTo(origRoom.printString());
    }

    @Test
    void testDropTake() {
        this.comm.create("Tester");
        this.comm.handleCommand("inventory");
        this.comm.handleCommand("take longsword", GameEventType.BAD_MESSAGE);
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
        GameEvent findEm = this.comm.handleCommand("players", GameEventType.LIST_PLAYERS);
        GameEventSubject.assertThat(findEm).contains(this.comm.name);
        GameEventSubject.assertThat(findEm).contains(dude1.name);
        GameEventSubject.assertThat(findEm).contains(dude2.name);
        dude2.handleCommand("go east", GameEventType.SEE);
        findEm = this.comm.handleCommand("players", GameEventType.LIST_PLAYERS);
        GameEventSubject.assertThat(findEm).contains(this.comm.name);
        GameEventSubject.assertThat(findEm).contains(dude1.name);
        GameEventSubject.assertThat(findEm).contains(dude2.name);
        dude2.handleCommand("exit", GameEventType.USER_LEFT);
        Mockito.verify(this.comm.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.any(UserLeftEvent.class));
        findEm = this.comm.handleCommand("players", GameEventType.LIST_PLAYERS);
        GameEventSubject.assertThat(findEm).contains(this.comm.name);
        GameEventSubject.assertThat(findEm).contains(dude1.name);
        GameEventSubject.assertThat(findEm).doesNotContain(dude2.name);
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
            GameEvent room = ServerTest.this.comm.handleCommand("see", GameEventType.SEE);
            GameEventSubject.assertThat(room).contains(ServerTest.this.comm.name);
            GameEventSubject.assertThat(room).contains(listener1.name);
            GameEventSubject.assertThat(room).contains(listener2.name);
        }

        @Test
        void testSpeakToRoom() {
            this.matcher = new MessageMatcher(GameEventType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "this is a unique string"), null);
            ServerTest.this.comm.handleCommand("say this is a unique string");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
            Mockito.verify(listener2.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
        }

        @Test
        void testSpeakDirectly() {
            this.matcher = new MessageMatcher(GameEventType.SPEAKING, List.of(ServerTest.this.comm.name, "hey you man"),
                    null);
            ServerTest.this.comm.handleCommand("say hey you man to Listener1");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(matcher));
            Mockito.verify(listener2.sssb, Mockito.after(1000).never()).send(Mockito.argThat(matcher));
        }

        @Test
        void testShoutSameRoom() {
            ServerTest.this.comm.handleCommand("shout hello world");
            this.matcher = new MessageMatcher(GameEventType.SPEAKING, List.of(ServerTest.this.comm.name, "hello world"),
                    null);
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(matcher));
            Mockito.verify(listener2.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(matcher));
        }

        @Test
        void testSpeakDifferentRoom() {
            listener2.handleCommand("go east", GameEventType.SEE);
            this.matcher = new MessageMatcher(GameEventType.SPEAKING, List.of(ServerTest.this.comm.name, "zaboomafoo"),
                    null);
            ServerTest.this.comm.handleCommand("say zaboomafoo");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(matcher));
            Mockito.verify(listener2.sssb, Mockito.after(1000).never()).send(Mockito.argThat(matcher));
        }

        @Test
        void testSpeakDirectlyWithDifferentRoom() {
            listener2.handleCommand("go east", GameEventType.SEE);

            this.matcher = new MessageMatcher(GameEventType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "lil dip sauce"), null);
            ServerTest.this.comm.handleCommand("say lil dip sauce to Listener1");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
            Mockito.verify(listener2.sssb, Mockito.after(1000).never()).send(Mockito.argThat(this.matcher));

        }

        @Test
        void testShoutWithDifferentRoom() {
            listener2.handleCommand("go east", GameEventType.SEE);

            this.matcher = new MessageMatcher(GameEventType.SPEAKING,
                    List.of(ServerTest.this.comm.name, "I like yelling"), null);
            ServerTest.this.comm.handleCommand("shout I like yelling");
            Mockito.verify(listener1.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
            Mockito.verify(listener2.sssb, Mockito.timeout(1000).atLeastOnce()).send(Mockito.argThat(this.matcher));
        }

    }

    @Test
    void testNameCollision() throws IOException {
        this.comm.create("Tester");
        this.comm.handleCommand("create Tester with password", GameEventType.BAD_MESSAGE);
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
        // GameEventSubject.assertThat(room2).contains(this.comm.name);
        // GameEventSubject.assertThat(room2.indexOf(twin1.name)).isEqualTo(room2.lastIndexOf(twin1.name));

    }

    @Test
    void testAttackMonster() {
        this.comm.create("AttackTester");
        GameEvent event = this.comm.handleCommand("go east", GameEventType.SEE);
        GameEventSubject.assertThat(event).ignoringCase().contains("Monsters that you can see:");
        String extract = event.printString();
        int monsters_list_index = extract.indexOf("Monsters that you can see:");
        int creature_index = extract.indexOf("**monster-", monsters_list_index);
        int endcreature_index = extract.indexOf("**", creature_index + 1);
        extract = extract.substring(creature_index + "**monster-".length(), endcreature_index);
        System.out.println(extract);
        Truth.assertThat(extract).isNotNull();
        Truth.assertThat(extract).isNotEmpty();
        Truth.assertThat(extract.isBlank()).isFalse();
        GameEvent room = this.comm.handleCommand("see", GameEventType.SEE);
        ArgumentMatcher<GameEvent> battleTurn = new MessageMatcher(GameEventType.BATTLE_ROUND,
                "should enter an action to take for the round");
        ArgumentMatcher<GameEvent> battleTurnAccepted = new MessageMatcher(GameEventType.BATTLE_ROUND,
                "action has been submitted for the round");
        ArgumentMatcher<GameEvent> fightOver = new MessageMatcher(GameEventType.FIGHT_OVER);
        ArgumentMatcher<GameEvent> reincarnated = new MessageMatcher(GameEventType.REINCARNATION);
        int i = 1;
        for (i = 1; i < 15 && room.printString().contains("**monster-" + extract + "**"); i++) {
            this.comm.handleCommand("attack " + extract);
            Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(i)).send(Mockito.argThat(battleTurnAccepted));

            Mockito.verify(this.comm.sssb, Mockito.timeout(SubArea.DEFAULT_MILLISECONDS + 500).atLeast(i))
                    .send(Mockito.argThat((gameEvent) -> {
                        return battleTurn.matches(gameEvent) || fightOver.matches(gameEvent)
                                || reincarnated.matches(gameEvent);
                    }));
            room = this.comm.handleCommand("see");

        }
        Truth.assertWithMessage("Expected more than one round.  The room looks like so: %s", room.printString()).that(i)
                .isGreaterThan(1);
        GameEventSubject.assertThat(room).asXML().doesNotContain("<monster>" + extract + "</monster>");
    }

    @Test
    void testUsePotionInBattle() {
        this.comm.create("AttackTester");
        GameEvent event = this.comm.handleCommand("go east", GameEventType.SEE);
        this.comm.handleCommand("equip Shield"); // try to prolong the battle
        this.comm.handleCommand("equip Leather Armor");
        this.comm.handleCommand("inventory");
        GameEventSubject.assertThat(event).ignoringCase().contains("Monsters that you can see:");
        String extract = event.printString();
        int monsters_list_index = extract.indexOf("Monsters that you can see:");
        int creature_index = extract.indexOf("**monster-", monsters_list_index);
        int endcreature_index = extract.indexOf("**", creature_index + 1);
        extract = extract.substring(creature_index + "**monster-".length(), endcreature_index);
        System.out.println(extract);
        Truth.assertThat(extract).isNotNull();
        Truth.assertThat(extract).isNotEmpty();
        Truth.assertThat(extract.isBlank()).isFalse();
        ArgumentMatcher<GameEvent> battleTurn = new MessageMatcher(GameEventType.BATTLE_ROUND,
                "should enter an action to take for the round");
        ArgumentMatcher<GameEvent> battleTurnAccepted = new MessageMatcher(GameEventType.BATTLE_ROUND,
                "action has been submitted for the round");

        this.comm.handleCommand("attack " + extract);
        Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(1)).send(Mockito.argThat(battleTurnAccepted));
        Mockito.verify(this.comm.sssb, Mockito.timeout(SubArea.DEFAULT_MILLISECONDS + 500).atLeast(1))
                .send(Mockito.argThat(battleTurn));
        this.comm.handleCommand("use Regular Potion of Healing");
        Mockito.verify(this.comm.sssb, Mockito.timeout(50000))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.USE, List.of("used this"), null, null, true)));
        Mockito.verify(this.comm.sssb, Mockito.timeout(500).atLeast(2)).send(Mockito.argThat(battleTurnAccepted));

    }

    @Test
    void testEquipment() {
        this.comm.create("Tester");
        final GameEvent status1 = this.comm.handleCommand("status");
        final GameEvent inventory1 = this.comm.handleCommand("inventory");
        GameEventSubject.assertThat(inventory1).contains("Shield");
        GameEventSubject.assertThat(inventory1).contains("nothing equipped");
        this.comm.handleCommand("equip shield");
        final GameEvent inventory2 = this.comm.handleCommand("inventory");
        GameEventSubject.assertThat(inventory2).asString().isNotEqualTo(inventory1.printString());
        GameEventSubject.assertThat(this.comm.handleCommand("status")).asString().isNotEqualTo(status1.printString());
        this.comm.handleCommand("unequip shield");
        GameEventSubject.assertThat(this.comm.handleCommand("inventory")).asString()
                .isEqualTo(inventory1.printString());
        GameEventSubject.assertThat(this.comm.handleCommand("status")).asString().isEqualTo(status1.printString());
    }

    @Test
    void testReincarnation() throws IOException {
        final int waitMillis = 500;
        this.comm.create("Tester");
        this.comm.handleCommand("go east");
        GameEvent status = this.comm.handleCommand("status");
        GameEvent inventory = this.comm.handleCommand("inventory");

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

        ArgumentMatcher<GameEvent> fightStarted = new MessageMatcher(GameEventType.START_FIGHT);
        ArgumentMatcher<GameEvent> battleTurn = new MessageMatcher(GameEventType.BATTLE_ROUND,
                "should enter an action to take for the round");
        ArgumentMatcher<GameEvent> battleTurnAccepted = new MessageMatcher(GameEventType.BATTLE_ROUND,
                "action has been submitted for the round");

        attacker.handleCommand("attack Tester");
        Mockito.verify(attacker.sssb, Mockito.timeout(waitMillis).atLeastOnce()).send(Mockito.argThat(fightStarted));
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis).atLeastOnce()).send(Mockito.argThat(fightStarted));
        Mockito.verify(attacker.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurnAccepted));

        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurn));
        this.comm.handleCommand("PASS");
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurnAccepted));

        for (int i = 1; i < 30; i++) {
            this.comm.handleCommand("SEE");
            final GameEvent seen = this.comm.outCaptor.getAllValues().stream()
                    .filter(event -> event != null && GameEventType.SEE.equals(event.getXmlEventType()))
                    .reduce((a, b) -> b).orElse(null); // watch out for infinite streams here
            if (seen == null || !seen.toString().contains("Attacker")) {
                System.out.printf("Attacker not found %d: \"%s\"\n", i, seen);
                break;
            }
            battleTurn = new MessageMatcher(GameEventType.BATTLE_ROUND,
                    List.of("should enter an action to take for the round", "It is round", Integer.toString(i)),
                    List.of());
            Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurn));
            Mockito.verify(attacker.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurn));
            attacker.handleCommand("attack Tester");
            this.comm.handleCommand("PASS");
            battleTurnAccepted = new MessageMatcher(GameEventType.BATTLE_ROUND,
                    List.of("action has been submitted for the round", "It is round", Integer.toString(i)), List.of());
            Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurnAccepted));
            Mockito.verify(attacker.sssb, Mockito.timeout(waitMillis)).send(Mockito.argThat(battleTurnAccepted));

        }
        System.out.println("Exited attack loop");
        Mockito.verify(this.comm.sssb, Mockito.timeout(waitMillis))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.REINCARNATION)));
        GameEventSubject.assertThat(attacker.handleCommand("SEE")).asString().doesNotContainMatch("<.+>Tester<.+>");
        GameEventSubject.assertThat(this.comm.handleCommand("inventory")).asString().isEqualTo(inventory.printString());
        GameEventSubject.assertThat(this.comm.handleCommand("status")).asString().isEqualTo(status.printString());
    }

    @Test
    void testReinforcements() throws IOException {
        this.comm.create("Tester");
        this.comm.handleCommand("GO east");
        ServerClientComBundle second = new ServerClientComBundle(this.server);
        second.create("second");
        second.handleCommand("GO east");
        ServerClientComBundle bystander = new ServerClientComBundle(this.server);
        bystander.create("bystander");
        bystander.handleCommand("GO east");

        MessageMatcher renegadeMatcher = new MessageMatcher(GameEventType.RENEGADE_ANNOUNCEMENT, Set.of("RENEGADE"),
                null, null, null);

        this.comm.handleCommand("attack " + second.name);
        Mockito.verify(this.comm.sssb, Mockito.atLeastOnce()).send(Mockito.argThat(renegadeMatcher));
        Mockito.verify(bystander.sssb, Mockito.timeout(500).atLeastOnce()).send(Mockito.argThat(renegadeMatcher));
        Mockito.verify(bystander.sssb, Mockito.timeout(500).atLeastOnce())
                .send(Mockito.argThat(new MessageMatcher(GameEventType.JOIN_BATTLE)));
    }

    @Test
    void testCasting() throws IOException {
        this.comm.create("Tester");
        ServerClientComBundle victim = new ServerClientComBundle(this.server);
        victim.create("victim");
        ServerClientComBundle caster = new ServerClientComBundle(this.server);
        caster.create("Caster", "MAGE", true);

        GameEvent spellResult = this.comm.handleCommand("cast zarmamoo"); // Thaumaturgy
        // because we know it's thaumaturgy
        // GameEventSubject.assertThat(spellResult).contains(this.comm.name);
        GameEventSubject.assertThat(spellResult).ignoringCase().contains("was not handled");
        // Truth.assertThat(victim.read()).contains(this.comm.name);

        spellResult = this.comm.handleCommand("cast Astra Horeb at " + victim.name); // attack spell
        // GameEventSubject.assertThat(spellResult).ignoringCase().contains("fight");
        GameEventSubject.assertThat(spellResult).ignoringCase().contains("was not handled");

        spellResult = caster.handleCommand("cast zarmamoo");
        if (!spellResult.printString().contains("should have done something")) {
            GameEventSubject.assertThat(spellResult).ignoringCase().contains("used");
            GameEventSubject.assertThat(spellResult).ignoringCase().contains("Thaumaturgy");
        } else {
            GameEventSubject.assertThat(spellResult).ignoringCase().doesNotContain("Thaumaturgy");
        }

    }

    @Test
    void testSpellbook() throws IOException {
        this.comm.create("Notmage");
        ServerClientComBundle mage = new ServerClientComBundle(this.server);
        mage.create("mage", "MAGE", true);

        this.comm.handleCommand("spellbook");
        Mockito.verify(this.comm.sssb, Mockito.timeout(500))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.BAD_MESSAGE, "was not handled")));
        mage.handleCommand("spellbook");
        Mockito.verify(mage.sssb, Mockito.timeout(500))
                .send(Mockito.argThat(new MessageMatcher(GameEventType.SPELL_ENTRY, "Thaumaturgy")));
    }
}
