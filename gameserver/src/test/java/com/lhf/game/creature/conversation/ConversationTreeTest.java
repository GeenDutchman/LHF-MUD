package com.lhf.game.creature.conversation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.RichOutput.RichOutputElement;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;
import com.lhf.game.creature.conversation.ConversationTreeNode.Builder;
import com.lhf.game.serialization.GsonBuilderFactory;
import com.lhf.server.client.Client.ClientID;

@ExtendWith(MockitoExtension.class)
public class ConversationTreeTest {

    private String basicEmpty = "I have nothing to say to you right now.";
    private ICreature talker;
    private ClientID talkerID;

    @BeforeEach
    void init() {
        this.talker = Mockito.mock(INonPlayerCharacter.class);
        this.talkerID = new ClientID();
    }

    @Test
    void testIgnoreUngreeted() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);

        ConversationTree tree = new ConversationTree.Builder().setStartBody(basicEmpty).build();
        ConversationTreeNodeResult response = tree.listen(talker, "unrecongized words like zaosdff");
        Truth.assertThat(response).isNull();
    }

    @Test
    void testOneTrackConversation() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String secondBody = "Yes I am!";
        ConversationTree tree = new ConversationTree.Builder(basicEmpty).addNode(null,
                new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE), secondBody)
                .build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(basicEmpty);
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);
    }

    @Test
    void testTwoTrackConversation() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String secondBody = "Yes I am!";
        String thirdBody = "Fine!";
        ConversationTree tree = new ConversationTree.Builder(basicEmpty)
                .addNode(null, new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                        secondBody)
                .addNode(null, new ConversationPattern("Fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE), thirdBody)
                .build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(basicEmpty);
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.printString()).isNotEqualTo(thirdBody);
        Truth.assertThat(response.printString()).isEqualTo(tree.getEndOfConvo());
    }

    @Test
    void testConvoRollover() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String secondBody = "Yes I am!";
        String thirdBody = "Fine!";
        ConversationTree tree = new ConversationTree.Builder(basicEmpty)
                .addNode(null, new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                        secondBody)
                .addNode(null, new ConversationPattern("Fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE), thirdBody)
                .build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(basicEmpty);
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.printString()).isNotEqualTo(thirdBody);
        Truth.assertThat(response.printString()).isEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "fine!");
        Truth.assertThat(response.printString()).isEqualTo(thirdBody);
        Truth.assertThat(response.printString()).isNotEqualTo(tree.getEndOfConvo());

        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isNotEqualTo(secondBody);
        Truth.assertThat(response.printString()).isEqualTo(tree.getEndOfConvo());
    }

    @Test
    void testRememberSpot() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String secondBody = "Yes I am!";
        Builder secondNode = ConversationTreeNode.Builder.ofString(secondBody);
        String thirdBody = "Fine!";
        ConversationTree tree = new ConversationTree.Builder(basicEmpty)
                .addNode(null, new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                        secondNode)
                .addNode(secondNode.getNodeID(),
                        new ConversationPattern("Fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE), thirdBody)
                .build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(basicEmpty);
        response = tree.listen(talker, "zippity doo dah");
        Truth.assertThat(response.printString()).doesNotContain(tree.getEndOfConvo());
        Truth.assertThat(response.printString()).isEqualTo(tree.getNotRecognized());
        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.printString()).isEqualTo(basicEmpty);
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);
        response = tree.listen(talker, "zippity eh");
        Truth.assertThat(response.printString()).isEqualTo(tree.getNotRecognized());
        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

    }

    @Test
    void testRepeatNode() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String secondBody = "Yes I am!";
        String thirdBody = "Fine!";
        ConversationTree tree = new ConversationTree.Builder(basicEmpty)
                .addNode(null, new ConversationPattern("Are you sure?", "\\bsure\\b.*?", Pattern.CASE_INSENSITIVE),
                        secondBody)
                .addNode(null, new ConversationPattern("Fine!", "^fine\\b!$", Pattern.CASE_INSENSITIVE), thirdBody)
                .build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).isEqualTo(basicEmpty);
        response = tree.listen(talker, "Are you sure?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);

        response = tree.listen(talker, "what was that again?");
        Truth.assertThat(response.printString()).isEqualTo(secondBody);
    }

    @Test
    void testHightlightNext() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        String body1 = "Hello there new young traveller!";
        String body2 = "Why yes, you are a traveller, are you not?";
        ConversationTree tree = new ConversationTree.Builder(body1).addNode(null,
                new ConversationPattern("I'm a traveller?", "\\btraveller\\b", Pattern.CASE_INSENSITIVE), body2)
                .build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        try {
            String xml = response.printXML();
            Truth.assertThat(xml).contains("<convo colored=\"true\">traveller</convo>");
        } catch (ParserConfigurationException | TransformerException e) {
            fail(e);
        }
    }

    @Test
    void testGreetBack() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        RichOutputBuilder builder = new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG)
                .appendChild("I greet you back").appendRichOutputElement(
                        RichOutputElement.ofMetaSignal(ConversationContextKey.TALKER_TAGGED_NAME.name()));
        ConversationTree tree = new ConversationTree.Builder().setStartBody(builder).build();

        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        Truth.assertThat(response.printString()).contains(talker.getName());
    }

    @Test
    void testForbidBranch() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ICreature unwelcome = Mockito.mock(INonPlayerCharacter.class);
        ClientID id = new ClientID();
        Mockito.when(unwelcome.getClientID()).thenReturn(id);
        Mockito.when(unwelcome.getName()).thenReturn("Unwelcome Bob");
        Mockito.when(unwelcome.getTagName()).thenReturn("npc");
        Mockito.when(unwelcome.getSimpleContent()).thenCallRealMethod();

        Truth.assertThat(unwelcome.getName()).isNotEqualTo(talker.getName());

        String oneWay = "I am friendly";
        String otherWay = "I am not friendly";

        RichOutputBuilder builder = new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG)
                .appendChild("I greet you back")
                .appendRichOutputElement(
                        RichOutputElement.ofMetaSignal(ConversationContextKey.TALKER_TAGGED_NAME.name()))
                .appendChild("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree.Builder().setStartBody(builder)
                .addNode(null, new ConversationPattern("I'm welcome?", "\\bwelcome\\b", Pattern.CASE_INSENSITIVE),
                        oneWay,
                        (oneBranch) -> oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                                new ConversationPattern(unwelcome.getName(), "\\b" + unwelcome.getName() + "\\b")))
                .addNode(null, new ConversationPattern("I'm unwelcome?", "\\bunwelcome\\b", Pattern.CASE_INSENSITIVE),
                        otherWay)
                .build();

        // welcome
        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        try {
            String xml = response.printXML();
            Truth.assertThat(xml).ignoringCase().contains("<convo colored=\"true\">welcome</convo>");
            Truth.assertThat(xml).ignoringCase().contains("<convo colored=\"true\">unwelcome</convo>");
        } catch (ParserConfigurationException | TransformerException e) {
            fail(e);
        }

        response = tree.listen(talker, "I think I'm welcome");
        Truth.assertThat(response.printString()).isEqualTo(oneWay);
        response = tree.listen(talker, "But I'll start over");
        Truth.assertThat(response.printString()).contains(tree.getEndOfConvo());
        response = tree.listen(talker, "Am I unwelcome?");
        Truth.assertThat(response.printString()).isEqualTo(otherWay);

        // unwelcome
        response = tree.listen(unwelcome, "hello there!");
        try {
            String xml = response.printXML();
            Truth.assertThat(xml).ignoringCase().doesNotContain("<convo colored=\"true\">welcome</convo>");
            Truth.assertThat(xml).ignoringCase().contains("<convo colored=\"true\">unwelcome</convo>");
        } catch (ParserConfigurationException | TransformerException e) {
            fail(e);
        }

        response = tree.listen(unwelcome, "Am I welcome?");
        Truth.assertThat(response.printString()).ignoringCase().isEqualTo(tree.getNotRecognized());
        response = tree.listen(unwelcome, "Am I unwelcome?");
        Truth.assertThat(response.printString()).isEqualTo(otherWay);

    }

    @Test
    void testDualTriggerForbiddance() {
        Mockito.when(this.talker.getClientID()).thenReturn(this.talkerID);
        Mockito.when(this.talker.getName()).thenReturn("Talker Joe");
        Mockito.when(this.talker.getTagName()).thenReturn("npc");
        Mockito.when(this.talker.getSimpleContent()).thenCallRealMethod();

        ICreature unwelcome = Mockito.mock(INonPlayerCharacter.class);
        ClientID id = new ClientID();
        Mockito.when(unwelcome.getClientID()).thenReturn(id);
        Mockito.when(unwelcome.getName()).thenReturn("Unwelcome Bob");
        Mockito.when(unwelcome.getTagName()).thenReturn("npc");
        Mockito.when(unwelcome.getSimpleContent()).thenCallRealMethod();

        Truth.assertThat(unwelcome.getName()).isNotEqualTo(talker.getName());

        String oneWay = "I am friendly";
        String otherWay = "I am not friendly";

        RichOutputBuilder builder = new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG)
                .appendChild("I greet you back")
                .appendRichOutputElement(
                        RichOutputElement.ofMetaSignal(ConversationContextKey.TALKER_TAGGED_NAME.name()))
                .appendChild("I will test the welcome and the unwelcome both");
        ConversationTree tree = new ConversationTree.Builder().setStartBody(builder).addNode(null,
                new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay, (oneBranch) -> {
                    oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                            new ConversationPattern(unwelcome.getName(), "\\b" + unwelcome.getName() + "\\b"));
                }).addNode(null, new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), otherWay)
                .build();

        // welcome
        ConversationTreeNodeResult response = tree.listen(talker, "hello there!");
        try {
            String xml = response.printXML();
            Truth.assertThat(xml).ignoringCase().contains("<convo colored=\"true\">both</convo>");
        } catch (ParserConfigurationException | TransformerException e) {
            fail(e);
        }
        response = tree.listen(talker, "You test both?");
        Truth.assertThat(response.printString()).isEqualTo(oneWay);

        // unwelcome
        response = tree.listen(unwelcome, "hello there!");
        try {
            String xml = response.printXML();
            Truth.assertThat(xml).ignoringCase().contains("<convo colored=\"true\">both</convo>");
        } catch (ParserConfigurationException | TransformerException e) {
            fail(e);
        }
        response = tree.listen(unwelcome, "You test both?");
        Truth.assertThat(response.printString()).isEqualTo(otherWay);
    }

    @Test
    void testSerialization() {
        RichOutputBuilder builder = new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG)
                .appendChild("I greet you back")
                .appendRichOutputElement(
                        RichOutputElement.ofMetaSignal(ConversationContextKey.TALKER_TAGGED_NAME.name()))
                .appendChild("I will test the welcome and the unwelcome both");

        Builder oneWay = ConversationTreeNode.Builder.ofString("I am friendly");
        Builder otherWay = ConversationTreeNode.Builder.ofString("I am not friendly");

        Builder oneWaySecond = ConversationTreeNode.Builder.ofString("So very friendly!");

        ConversationTree tree = new ConversationTree.Builder().setStartBody(builder).addNode(null,
                new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay, (oneBranch) -> {
                    oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                            new ConversationPattern("badperson", "\\b" + "badperson" + "\\b"));
                }).addNode(null, new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), otherWay)
                .addNode(oneWay.getNodeID(),
                        new ConversationPattern("You are friendly?", "\\bfriendly\\b", Pattern.CASE_INSENSITIVE),
                        oneWaySecond)
                .build();

        Gson gson = GsonBuilderFactory.start().conversation().build();
        String json = gson.toJson(tree);
        System.out.println(json);
        Truth.assertThat(json).ignoringCase().contains("greet");
        Truth.assertThat(json).ignoringCase().contains("test");
        Truth.assertThat(json).ignoringCase().contains("friendly");
        Truth.assertThat(json).ignoringCase().contains("badperson");

        ConversationTree secondTree = gson.fromJson(json, ConversationTree.class);
        Truth.assertThat(secondTree).isInstanceOf(ConversationTree.class);
        Truth.assertThat(secondTree.getTreeName()).isEqualTo(tree.getTreeName());
        Truth.assertThat(secondTree).isEqualTo(tree);

    }

    @Test
    void testMermaid() {
        RichOutputBuilder builder = new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG)
                .appendChild("I greet you back")
                .appendRichOutputElement(
                        RichOutputElement.ofMetaSignal(ConversationContextKey.TALKER_TAGGED_NAME.name()))
                .appendChild("I will test the welcome and the unwelcome both");

        Builder oneWay = ConversationTreeNode.Builder.ofString("I am friendly");
        Builder otherWay = ConversationTreeNode.Builder.ofString("I am not friendly");
        oneWay.addPrompt("PROMPT DROP money");

        Builder oneWaySecond = ConversationTreeNode.Builder.ofString("So very friendly!");

        ConversationTree.Builder treeBuilder = new ConversationTree.Builder().setStartBody(builder).addNode(null,
                new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), oneWay, (oneBranch) -> {
                    oneBranch.addRule(ConversationContextKey.TALKER_NAME,
                            new ConversationPattern("badperson", "\\b" + "badperson" + "\\b"));
                }).addNode(null, new ConversationPattern("both?", "\\bboth\\b", Pattern.CASE_INSENSITIVE), otherWay)
                .addNode(oneWay.getNodeID(),
                        new ConversationPattern("You are friendly?", "\\bfriendly\\b", Pattern.CASE_INSENSITIVE),
                        oneWaySecond);

        String mermaid = treeBuilder.toMermaidStateDiagram(false);
        System.out.println(mermaid);
        Truth.assertThat(mermaid).ignoringCase().contains("greet");
        Truth.assertThat(mermaid).ignoringCase().contains("test");
        Truth.assertThat(mermaid).ignoringCase().contains("friendly");
        Truth.assertThat(mermaid).ignoringCase().contains("badperson");

        ConversationTree tree = treeBuilder.build();

        String mermaid2 = tree.toMermaidStateDiagram(false);
        System.out.println(mermaid2);
        Truth.assertThat(mermaid2).ignoringCase().contains("greet");
        Truth.assertThat(mermaid2).ignoringCase().contains("test");
        Truth.assertThat(mermaid2).ignoringCase().contains("friendly");
        Truth.assertThat(mermaid2).ignoringCase().contains("badperson");
    }

    @Test
    void copyTest() {
        ConversationTree.Builder builder = StaticConversationTreeProducer.produceGary();
        final String builderMermaid = builder.toMermaidStateDiagram(false);
        // System.out.println("builder");
        // System.out.println(builderMermaid);

        ConversationTree built = builder.build();
        final String builtMermaid = built.toMermaidStateDiagram(false);
        System.out.println("built");
        System.out.println(builtMermaid);

        Truth.assertThat(builderMermaid).isEqualTo(builtMermaid);

        ConversationTree.Builder copier = ConversationTree.Builder.fromTree(built);
        // final String copierMermaid = copier.toMermaidStateDiagram(false);
        // System.out.println("copier");
        // System.out.println(copierMermaid);

        ConversationTree copied = copier.build();
        final String copiedMermaid = copied.toMermaidStateDiagram(false);
        System.out.println("copied");
        System.out.println(copiedMermaid);

        Truth.assertThat(copiedMermaid).isEqualTo(builtMermaid);

        Gson gson = GsonBuilderFactory.start().prettyPrinting().conversation().build();

        final String asJson = gson.toJson(built, ConversationTree.class);
        // System.out.println("json");
        // System.out.println(asJson);

        ConversationTree fromJson = gson.fromJson(asJson, ConversationTree.class);
        final String jsonMermaid = fromJson.toMermaidStateDiagram(false);
        System.out.println("fromJson");
        System.out.println(jsonMermaid);

        ConversationTree.Builder jsonCopier = ConversationTree.Builder.fromTree(fromJson);
        // final String jsonCopierMermaid = jsonCopier.toMermaidStateDiagram(false);
        // System.out.println("jsonCopierMermaid");
        // System.out.println(jsonCopierMermaid);

        ConversationTree jsonCopied = jsonCopier.build();
        final String jsonCopiedMermaid = jsonCopied.toMermaidStateDiagram(false);
        System.out.println("jsonCopiedMermaid");
        System.out.println(jsonCopiedMermaid);

        Truth.assertThat(jsonMermaid).isEqualTo(builtMermaid);

    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @TestMethodOrder(OrderAnnotation.class)
    class StaticConversations {
        private ConversationManager manager = new ConversationManager();

        public boolean isRewriteNeeded() {
            return false;
        }

        @Test
        @Order(1)
        void readStaticFiles() {
            ConversationTree nonVerbal = assertDoesNotThrow(() -> manager.convoTreeFromFile("non_verbal_default"),
                    () -> "Failed to load non verbal tree, rewrite it");
            String mermaid = nonVerbal.toMermaidStateDiagram(false);
            System.out.println("non_verbal_default");
            System.out.println(mermaid);
            Truth.assertThat(mermaid).ignoringCase().contains("Growl");

            ConversationTree verbal = assertDoesNotThrow(() -> manager.convoTreeFromFile("verbal_default"),
                    () -> "Failed to load verbal tree, rewrite it");
            mermaid = verbal.toMermaidStateDiagram(false);
            System.out.println("verbal_default");
            System.out.println(mermaid);
            Truth.assertThat(mermaid).ignoringCase().contains("secrets");
        }

        @Test
        @Order(1)
        void readGary() {
            ConversationTree gary = assertDoesNotThrow(() -> manager.convoTreeFromFile("gary"),
                    () -> "Failed to load gary's tree, rewrite it");
            String mermaid = gary.toMermaidStateDiagram(false);
            System.out.println("gary");
            System.out.println(mermaid);
            Truth.assertThat(mermaid).contains("CREATE_VOCATION");

        }

        @Test
        @Order(2)
        @EnabledIf("isRewriteNeeded")
        void writeNonVerbalDefault() {
            ConversationTree.Builder builder = StaticConversationTreeProducer.produceNonVerbalDefault();

            ConversationTree built = builder.build();
            boolean written = assertDoesNotThrow(() -> manager.convoTreeToFile(built), () -> "Failed to write tree");
            Truth.assertThat(written).isTrue();
        }

        @Test
        @Order(3)
        @EnabledIf("isRewriteNeeded")
        void writeVerbalDefault() {
            ConversationTree.Builder builder = StaticConversationTreeProducer.produceVerbalDefault();

            ConversationTree built = builder.build();
            boolean written = assertDoesNotThrow(() -> manager.convoTreeToFile(built), () -> "Failed to write tree");
            Truth.assertThat(written).isTrue();
        }

        @Test
        @Order(4)
        @EnabledIf("isRewriteNeeded")
        void writeAggravated() {
            ConversationTree.Builder builder = StaticConversationTreeProducer.produceAggravated();

            ConversationTree built = builder.build();
            boolean written = assertDoesNotThrow(() -> manager.convoTreeToFile(built), () -> "Failed to write tree");
            Truth.assertThat(written).isTrue();

        }

        @Test
        @Order(5)
        // @EnabledIf("isRewriteNeeded")
        void writeGary() {

            ConversationTree.Builder builder = StaticConversationTreeProducer.produceGary();

            ConversationTree built = builder.build();
            boolean written = assertDoesNotThrow(() -> manager.convoTreeToFile(built), () -> "Failed to write tree");
            Truth.assertThat(written).isTrue();

            ConversationTree gary = assertDoesNotThrow(() -> manager.convoTreeFromFile("gary"),
                    () -> "Failed to load gary's tree, rewrite it");
            String mermaid = gary.toMermaidStateDiagram(false);
            System.out.println("gary");
            System.out.println(mermaid);
            Truth.assertThat(mermaid).contains("CREATE_VOCATION");
            Truth.assertThat(built.toMermaidStateDiagram(false)).isEqualTo(mermaid);

        }
    }
}
