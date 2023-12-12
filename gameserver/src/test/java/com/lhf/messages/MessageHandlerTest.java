package com.lhf.messages;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest {
    @Mock
    private MessageChainHandler leafNodeOne;
    @Mock
    private MessageChainHandler leafNodeTwo;
    @Mock
    private MessageChainHandler branchNode;
    @Mock
    private MessageChainHandler rootNode;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    public void buildTree() {
        when(this.rootNode.getSuccessor()).thenReturn(null);
        when(this.branchNode.getSuccessor()).thenReturn(this.rootNode);
        when(this.leafNodeOne.getSuccessor()).thenReturn(this.branchNode);
        when(this.leafNodeTwo.getSuccessor()).thenReturn(this.branchNode);

        /**
         * //
         * when(this.rootNode.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         * //
         * when(this.branchNode.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         * //
         * when(this.leafNodeOne.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         * //
         * when(this.leafNodeTwo.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         */

        Map<CommandMessage, String> leafNodeOneHelps = new HashMap<>();
        leafNodeOneHelps.put(CommandMessage.HELP, "When you need help");
        leafNodeOneHelps.put(CommandMessage.INVENTORY, "When you need to know what you have");
        when(this.leafNodeOne.getCommands(Mockito.isA(CommandContext.class))).thenAnswer(i -> {
            CommandContext cc = i.getArgument(0);
            if (cc != null) {
                cc.addHelps(leafNodeOneHelps);
            }
            return leafNodeOneHelps;
        });

        Map<CommandMessage, String> leafNodeTwoHelps = new HashMap<>();
        leafNodeTwoHelps.put(CommandMessage.CAST, "If you are a caster");
        when(this.leafNodeTwo.getCommands(Mockito.isA(CommandContext.class))).thenAnswer(i -> {
            CommandContext cc = i.getArgument(0);
            if (cc != null) {
                cc.addHelps(leafNodeTwoHelps);
            }
            return leafNodeTwoHelps;
        });

        Map<CommandMessage, String> branchNodeHelps = new HashMap<>();
        branchNodeHelps.put(CommandMessage.HELP, "When you get higher help");
        branchNodeHelps.put(CommandMessage.SEE, "I added something!");
        when(this.branchNode.getCommands(Mockito.isA(CommandContext.class))).thenAnswer(i -> {
            CommandContext cc = i.getArgument(0);
            if (cc != null) {
                cc.addHelps(branchNodeHelps);
            }
            return branchNodeHelps;
        });

        // root node does nothing
        when(this.rootNode.getCommands(any())).thenReturn(null);

    }

    private CommandContext follow(MessageChainHandler mh, CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        MessageChainHandler following = mh;
        while (following != null) {
            following.getCommands(ctx);
            following = following.getSuccessor();
        }
        return ctx;
    }

    @Test
    void testGetCommands() {
        buildTree();
        Map<CommandMessage, String> receivedNodeOne = this.follow(this.leafNodeOne, null).getHelps();
        System.out.println(receivedNodeOne);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.INVENTORY);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.HELP);
        Truth.assertThat(receivedNodeOne).doesNotContainKey(CommandMessage.CAST);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.SEE);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.HELP);
        Truth.assertThat(receivedNodeOne.get(CommandMessage.HELP)).doesNotContain("higher");

        Map<CommandMessage, String> rec = this.follow(this.leafNodeTwo, null).getHelps();
        Truth.assertThat(rec).doesNotContainKey(CommandMessage.INVENTORY);
        Truth.assertThat(rec).containsKey(CommandMessage.HELP);
        Truth.assertThat(rec).containsKey(CommandMessage.CAST);
        Truth.assertThat(rec).containsKey(CommandMessage.SEE);
        Truth.assertThat(rec).containsKey(CommandMessage.HELP);
        Truth.assertThat(rec.get(CommandMessage.HELP)).contains("higher");
    }

    // @Test
    // void testHandleMessage() {

    // }
}
